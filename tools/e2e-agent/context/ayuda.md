# Sección: Asistente `/ayuda` (agent=general)

## Precondición

Requiere sesión autenticada. Flujo típico: login JD → `page.goto('/ayuda')`.

## Pantalla

- URL: `/ayuda`
- Header: «SIGESA · Ayuda», heading «Asistente virtual»
- Textarea consulta: `getByRole('textbox', { name: /Escriba su consulta/ })`
- Botón «Enviar» (deshabilitado si consulta vacía)
- Link «Historial de acciones (N)»
- Botón «Limpiar»
- Paneles laterales (lg): «Escenarios demo», «Capacidades»

## Modal de trazabilidad

Al enviar un mensaje se abre automáticamente `role="dialog"` con heading «Historial de acciones del asistente».
Pasos típicos: «Mensaje enviado al backend (/assistant/chat)», «Respuesta formateada entregada al chat».
Badge final «OK» o error. Cerrar con botón «Cerrar».

## Metadata en conversación

Línea «Camino: KEYWORD | LLM | OUT_OF_SCOPE» visible en el main (no assert de texto LLM).

## Casos típicos

- Pregunta funciones del usuario → respuesta informativa + Camino KEYWORD o meta
- Consulta vacía → Enviar deshabilitado
- Pregunta fuera de alcance → OUT_OF_SCOPE, sin crash
- Keyword «Lista las fases de…» → Camino KEYWORD
- Reabrir historial tras cerrar modal
- Limpiar conversación
- CC vs JD (roles distintos en capacidades)

## Referencia manual

Patrón generado auditado: `frontend/tests/agente/ayuda-funciones-usuario-jd.spec.ts`
