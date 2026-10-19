# FEAT-06: Bandeja Centralizada de Auditoría y Dictamen del Técnico DUEA

- **ID Issue:** [#55](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/55)
- **Trazabilidad Funcional:** `FSD-UC-012` · Task Spec Kit `T-007` · `PRD-US-014`
- **Módulo:** `MOD-DASH` / `MOD-EVIDENCE`
- **Release:** 8.1.0 / v2.1
- **Responsable / Asignado:** Alexander Alvarez (`Alex553-bot`)

---

## 1. Contexto y Problema de Negocio (Pain Point)

Durante la etapa de recolección (`COLLECTION`, E2) y avance hacia sistematización (E3), el Técnico DUEA (`[TD]`) debe auditar cientos de indicadores con sus respectivas evidencias.

### Dolores de los Actores:
- **Técnico DUEA (`[TD]`):** Sufre de sobrecarga operativa severa al tener que navegar manualmente por cada rama del árbol normativo (Nivel 1 → Nivel 2 → Nivel 3 → Indicador) para encontrar qué evidencias han sido cargadas en estado `SUBIDO` o subsanadas en `SUBSANADO`. Esta fragmentación genera retrasos de hasta 20+ horas al mes por evaluador y bloquea las compuertas de avance metodológico (*Stage Gates*).

---

## 2. Requisitos de Auditoría Técnica (12-Factor & Concurrencia)

| Dimensión | Factor / Regla | Impacto y Solución Arquitectónica |
|---|---|---|
| **Concurrencia** | `CON-03` | **Protección del Pool HikariCP (Ley de Little):** La bandeja técnica consolidada ejecutará consultas de agregación sobre miles de registros. Se deben añadir índices compuestos en base de datos (`program_id`, `level1_id`, `status`) para garantizar que la adquisición y consulta libere la conexión en $W_{db} < 30\text{ ms}$, evitando agotar el pool de 10 conexiones ante múltiples técnicos concurrentes. |
| **Concurrencia** | `CON-02` | **Paginación Estricta:** Prohibido retornar listas ilimitadas que saturen la memoria heap de Tomcat o demanden bloqueos de lectura extendidos. Uso de Spring `Pageable` con límites máximos forzados (e.g. `size <= 50`). |
| **Concurrencia** | `CON-01` | **Transiciones y Dictamen Atómico:** La aprobación (`UC-009`) y la observación (`UC-008`) deben emplear control de concurrencia optimista (`@Version` en la entidad) o locking a nivel de fila para prevenir inconsistencias si dos técnicos auditan el mismo indicador en simultáneo. |
| **Concurrencia** | `CON-04` | **Resiliencia de Cliente (Frontend):** Las llamadas desde la interfaz a la API deben incorporar `AbortSignal.timeout(10000)` para cancelar peticiones rezagadas y evitar bloqueos visuales (*frozen UI*). |
| **12-Factor** | `F12-03` | **Procesos sin Estado (Stateless):** Los filtros de búsqueda y estados de paginación se transfieren íntegramente por query params en la URL (`/technician/inbox?page=0&status=SUBIDO&programId=...`), sin almacenar estado de sesión en memoria del servidor. |

---

## 3. Alcance de Implementación

### Backend (Spring Boot 4.x / Java 21)
1. **Endpoint REST:** `GET /api/v1/dashboards/technician/inbox`
   - Parámetros: `programId` (UUID, opcional), `level1Id` (UUID, opcional), `status` (Enum, opcional: `SUBIDO`, `SUBSANADO`, `OBSERVADO`), `page`, `size`.
   - Seguridad: Restringido a roles con privilegio técnico (`[TD]`, `[JD]`).
2. **Consultas Optimizadas:**
   - DTO de proyección liviano conteniendo: ID de indicador, código, nombre, ruta jerárquica (N1 → N2 → N3), metadatos de la evidencia activa (folio, hash, fecha de carga), y conteo de observaciones abiertas.
   - Transacción explícita `@Transactional(readOnly = true)`.

### Frontend (React 19 / TypeScript / Vite)
1. **Pantalla `/technician/inbox`:**
   - Barra de filtros superiores con selectores de carrera, dimensión y estado.
   - Tabla responsiva con paginación y ordenamiento.
   - Acciones integradas en ≤ 2 clics:
     - Botón **Ver Detalle / Evidencia**: Previsualización del archivo o metadatos.
     - Botón **Aprobar**: Modal de confirmación para disparar `POST /indicators/{id}/approve` (`UC-009`).
     - Botón **Observar**: Modal con campo de descripción, plazo y fundamentación técnica para disparar `POST /indicators/{id}/reject` (`UC-008`).

### Fuera de Alcance
- Modificación del modelo de datos de programas o usuarios.
- Carga de archivos (tarea exclusiva de `[CC]`).

---

## 4. Criterios de Aceptación

- [ ] El técnico puede filtrar y visualizar todas las evidencias pendientes de su ámbito en menos de 2 minutos.
- [ ] Tiempo de respuesta P95 del endpoint $\le 1.5\text{ s}$ bajo carga concurrente (10 rps).
- [ ] La aprobación u observación se ejecuta directamente desde la bandeja sin perder los filtros activos ni recargar la página completa.
- [ ] Manejo de abortos por timeout en frontend ante demoras de red.
- [ ] Cobertura de pruebas unitarias (backend y frontend) > 90%.
