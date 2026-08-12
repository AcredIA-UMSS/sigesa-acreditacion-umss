#!/usr/bin/env python3
"""Genera ENTREGA-TOOL-CALLING-SEMANA.docx sin dependencias externas."""
from __future__ import annotations

import html
import zipfile
from pathlib import Path

OUT = Path(__file__).with_name("ENTREGA-TOOL-CALLING-SEMANA.docx")

WNS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"

# Respuesta real esperada del seed (AssistantResponseFormatter.formatPhases)
RESPUESTA_FASES = """Fases del proceso **Ingeniería de Sistemas** [CEUB] (INF-SIS):

1. **Fase 1: Autoevaluación Inicial**
   - ID: 550e8400-e29b-41d4-a716-446655440004 | Subfases: …
2. **Fase 2: Verificación de Evidencias**
   - ID: 550e8400-e29b-41d4-a716-446655440005 | Subfases: …
3. **Fase 3: Dictamen de Pares Evaluadores**
   - ID: 550e8400-e29b-41d4-a716-446655440006 | Subfases: …"""

RESPUESTA_FUERA_ALCANCE = """No puedo responder eso con las herramientas disponibles en SIGESA.

Puedo ayudarte con:
• Listar fases del proceso activo de una carrera (palabra clave: «fases»).
• Listar carreras con proceso de acreditación activo.
• Listar carreras/programas académicos.
• Gestionar fases del proceso activo (con confirmación en chat).
• Listar usuarios registrados (palabra clave: «usuarios»).
• Activar o desactivar usuarios (con confirmación en chat)."""

RESPUESTA_LLM_APAGADA_SINONIMO = """No puedo responder eso con las herramientas disponibles en SIGESA.

La consulta no coincide con el catálogo de palabras clave y la IA está desactivada (SIGESA_ASSISTANT_LLM_ENABLED=false). Use frases del catálogo o active la IA para preguntas con sinónimos.

Puedo ayudarte con:
• Listar fases del proceso activo de una carrera (palabra clave: «fases»).
• Listar carreras con proceso de acreditación activo.
…"""


def esc(text: str) -> str:
    return html.escape(text, quote=False)


def p(text: str, bold: bool = False, size: int | None = None) -> str:
    rpr = ""
    if bold:
        rpr += "<w:b/>"
    if size:
        rpr += f'<w:sz w:val="{size * 2}"/><w:szCs w:val="{size * 2}"/>'
    rpr_xml = f"<w:rPr>{rpr}</w:rPr>" if rpr else ""
    lines = text.split("\n")
    parts = []
    for i, line in enumerate(lines):
        br = "<w:br/>" if i > 0 else ""
        parts.append(
            f"<w:r>{rpr_xml if i == 0 else ''}{br}<w:t xml:space=\"preserve\">{esc(line)}</w:t></w:r>"
        )
    return f"<w:p>{''.join(parts)}</w:p>"


def heading(text: str, level: int) -> str:
    return (
        f'<w:p><w:pPr><w:pStyle w:val="Heading{level}"/></w:pPr>'
        f'<w:r><w:t xml:space="preserve">{esc(text)}</w:t></w:r></w:p>'
    )


def bullet(text: str) -> str:
    return (
        f'<w:p><w:pPr><w:pStyle w:val="ListParagraph"/>'
        f'<w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr></w:pPr>'
        f'<w:r><w:t xml:space="preserve">{esc(text)}</w:t></w:r></w:p>'
    )


