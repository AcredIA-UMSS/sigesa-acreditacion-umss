#!/usr/bin/env python3
"""Genera ENTREGA-ACREDIA-MOD-ASSISTANT.docx — informe tarea AcredIA (sin deps externas)."""
from __future__ import annotations

import html
import zipfile
from pathlib import Path

OUT = Path(__file__).with_name("ENTREGA-ACREDIA-MOD-ASSISTANT.docx")
WNS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"

SCHEMA_RESPONSE = """{
  "type": "object",
  "required": ["reply", "path", "llmInvoked", "steps"],
  "properties": {
    "reply": { "type": "string" },
    "toolId": { "type": ["string", "null"] },
    "sourceTables": { "type": "array", "items": { "type": "string" } },
    "path": { "enum": ["KEYWORD", "LLM", "RAG", "OUT_OF_SCOPE"] },
    "llmInvoked": { "type": "boolean" },
    "steps": {
      "type": "array",
      "items": {
        "required": ["step", "toolId", "sourceTables", "success"],
        "properties": {
          "step": { "type": "integer", "minimum": 1 },
          "toolId": { "type": "string" },
          "sourceTables": { "type": "array" },
          "success": { "type": "boolean" }
        }
      }
    }
  }
}"""

RUN_OUTPUT = """$ docker run --rm -v backend:/work -w /work eclipse-temurin:21-jdk-alpine \\
    ./mvnw test -Dtest="Assistant*,SendChatMessageServiceToolLoopTest,\\
NormativeSearchQueryNormalizerTest,AssistantControllerWebMvcTest"

[INFO] Tests run: 99, Failures: 0, Errors: 0, Skipped: 2
[INFO] BUILD SUCCESS
[INFO] Total time:  02:30 min

Notas:
• Sin Ollama, Groq ni Open WebUI en ejecución (mocks + @WebMvcTest).
• 2 skipped = tests @Disabled por duplicados auditados.
• Comando reproducible en Docker JDK 21 (workaround permisos target/)."""


def esc(text: str) -> str:
    return html.escape(text, quote=False)


def p(text: str, bold: bool = False, mono: bool = False) -> str:
    rpr = ""
    if bold:
        rpr += "<w:b/>"
    if mono:
        rpr += '<w:rFonts w:ascii="Consolas" w:hAnsi="Consolas"/>'
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
            cell_p = p(cell)
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


