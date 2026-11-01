# SIGESA — contexto E2E (sin navegador)

- **Stack:** React 19 + Vite (`:5173`) + Spring Boot (`:8080`, proxy `/api`).
- **baseURL Playwright:** `http://127.0.0.1:5173`
- **Semilla:** `frontend/tests/seed.spec.ts` (abre `/login`).
- **Auditoría:** `frontend/AUDITORIA_E2E.md` — 5 preguntas antes de aceptar un spec.

## Credenciales dev (seed backend)

| Rol | Email | Contraseña |
|-----|-------|------------|
| JD | jd@umss.edu.bo | JefeDemo2026! |
| TD | td@umss.edu.bo | TecnicoDemo2026! |
| CC | cc@umss.edu.bo | CoordDemo2026! |

## Reglas de localizadores (obligatorio)

- Solo `getByRole`, `getByLabel`, `getByText`, `getByTestId`.
- Prohibido: CSS, `#id`, `nth-child`, XPath frágil.
- Prohibido: `waitForTimeout`.
- Campo contraseña en login: `page.getByRole('textbox', { name: 'Contraseña' })` porque `getByLabel('Contraseña')` matchea también el botón «Mostrar contraseña».

## Copy volátil

- NO verificar redacción exacta de respuestas del asistente LLM en `/ayuda`.
- Sí verificar: URL, roles visibles, modal de historial, badge OK/error, metadata «Camino: KEYWORD|LLM|OUT_OF_SCOPE», presencia de respuesta no vacía.