def table(headers: list[str], rows: list[list[str]], header_fill: str = "D9E2F3") -> str:
    col_count = len(headers)
    col_width = max(2400, 9600 // col_count)
    grid = "".join(f'<w:gridCol w:w="{col_width}"/>' for _ in headers)
    parts = [
        "<w:tbl>",
        "<w:tblPr><w:tblW w:w=\"5000\" w:type=\"pct\"/><w:tblBorders>"
        "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
        "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
        "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
        "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
        "<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
        "<w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
        "</w:tblBorders></w:tblPr>",
        f"<w:tblGrid>{grid}</w:tblGrid>",
    ]

    def row(cells: list[str], header: bool = False) -> str:
        xml = "<w:tr>"
        for cell in cells:
            fill = header_fill if header else "FFFFFF"
            cell_p = p(cell, bold=header)
            xml += (
                f"<w:tc><w:tcPr><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"{fill}\"/>"
                f"</w:tcPr>{cell_p}</w:tc>"
            )
        xml += "</w:tr>"
        return xml

    parts.append(row(headers, header=True))
    for row_data in rows:
        parts.append(row(row_data))
    parts.append("</w:tbl>")
    return "".join(parts)


def evidence_table(fields: dict[str, str]) -> str:
    """Tabla Campo | Completar por escenario."""
    rows = [[k, v] for k, v in fields.items()]
    return table(["Campo", "Completar"], rows, header_fill="E2EFDA")


SCENARIOS = [
    {
        "title": "Escenario 1 — Controlado (catálogo KEYWORD)",
        "fields": {
            "Pregunta del usuario": "Lista las fases de Ingeniería de Sistemas CEUB",
            "Camino tomado": "KEYWORD",
            "Herramienta usada": "list_process_phases",
            "Tabla / fuente de datos": "public.phases, public.subphases, public.accreditation_processes, public.programs",
            "Modelo y versión (si aplica)": "No aplica — el LLM no fue invocado",
            "Respuesta obtenida": RESPUESTA_FASES,
        },
    },
    {
        "title": "Escenario 2 — Sinónimo / paráfrasis (LLM elige tool)",
        "fields": {
            "Pregunta del usuario": "¿Qué etapas tiene el proceso activo de Ingeniería de Sistemas CEUB?",
            "Camino tomado": "LLM",
            "Herramienta usada": "list_process_phases",
            "Tabla / fuente de datos": "public.phases, public.subphases, public.accreditation_processes, public.programs",
            "Modelo y versión (si aplica)": "qwen2.5:7b (Ollama, pin fijado en docker-compose — no «latest»)",
            "Respuesta obtenida": RESPUESTA_FASES + "\n\n(Misma respuesta que escenario 1; el LLM solo eligió la tool; el texto lo produjo el código.)",
        },
    },
    {
        "title": "Escenario 3 — Fuera de alcance",
        "fields": {
            "Pregunta del usuario": "¿Cuál es el presupuesto de la universidad para 2027?",
            "Camino tomado": "OUT_OF_SCOPE",
            "Herramienta usada": "ninguna",
            "Tabla / fuente de datos": "— (dato no existe en SIGESA)",
            "Modelo y versión (si aplica)": "No aplica — AssistantOutOfScopeDetector; LLM no invocado",
            "Respuesta obtenida": RESPUESTA_FUERA_ALCANCE,
        },
    },
    {
        "title": "Escenario 4 — Modelo apagado (SIGESA_ASSISTANT_LLM_ENABLED=false)",
        "fields": {
            "Pregunta del usuario": "Lista las fases de Ingeniería de Sistemas CEUB",
            "Camino tomado": "KEYWORD",
            "Herramienta usada": "list_process_phases",
            "Tabla / fuente de datos": "public.phases, public.subphases, public.accreditation_processes, public.programs",
            "Modelo y versión (si aplica)": "No aplica — SIGESA_ASSISTANT_LLM_ENABLED=false",
            "Respuesta obtenida": RESPUESTA_FASES
            + "\n\n--- Prueba complementaria (misma sesión, IA apagada) ---\n"
            + "Pregunta: «¿Qué etapas tiene el proceso activo de Ingeniería de Sistemas CEUB?»\n"
            + RESPUESTA_LLM_APAGADA_SINONIMO,
        },
    },
]


def build_document_xml() -> str:
    body: list[str] = []

    body.append(heading("Entrega — Tool Calling en SIGESA", 1))
    body.append(p("Tarea de la semana: Tool calling en su propio sistema", bold=True, size=14))
    body.append(p(""))
    body.append(p("Proyecto: SIGESA — Sistema de Gestión de Acreditación UMSS", bold=True))
    body.append(p("Entrega: 8 de agosto de 2026"))
    body.append(p("Repositorio: https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss"))
    body.append(p("Rama: feature/toolcalling"))
    body.append(p("URL demo: http://localhost:3000/ayuda (usuario JD/TD)"))

    body.append(heading("1. Objetivo cumplido", 2))
    body.append(p(
        "El asistente demuestra tool calling sobre SIGESA: el LLM solo elige la herramienta cuando "
        "no hay palabra clave en el catálogo; la respuesta con datos la produce siempre el código Java."
    ))

    body.append(heading("2. Interruptor IA", 2))
    body.append(table(
        ["Variable", "Efecto"],
        [
            ["SIGESA_ASSISTANT_ENABLED=true", "Módulo asistente activo"],
            ["SIGESA_ASSISTANT_LLM_ENABLED=true", "Escenario 2 (sinónimos) habilitado"],
            ["SIGESA_ASSISTANT_LLM_ENABLED=false", "Escenarios 1 y 4 OK; esc. 2 → NINGUNO"],
            ["SIGESA_ASSISTANT_MODEL=qwen2.5:7b", "Modelo fijado"],
        ],
    ))

    body.append(heading("3. Tools publicadas", 2))
    body.append(table(
        ["Tool", "Roles", "Tablas fuente"],
        [
            ["list_users", "JD", "app_user"],
            ["set_user_status", "JD", "app_user"],
            ["list_programs", "JD, TD", "programs"],
            ["list_active_processes", "JD, TD", "accreditation_processes, programs, templates"],
            ["list_process_phases", "JD, TD", "phases, subphases, accreditation_processes, programs"],
            ["manage_process_phase", "JD, TD", "phases, subphases"],
        ],
    ))

    body.append(heading("3.1 Resumen para informe (Campo | Completar)", 2))

    body.append(heading("Tools publicadas", 3))
    body.append(evidence_table({
        "Cantidad de tools registradas": "6 tools en AssistantToolRegistry",
        "list_users": "JD · lectura · app_user · ListUsersUseCase",
        "set_user_status": "JD · escritura (confirmación) · app_user · Activate/DeactivateUserUseCase",
        "list_programs": "JD, TD · lectura · programs · ListProgramsUseCase",
        "list_active_processes": "JD, TD · lectura · accreditation_processes, programs, templates · procesos ACTIVE",
        "list_process_phases": "JD, TD · lectura · phases, subphases, accreditation_processes, programs · demo esc. 1/2/4",
        "manage_process_phase": "JD, TD · escritura (confirmación) · phases, subphases · ProcessStructureController",
        "Regla de oro": "El LLM solo elige tool; la respuesta con datos la produce siempre AssistantResponseFormatter (código Java)",
    }))

    body.append(heading("Modelo y versión", 3))
    body.append(evidence_table({
        "Modelo LLM seleccionado": "qwen2.5:7b",
        "Variable de configuración": "SIGESA_ASSISTANT_MODEL=qwen2.5:7b (docker-compose.yml)",
        "Runtime": "Ollama (contenedor sigesa-ollama)",
        "Proxy API": "Open WebUI → http://open-webui:8080/api/v1/chat/completions",
        "Digest Ollama": "845dbda0ea48 · 4.7 GB · Q4_K_M",
        "Política de versionado": "Modelo pinneado por tag; no se usa «latest» ni alias flotante para inferencia",
        "Modelo descartado": "llama3.2:3b — alucinaciones y tool calling poco fiable",
        "Escenarios sin LLM": "Esc. 1 y 4: camino KEYWORD; Esc. 3: OUT_OF_SCOPE sin invocar modelo",
    }))

    body.append(heading("Qué no funcionó (y mitigación)", 3))
    body.append(evidence_table({
        "llama3.2:3b": "Alucinaba respuestas e inventaba fases genéricas → reemplazado por qwen2.5:7b",
        "Open WebUI — Model not found (HTTP 400)": "Lista de modelos vacía; OWUI no alcanzaba Ollama (red Docker) + permisos → BYPASS_MODEL_ACCESS_CONTROL=true; reconectar stack; alternativa: Ollama directo :11434",
        "Timeout en /ayuda (~60 s)": "nginx proxy_read_timeout corto con inferencia lenta en CPU → proxy_read_timeout 300s",
        "Escenario 3 — presupuesto": "LLM eligió erróneamente list_active_processes → AssistantOutOfScopeDetector antes del LLM",
        "API key Open WebUI": "401/403 al recrear contenedor → regenerar key en OWUI y actualizar SIGESA_ASSISTANT_API_KEY",
        "Escenario 4 vs 2 (IA apagada)": "Sinónimos («etapas») no resuelven sin LLM → comportamiento esperado; demuestra valor del LLM",
    }))

    body.append(heading("4. Evidencia por escenario (tablas de prueba)", 2))
    body.append(p(
        "Cada escenario incluye la tabla de evidencia solicitada. "
        "Los valores corresponden al seed PostgreSQL y al comportamiento implementado en /ayuda."
    ))

    for scenario in SCENARIOS:
        body.append(heading(scenario["title"], 3))
        body.append(evidence_table(scenario["fields"]))
        body.append(p(""))

    body.append(heading("5. Cómo reproducir", 2))
    for step in [
        "docker compose up -d --build",
        "Login: jd@umss.edu.bo / JefeDemo2026!",
        "Abrir http://localhost:3000/ayuda — panel «Escenarios demo»",
        "Verificar metadata en UI: Herramienta · Fuente · Camino",
        "Esc. 4: SIGESA_ASSISTANT_LLM_ENABLED=false → docker compose up -d backend --force-recreate",
        "Tests: cd backend && ./mvnw test -Dtest=SendChatMessageServiceToolLoopTest",
    ]:
        body.append(bullet(step))

    body.append(heading("6. Referencias", 2))
    for ref in [
        "docs/design/assistant/TOOL-CATALOG.md",
        "docs/design/DD-SYS-002.md §11",
        "docs/product/DTP.md §B.5",
    ]:
        body.append(bullet(ref))

    return (
        f'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
        f'<w:document xmlns:w="{WNS}">'
        f"<w:body>{''.join(body)}<w:sectPr/></w:body></w:document>"
    )


def write_docx(path: Path) -> None:
    content_types = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>"""

    rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

    doc_rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering" Target="numbering.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    styles = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="{WNS}">
  <w:style w:type="paragraph" w:styleId="Normal" w:default="1">
    <w:name w:val="Normal"/><w:qFormat/>
    <w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/>
    <w:rPr><w:b/><w:sz w:val="32"/><w:szCs w:val="32"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Heading2"><w:name w:val="heading 2"/><w:basedOn w:val="Normal"/>
    <w:rPr><w:b/><w:sz w:val="26"/><w:szCs w:val="26"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Heading3"><w:name w:val="heading 3"/><w:basedOn w:val="Normal"/>
    <w:rPr><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="ListParagraph"><w:name w:val="List Paragraph"/><w:basedOn w:val="Normal"/></w:style>
</w:styles>"""

    numbering = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:numbering xmlns:w="{WNS}">
  <w:abstractNum w:abstractNumId="0">
    <w:multiLevelType w:val="hybridMultilevel"/>
    <w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="bullet"/>
      <w:lvlText w:val="•"/><w:lvlJc w:val="left"/>
      <w:pPr><w:ind w:left="720" w:hanging="360"/></w:pPr></w:lvl>
  </w:abstractNum>
  <w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num>
</w:numbering>"""

    with zipfile.ZipFile(path, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("[Content_Types].xml", content_types)
        zf.writestr("_rels/.rels", rels)
        zf.writestr("word/_rels/document.xml.rels", doc_rels)
        zf.writestr("word/document.xml", build_document_xml())
        zf.writestr("word/styles.xml", styles)
        zf.writestr("word/numbering.xml", numbering)


if __name__ == "__main__":
    write_docx(OUT)
    print(f"Generado: {OUT} ({OUT.stat().st_size} bytes)")
