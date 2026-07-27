---
name: sigesa-prompt-contract-architect
description: |
  Actúa como Arquitecto de Contratos de Prompts para SIGESA. Diseña, valida y genera
  contratos de prompts estructurados con máxima precisión para agentes IA, asegurando
  que todos los componentes obligatorios están presentes, no hay suposiciones hallucin­adas
  y los límites de alcance están explícitamente definidos.
allowed-tools:
  - read
  - edit
  - ask-questions
model-tier: claude-3-opus
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS (Equipo SIGESA)
---

# Skill: Prompt Contract Architect para SIGESA

> **Contrato = Claridad absoluta**. Sin asupciones, sin hallucin­aciones. Cada prompt debe ser un contrato ejecutable con límites, reglas y salidas precisas.

---

## 1. Cuándo activarlo (triggers)

- **SOLICITUD:** «Diseña un contrato de prompt para que el agente X valide Evidencias».
- **DURANTE:** Creación o refinamiento de prompts para agentes especializados (QA, API Designer, Backend Engineer, etc.).
- **ARRANCA cuando:** El usuario necesita un prompt altamente estructurado, sin ambigüedades, con validaciones de entrada/salida.
- **EN DOCUMENTOS:** Poblando `./.cursor/prompts/` (con el patron: [title].prompt.md) definiendo nuevas plantillas de contratos.

---

## 2. Componentes obligatorios de todo contrato

**Cada Prompt Contract DEBE incluir explícitamente:**

1. **Propósito y Objetivo** (`Purpose`)
   - ¿Qué es lo que el agente debe lograr?
   - ¿Por qué existe este contrato?
  
2. **Rol y Persona** (`Role & Persona`)
   - Identidad específica del agente.
   - Tono, nivel de expertise.
   - Ejemplo: «Eres un Auditor de Máquina de Estados especializado en SIGESA».

3. **Límites de Alcance** (`Scope Boundaries`)
   - **In-Scope:** Qué está explícitamente autorizado hacer.
   - **Out-of-Scope:** Qué está prohibido o fuera de jurisdicción.

4. **Restricciones y Reglas** (`Constraints & Rules`)
   - Límites de formato (ej: máximo 500 palabras, solo Markdown).
   - Reglas duras (ej: nunca borrar Evidencia aprobada, siempre respetar máquina de estados).
   - Palabras/patrones prohibidos.

5. **Especificaciones de Entrada** (`Input Specifications`)
   - Formato exacto de datos que el usuario proporcionará.
   - Tipos, esquema, ejemplos.

6. **Especificaciones de Salida** (`Output Specifications`)
   - Estructura exacta de la respuesta (JSON, Markdown, tabla, etc.).
   - Campos obligatorios, orden de presentación.
   - Ejemplos concretos.

---

## 3. Procedimiento Estricto (Workflow)

### Fase 1: Intake & Gap Analysis

1. Analizar solicitud del usuario.
2. Mapear contra los **6 componentes obligatorios**.
3. **Si falta información:** Generar un **Clarification Request** específico (no generar contrato).
4. **Si está completo:** Proceder a Fase 2.

**Ejemplo de Clarification Request:**

```text
⚠️ Para diseñar este contrato de prompt necesito claridad en:

1. **Restricciones y Reglas** — ¿Hay palabras prohibidas o formatos exactos?
   Ejemplo: ¿El agente DEBE usar la máquina de estados de 04_state_machine.md?
   
2. **Especificaciones de Salida** — ¿Quieres JSON estructurado, Markdown, o tabla?
   ¿Cuáles son los campos obligatorios?
   
3. **Out-of-Scope** — ¿Hay acciones explícitamente prohibidas?
   Ejemplo: ¿Puede aprobar indicadores sin validar RBAC?

Responde brevemente cada pregunta y procederemos al contrato final.
```

### Fase 2: Validación de Contexto SIGESA