def build_document_xml() -> str:
    body: list[str] = []

    body.append(heading("Entrega AcredIA — MOD-ASSISTANT (SIGESA)", 1))
    body.append(p("Proyecto: SIGESA — Acreditación UMSS"))
    body.append(p("Alumno: Boris Anthony Angulo Urquieta"))
    body.append(p("Fecha: 6 de septiembre de 2026"))
    body.append(p("Alcance: backend MOD-ASSISTANT (/api/v1/assistant/**) — pirámide S2 (unit) + S3 (integración/contrato)."))
    body.append(p(""))

    body.append(heading("1. Cobertura JaCoCo (MOD-ASSISTANT)", 2))
    body.append(p("Paquetes medidos: application.service.assistant, adapter.out.assistant, AssistantController, NormativeSearchQueryNormalizer."))
    body.append(table(
        ["Métrica", "ANTES (Fase 2b)", "DESPUÉS (Fase 5)", "Δ"],
        [
            ["Instrucciones", "43,9 %", "49,6 %", "+5,7 pp"],
            ["Líneas", "43,6 %", "48,8 %", "+5,2 pp"],
            ["Métodos", "53,8 %", "57,5 %", "+3,7 pp"],
            ["Ramas", "31,4 %", "36,5 %", "+5,1 pp"],
            ["Invocaciones test", "70 (3 fallos)", "99 (0 fallos)", "+29"],
        ],
    ))
    body.append(p(""))
    body.append(p("Clases P0 destacadas (línea DESPUÉS): AssistantController 77,1 % · AssistantKeywordRouter 65,7 % · AssistantResponseFormatter 55,6 % · AssistantToolExecutor 23,2 % · OpenWebUiChatAdapter 0 %."))
    body.append(p("Comando: ./mvnw test jacoco:report -Dtest=\"Assistant*,SendChatMessageServiceToolLoopTest,NormativeSearchQueryNormalizerTest,AssistantControllerWebMvcTest\" (Docker JDK 21)."))
    body.append(p(""))

    body.append(heading("2. Tabla de duplicados auditados", 2))
    body.append(table(
        ["Test omitido (@Disabled)", "Duplica a (mantener)", "Decisión"],
        [
            [
                "AssistantToolRbacGuardTest.tdUsersAgentSubset_excludesListUsersForTdRole",
                "AssistantToolRegistryTest.toolsForRoleAndAgent_tdUsersProfile_isEmpty",
                "Omitir RbacGuard — mismo SUT y assert .isEmpty() para TD+USERS",
            ],
            [
                "AssistantToolRbacGuardTest.eeHasNormativeSearchOnly",
                "AssistantToolRegistryTest.toolsForRole_eeHasNormativeSearchOnly",
                "Omitir RbacGuard — mismo SUT registry.toolsForRole(\"EE\")",
            ],
        ],
    ))
    body.append(p(""))

    body.append(heading("3. Tests omitidos (@Disabled) con motivo", 2))
    body.append(table(
        ["Clase", "Método", "Motivo"],
        [
            [
                "AssistantToolRbacGuardTest",
                "tdUsersAgentSubset_excludesListUsersForTdRole",
                "Duplicado funcional de AssistantToolRegistryTest (subset users TD). No se borra código.",
            ],
            [
                "AssistantToolRbacGuardTest",
                "eeHasNormativeSearchOnly",
                "Duplicado funcional de AssistantToolRegistryTest (EE solo normativa). No se borra código.",
            ],
        ],
    ))
    body.append(p(""))

    body.append(heading("4. Auditoría por capa — agente vs aceptados", 2))
    body.append(p("Flujo: agente propone → auditoría humana test por test → integración en paquete definitivo."))
    body.append(p(""))
    body.append(heading("4.1 Capa unit (S2)", 3))
    body.append(table(
        ["Origen", "Generados", "Aceptados", "Descartados", "Motivo descarte"],
        [
            [
                "Generador LLM (Ollama qwen2.5:7b, modo full)",
                "1 lote (~8 métodos)",
                "0",
                "1 lote",
                "Prosa + reescritura de producción; API inventada; sin @Test válido",
            ],
            [
                "Generador LLM (Groq compact)",
                "1 lote (8 métodos)",
                "0",
                "1 lote",
                "API inventada (router.route); package wrong; no compila",
            ],
            [
                "Agente IDE (Cursor, post-auditoría)",
                "21 métodos",
                "21",
                "0",
                "L1 KeywordRouter (8) + L2 Formatter (8) + L3 Executor evidence (5 inv.)",
            ],
        ],
    ))
    body.append(p(""))

    body.append(heading("4.2 Capa integración (S3)", 3))
    body.append(table(
        ["Origen", "Generados", "Aceptados", "Descartados", "Motivo descarte"],
        [
            [
                "Suite baseline (pre-existente)",
                "13 métodos",
                "13 (mantenidos)",
                "0",
                "SendChatMessageServiceToolLoopTest — flujo KEYWORD/LLM/OOS con mocks",
            ],
            [
                "Agente IDE — AssistantControllerWebMvcTest",
                "7 métodos",
                "7",
                "0",
                "HTTP 200/401/400/403/503, GET /status, flujo chat sin invocar LLM real",
            ],
        ],
    ))
    body.append(p(""))

    body.append(heading("4.3 Capa contrato (S3)", 3))
    body.append(table(
        ["Origen", "Generados", "Aceptados", "Descartados", "Motivo descarte"],
        [
            [
                "Agente IDE — WebMvc contrato",
                "1 método (+ asserts en 200 chat)",
                "3 casos",
                "0",
                "steps[] secuencial; campos reply/path/llmInvoked; shapes GET status",
            ],
            [
                "Artefacto JSON Schema",
                "1 response + 1 request",
                "2",
                "0",
                "docs/qa/schemas/send-chat-message-*.schema.json",
            ],
        ],
    ))
    body.append(p(""))
    body.append(p("Resumen global agente (Fase 4): 2 lotes LLM descartados; 29 métodos nuevos aceptados (21 unit + 8 WebMvc, de los cuales 3 son contrato puro)."))
    body.append(p(""))

    body.append(heading("5. Esquema del endpoint principal", 2))
    body.append(p("Endpoint: POST /api/v1/assistant/chat"))
    body.append(p("Response: SendChatMessageResponse (JSON Schema draft 2020-12):"))
    body.append(p(SCHEMA_RESPONSE, mono=True))
    body.append(p(""))
    body.append(p("Request (resumen): { message: string (required), history?: ChatMessageDto[], context?: { agent: general|phases|users|evidence, ... } }"))
    body.append(p("Archivos completos: docs/qa/schemas/send-chat-message-request.schema.json y send-chat-message-response.schema.json"))
    body.append(p(""))

    body.append(heading("6. Corrida final en verde (sin modelo ni red)", 2))
    body.append(p("Condiciones: sin Ollama/Groq/Open WebUI; ChatCompletionPort y use cases mockeados; @WebMvcTest con SecurityConfig."))
    body.append(p(RUN_OUTPUT, mono=True))
    body.append(p(""))
    body.append(p("Fix producción aplicado: AssistantToolRegistry — perfil USERS solo para rol JD (baseline P0-1)."))
    body.append(p(""))

    body.append(heading("7. Criterio de completitud de la actividad", 2))
    body.append(bullet("Cobertura ANTES y DESPUÉS documentada: cumplido (43,6 % → 48,8 % línea)."))
    body.append(bullet("Al menos un test aceptado por capa: cumplido (unit 21 · integración 18 · contrato 3+schema)."))
    body.append(bullet("Suite MOD-ASSISTANT verde sin LLM/red: cumplido (99 run, 0 failures)."))
    body.append(p(""))

    body.append(heading("8. Pendiente / trabajo futuro (opcional)", 2))
    body.append(bullet("L5 OpenWebUiChatAdapterTest con WireMock (adapter HTTP LLM — 0 % cobertura)."))
    body.append(bullet("L6 AssistantChatContextFactoryTest (0 % cobertura)."))
    body.append(bullet("Ampliar AssistantToolExecutor (>23 % línea) para acercarse a meta >60 % en nodos P0."))
    body.append(bullet("Validador automático JSON Schema en CI (networknt/json-schema-validator)."))
    body.append(bullet("Suite global backend (fuera MOD-ASSISTANT): 3–6 fallos legacy documentados en baseline."))
    body.append(bullet("Actualizar tabla foto final en TEST-BASELINE-2026-09-06.md §9."))

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
