# Localizadores estables — login y evidencias

Contrato de persona para E2E. Preferir `getByRole` / `getByLabel`; `getByTestId` como ancla de región.

| Control | Rol / nombre accesible | `data-testid` | Specs |
| --- | --- | --- | --- |
| Página login | — | `login-page` | 1.1, 1.3 |
| Correo | `getByLabel('Correo Institucional')` | `login-email` | 1.1–2.1, 3.1 |
| Contraseña | `getByLabel('Contraseña')` | `login-password` | 1.1–2.1, 3.1 |
| Mostrar/ocultar | `getByRole('button', { name: 'Mostrar caracteres' })` | `login-toggle-password` | login.spec |
| Enviar login | `getByRole('button', { name: 'Iniciar sesión' })` (`aria-label` fijo si el botón muestra «Procesando…») | `login-submit` | 1.1–2.1 |
| Dashboard | — | `dashboard-page` | 1.1, 1.2, 3.1 |
| Nav evidencias [CC] | `getByRole('link', { name: 'Cargar evidencia' })` | `sidebar-nav-evidence` | 1.1, 2.1 |
| Página evidencias | — | `evidence-upload-page` | 1.1–2.1 |
| Título módulo | `getByRole('heading', { name: 'Cargar Evidencia' })` | `evidence-upload-heading` | 1.1, 1.2, 2.1 |
| Indicador | `getByLabel('Indicador')` | `evidence-indicator` | 1.1, 3.1 |
| Criterio | `getByLabel('Criterio')` | `evidence-criterion` | 1.1, 3.1 |
| Descripción | `getByLabel('Descripción')` | `evidence-description` | 1.1 |
| Archivo | `getByLabel('Archivo de evidencia')` | `evidence-file` | 1.1 |
| Subir | `getByRole('button', { name: 'Subir evidencia' })` | `evidence-submit` | 1.1 |

## Cambios de producto (2026-09-11)

- Toggle de contraseña: `aria-label` **Mostrar/Ocultar caracteres** (ya no incluye «Contraseña»; `getByLabel('Contraseña')` es único).
- Submit login: `aria-label="Iniciar sesión"`.
- Nav evidencias: `aria-label="Cargar evidencia"` (estable con sidebar colapsado).
- H1 evidencias: `data-testid="evidence-upload-heading"`.
- Campos: etiquetas «Descripción» y «Archivo de evidencia»; `aria-label` en el `input[type=file]`.
- Submit evidencia: `aria-label="Subir evidencia"`.
