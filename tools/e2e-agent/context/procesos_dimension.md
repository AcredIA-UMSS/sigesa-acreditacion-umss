# Sección: Procesos — estructura normativa (JD)

## Rutas

- Listado: `/procesos` — heading «Procesos de acreditación», tabla columna «Carrera»
- Detalle: `/procesos/{uuid}` — «Estructura del proceso», botón «Editar estructura normativa» (solo JD/TD + proceso ACTIVE)
- Editor: `/procesos/{uuid}/estructura` — «Agregar nueva dimensión (nivel 1)», campos Nombre/Orden/Descripción, «Agregar nivel 1», «Volver al detalle»

## Seed dev (referencia)

- Carrera: **Administración de Empresas** (ADM-EMP)
- Link fila: `getByRole('link', { name: 'Ver detalle del proceso Administración de Empresas' })`
- Proceso ACTIVE; UUID ejemplo seed: `8d38cabf-02f5-4d62-86e8-4aae588c4f9c`

## Dimensión de prueba

- Nombre sugerido en plan: `dimensionPrueba`
- Si ya existe por un run anterior: usar `dimensionPrueba-${Date.now()}` o documentar skip

## Login JD

- jd@umss.edu.bo / JefeDemo2026!
- Contraseña: `getByRole('textbox', { name: 'Contraseña' })`

## Patrón

- Login + navegación: `tests/tradicional/login.spec.ts`
