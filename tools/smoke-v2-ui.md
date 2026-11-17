# Smoke V15 + UI v2

## 1. Verificar datos (read-only)

```bash
docker exec -i sigesa-postgres psql -U sigesa_user -d sigesa < tools/verify-v15-migration.sql
```

**Esperado tras V15 completo:**

| Entidad | Debe coincidir con |
|---------|-------------------|
| `level1_nodes` | `phases` (mismos UUID) |
| `indicators` | `subphases` |
| `level2_nodes` | ≥ 1 por cada `level1` (placeholder "General") |
| `template_level1_nodes` | `template_phases` |

## 2. Aplicar V15 (si level2/indicators = 0)

En dev Docker, Flyway está **desactivado** (`application-dev.yaml`). Ejecutar manualmente:

```bash
docker exec -i sigesa-postgres psql -U sigesa_user -d sigesa \
  < backend/src/main/resources/db/migration/V15__normative_hierarchy_data_migration.sql
```

> **Nota:** V15 corregido usa `gen_random_uuid()` en nodos L2/L3 placeholder (tablas creadas por Hibernate no tienen DEFAULT).

Volver a ejecutar el script de verificación.

## 3. Smoke API

Credenciales demo: ver `README.md` (JD/TD).

```bash
# Login → guardar token en variable local (no commitear)
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"td@umss.edu.bo","password":"TecnicoDemo2026!"}' \
  | jq -r '.accessToken')

# Listado con contadores v2
curl -s http://localhost:8080/api/v1/processes -H "Authorization: Bearer $TOKEN" \
  | jq '.[] | {careerCode, level1Count, indicatorCount}'

# Detalle con árbol
PID=<uuid-proceso-active>
curl -s "http://localhost:8080/api/v1/processes/$PID" -H "Authorization: Bearer $TOKEN" \
  | jq '{evaluatorModel, level1: (.level1Nodes | length), phases: (.phases | length)}'

curl -s "http://localhost:8080/api/v1/processes/$PID/level1-nodes" -H "Authorization: Bearer $TOKEN" \
  | jq '.[0] | {name, label, level2: (.level2Nodes | length)}'
```

**Esperado:** `indicatorCount > 0`, `level1Nodes[].level2Nodes[].level3Nodes[].indicators` poblado.

## 4. Smoke UI (http://localhost:3000)

1. Login como **TD** o **JD**.
2. Ir a **Procesos** → abrir proceso ACTIVE (ej. INF-SIS).
3. Sección **Estructura del proceso**:
   - Texto menciona jerarquía normativa (no solo fases).
   - Acordeones N1 → N2 → N3 → indicadores con código y ponderación.
4. **Editar estructura** → tab **Jerarquía normativa v2** → CRUD visible.
5. Legacy: tab **Fases / subfases** sigue disponible.

## 5. Estado conocido en dev Docker

- Flyway historial puede quedar en V3 aunque tablas v2 existan (Hibernate + migración manual).
- Tras V15 manual, registrar en `flyway_schema_history` solo si habilitas Flyway en dev.
