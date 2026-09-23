# Sección: Responsable del proceso — JD asigna CC

## Rutas

- Listado: `/procesos`
- Detalle: `/procesos/{uuid}` — sección H2 «Responsable del proceso», texto «Sin responsable asignado» o datos del CC
- Botones (solo JD + proceso ACTIVE): «Asignar responsable» | «Cambiar responsable», «Quitar»
- Modal: dialog «Asignar responsable», select label «Coordinador responsable», «Confirmar asignación»

## Regla de negocio (tests)

- Candidatos = CC ACTIVE cuya carrera coincide con la del proceso
- Seed actual: `cc@umss.edu.bo` → **Ingeniería de Sistemas**; `cc2@umss.edu.bo` → Ingeniería Civil
- Proceso **Administración de Empresas** (UUID MCP `8d38cabf-02f5-4d62-86e8-4aae588c4f9c`) puede **no tener CC elegibles** en seed vanilla → el test debe:
  - usar proceso **Ingeniería de Sistemas** (`950e8400-e29b-41d4-a716-446655440020`) **o**
  - `test.skip()` documentado si el select solo tiene «Seleccione un coordinador» sin opciones reales

## Login JD

- jd@umss.edu.bo / JefeDemo2026!
- Post-login: `/admin/users`

## Select en modal

- `page.getByLabel('Coordinador responsable')` → `.selectOption({ index: 1 })` o por label con email `@umss.edu.bo`

## Verificación post-asignación

- Email o nombre del CC visible bajo «Responsable del proceso»
- Modal no visible (`getByRole('dialog', { name: 'Asignar responsable' })` hidden)
