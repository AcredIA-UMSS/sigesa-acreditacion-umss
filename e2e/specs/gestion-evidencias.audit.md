# Auditoría del plan — acceso a gestión de evidencias

**Alcance pedido:** usuario SIGESA inicia sesión y llega al módulo donde se administran evidencias (`/evidencias/cargar`).  
**Fuentes:** FSD-UC-004, `CcOnlyRoute`, `Sidebar` (`sidebar-nav-evidence`), testids frontend.  
**Fecha:** 2026-09-11.

## Resumen

| Decisión | Cantidad |
| --- | ---: |
| Propuestos | 15 |
| Aceptados | 4 |
| Corregidos | 1 |
| Descartados | 10 |

## Auditoría por caso

| ID | Caso propuesto | Decisión | Motivo |
| --- | --- | --- | --- |
| P01 | [CC] login + sidebar CARGAR EVIDENCIA llega a `/evidencias/cargar` | **Aceptado** → 1.1 | Flujo pedido: sesión y llegada al módulo. Anclas `login-*` + `sidebar-nav-evidence` + `evidence-upload-page`. |
| P02 | [CC] autenticado abre `/evidencias/cargar` por URL | **Aceptado** → 1.2 | Verifica el módulo, no solo el ítem de menú. |
| P03 | Anónimo abre `/evidencias/cargar` y vuelve a login | **Aceptado** → 1.3 | `ProtectedRoute`; el módulo no es público. |
| P04 | [JD] no ve el nav y `CcOnlyRoute` bloquea la URL | **Aceptado** → 2.1 | FSD-BR-09 / actor UC-004 = [CC]. Sin esto el “llegué al módulo” es falso para otros roles. |
| P05 | Verificar redacción del modelo (copy «pasará a SUBIDO», título copiloto, nombre LLM) | **Corregido** → 3.1 | No testear copy de UI/LLM. Verificar **fuente** (`GET /indicators/uploadable`), **estado** (`PENDIENTE`/`OBSERVADO` en el select) o **ticket** TC-04 / FSD-UC-004. |
| P06 | [EE] y [TD] también bloqueados | **Descartado** | Cubierto por P04 (mismo `CcOnlyRoute`). TD no tiene el nav CC; duplicar no aporta. |
| P07 | Carga multipart exitosa v1 + hash | **Descartado** | Fuera de alcance: persistencia UC-004, no “llegar al módulo”. |
| P08 | Validación sin indicador/archivo (422/mensaje) | **Descartado** | Excepción de carga, no de acceso. |
| P09 | Barra de progreso archivo > 5 MB | **Descartado** | US-025 / TC-04b; no es navegación. |
| P10 | Slot de carga en subfase del árbol de proceso | **Descartado** | Entrada secundaria UC-019/022; el módulo pedido es `/evidencias/cargar`. |
| P11 | Enviar mensaje al copiloto documental | **Descartado** | FSD-UC-024, no acceso al módulo de administración. |
| P12 | Campana de notificaciones / olvidé contraseña | **Descartado** | Controles decorativos o no implementados. |
| P13 | Screenshot de marca / tipografía del H1 | **Descartado** | Visual no funcional; solapa el caso de copy (P05). |
| P14 | Recarga F5 conserva sesión en el módulo | **Descartado** | Persistencia JWT; no pedido. Puede un follow-up AUTH. |
| P15 | Login [CC] con correo no `@umss.edu.bo` | **Descartado** | FSD-UC-001 / BR-12; no es evidencia. |

## Corrección P05 (detalle)

| | |
| --- | --- |
| **Propuesto** | Assert del texto «El indicador pasará a estado SUBIDO» o del label del modelo del copiloto. |
| **Corregido** | 3.1 `uploadable-indicators-show-state-and-source` |
| **Qué se verifica** | Fuente HTTP `GET /api/v1/indicators/uploadable`; estado en opciones del select; criterio auto-asignado; trazabilidad ticket **TC-04** / **FSD-UC-004**. |
| **Qué no se verifica** | Wording del hero, nombre del LLM, historial de tools del copiloto. |

## Relación con specs existentes

`e2e/navigation.spec.ts` → `carga de evidencia expone el formulario UC-004` cubre parte de 1.1 **sin** el login por formulario (usa `seedSession`). El Planner mantiene 1.1 con login real para el alcance “quiero iniciar sesión y acceder”.
