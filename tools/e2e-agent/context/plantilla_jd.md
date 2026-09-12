# Sección: Plantillas normativas — JD

## Rutas

- Listado: `/admin/plantillas` — H1 «Gestión de plantillas», link «Nueva plantilla»
- Alta: `/admin/plantillas/nueva` — título página «Nueva plantilla», sección «Metadatos de la plantilla»
- Campos: `getByLabel('Nombre')`, `getByLabel('Tipo normativo').selectOption('CEUB')` (es `<select>`, no radio), `getByLabel('Descripción')`
- Badge estado: texto UI **«Borrador»** (no `DRAFT`)
- «Nueva plantilla»: `getByRole('link', { name: 'Nueva plantilla' })`
- Acciones: botón «Guardar» (crea id y redirige a `/admin/plantillas/{id}`)

## Login JD

- jd@umss.edu.bo / JefeDemo2026!

## Datos únicos

- Nombre: `Plantilla E2E ${Date.now()}` — evita colisión en listado

## Verificación

- Tras guardar: URL con UUID, badge borrador (DRAFT / Borrador)
- Volver a listado y buscar fila con el nombre creado (`getByRole('row')` + texto)

## Fuera de alcance E2E rápido

- Publicar / jerarquía N1→N3 completa (panel inferior) — solo si el plan lo pide explícitamente