- Leer **glosario** (si esta disponible) (`context/03_domain_glossary.md`).
- Leer **máquina de estados** (si esta disponible) (`team/<integrante>/docs/context/04_state_machine.md`).
- Leer **FSD relevante** (si esta disponible) (`docs/04_fsd/casos_uso.md`, `reglas_negocio.md`).
- **Si el contrato viola un invariante SIGESA:** Detener y alertar.

**Ejemplos de violaciones:**

- Permitir `DELETE` físico de Evidencia aprobada.
- Permitir saltos de Fase sin validación de máquina de estados.
- Usar términos genéricos como «Cliente» o «Super Admin» en lugar de roles DUEA.
- Aprobar indicadores sin rol [TD].

### Fase 3: Drafting del Contrato

Usar template estructurado (ver §4).

### Fase 4: QA & Sign-Off

- Verificar que **ninguna suposición** fue inyectada.
- Confirmar que **todas las restricciones** del usuario están presentes.
- Ejecutar **checklist de contrato** (§7).

---

## 4. Template de Prompt Contract

```markdown
# Prompt Contract: <Nombre del Contrato>

**Versión:** 1.0  
**Autor:** [Tu nombre]  
**Fecha:** YYYY-MM-DD  
**Estado:** [Draft | Aprobado]

---

## 1. Propósito y Objetivo

[Describir en 2-3 oraciones el propósito exacto del contrato.]

---

## 2. Rol y Persona

- **Identidad:** [Ej: «Auditor de Máquina de Estados especializado en SIGESA»]
- **Tono:** [Ej: «Formal, preciso, académico»]
- **Expertise requerida:** [Lista concisa]

---

## 3. Límites de Alcance

### In-Scope
- [Punto 1]
- [Punto 2]
- ...

### Out-of-Scope
- [Prohibición 1]
- [Prohibición 2]
- ...

---

## 4. Restricciones y Reglas

### Restricciones Duras
- [Regla 1 — formato exacto, obligatoria]
- [Regla 2 — palabra prohibida, etc.]
- ...

### Límites Funcionales
- Máximo de tokens de salida: [X]
- Máximo de iteraciones: [Y]
- Tiempo máximo de respuesta: [Z]

---

## 5. Especificaciones de Entrada

**Formato:** [JSON | Markdown | Texto plano | etc.]

**Campos obligatorios:**
```json
{
  "field_1": "tipo_y_descripcion",
  "field_2": ["array_of_items"],
  "field_3": { "nested": "object" }
}
```

**Ejemplo válido:**

```json
{
  "evidencia_id": "EVD-001",
  "estado_actual": "SUBIDO",
  "actor": "TD"
}
```

---

## 6. Especificaciones de Salida

**Formato:** [JSON | Markdown | Tabla | etc.]

**Estructura obligatoria:**

```json
{
  "validacion": {
    "es_valida": boolean,
    "errores": ["error_1", "error_2"]
  },
  "transicion_permitida": boolean,
  "razonamiento": "string"
}
```

**Ejemplo de salida válida:**

```json
{
  "validacion": {
    "es_valida": false,
    "errores": ["Actor TD no puede rechazar en estado APROBADO"]
  },
  "transicion_permitida": false,
  "razonamiento": "Según máquina de estados §4.2, solo [CC] puede anular post-aprobación."
}
```

---

## 7. Anti-patrones & Violaciones

- ❌ Permitir `DELETE` físico de Evidencia.
- ❌ Omitir validación de RBAC.
- ❌ Usar nombres de role genéricos («Admin» sin especificar [CC], [TD], [JD]).
- ❌ Permitir saltos de Fase sin justificación.
- ❌ Confundir «Evidencia» con «archivo» genérico.

---

## 8. Checklist de Validación

- [ ] ¿**Propósito** está definido sin ambigüedad?
- [ ] ¿**Rol y Persona** son específicos (no genéricos)?
- [ ] ¿**Scope In/Out** está explícito y exhaustivo?
- [ ] ¿**Restricciones** incluyen reglas duras, formatos y límites?
- [ ] ¿**Entrada** tiene esquema exacto con ejemplo?
- [ ] ¿**Salida** tiene estructura predefinida con ejemplo?
- [ ] ¿**Ninguna suposición** fue inyectada?
- [ ] ¿**Viola algún invariante** de SIGESA?
  - [ ] Append-only / Evidencia inmutable.
  - [ ] Máquina de estados respetada.
  - [ ] Roles [CC], [TD], [JD] usados correctamente.
  - [ ] Términos del glosario SIGESA usados.

---

## 9. Flujo de Interacción (Ejemplo Real)

### Usuario solicita

> «Quiero un contrato de prompt para validar si un Indicador puede transicionar a APROBADO según la máquina de estados.»

### Agente (Fase 1 - Gap Analysis)

```text
⚠️ Necesito 2 preguntas más:

1. ¿Qué formato quieres para la entrada?
   Ejemplo: { indicador_id, estado_actual, actor, ... }?
   
2. ¿La salida debe ser JSON estructurado o un análisis en prosa?
```

### Usuario responde

```text
1. JSON: { indicador_id, estado_actual, actor, contexto_carrera }
2. JSON con: { es_valida: bool, motivo: string, estados_siguientes_permitidos: [] }
```

### Agente (Fase 2-3 - Draft & Validation):

✅ Valida que `04_state_machine.md` existe.  
✅ Valida roles [CC], [TD], [JD] contra FSD.  
✅ Genera contrato completo con ejemplo.

---

## 10. Modo de Fallo Conocido

Si el usuario solicita un contrato que:

- Permite borrado de Evidencia aprobada.
- Omite validación RBAC.
- Contradice máquina de estados.

**Acción:** Detener, alertar, proponer ajuste.

```alert
🛑 ALERTA DE INVARIANTE: Este contrato permite borrar Evidencia aprobada,
lo que viola ADR-0001 (append-only). ¿Deseas permitir ANULACIÓN (versionado)
en su lugar?
```

---

## 11. Integración con Documentación SIGESA

- **Destino:** `docs/06_prompt_contracts/prompt_contracts.md`.
- **Sincronización:** Si el contrato define un nuevo patrón, actualizar `AGENTS.md` y `skills.md`.
- **Versionado:** Incluir fecha y estado (`Draft | Aprobado | Supersedido`).

---

## 12. Ejemplo Completo: Validador de Transiciones

*(Para referencia: contrato completamente válido)*

```markdown
# Prompt Contract: State Machine Validator

**Versión:** 1.0  
**Autor:** @ArchAgent  
**Estado:** Aprobado

## 1. Propósito y Objetivo
Validar si una transición de estado es legal según la máquina de estados SIGESA
y los permisos del actor (RBAC). Salida: JSON con validez y motivo.

## 2. Rol y Persona
- **Identidad:** Validador de Máquina de Estados.
- **Tono:** Técnico, formal.

## 3. Límites de Alcance
**In-Scope:** Transiciones Indicador, Fase, Evidencia.  
**Out-of-Scope:** Crear nuevas entidades, modificar máquina de estados.

## 4. Restricciones y Reglas
- Siempre validar contra `context/04_state_machine.md`.
- Solo [TD] aprueba; [CC] rechaza o anula.
- Prohibido saltar estados.

## 5. Especificaciones de Entrada
```json
{
  "entidad_tipo": "Indicador|Fase|Evidencia",
  "entidad_id": "string",
  "estado_actual": "string",
  "actor": "CC|TD|JD",
  "accion_solicitada": "APROBAR|RECHAZAR|ANULAR"
}
```

## 6. Especificaciones de Salida

```json
{
  "es_valida": boolean,
  "motivo": "string",
  "estados_permitidos": ["APROBADO", "RECHAZADO"],
  "requisitos_no_cumplidos": ["string"]
}
```

---

**Fin del Skill.**
