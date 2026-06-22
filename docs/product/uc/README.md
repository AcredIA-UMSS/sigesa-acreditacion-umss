# Casos de uso atomizados (LFSD)

Cada archivo `FSD-UC-NNN.md` es la **única fuente funcional viva** para implementar ese caso de uso.

| Archivo | Índice maestro |
|---------|----------------|
| [`../FSD.md`](../FSD.md) | LFSD ⚡ — índice y hard constraints |

**Estructura estándar por UC:**

1. Frontmatter YAML (id, estado, trazabilidad)
2. Contexto
3. Flujo principal
4. Excepciones y flujos alternos
5. Postcondiciones
6. Diagramas (enlaces a `../diagramas/`)
7. Escenarios Gherkin

**Baseline congelado:** no editar `docs/baseline/`. Divergencias → actualizar aquí + `docs/adr/` antes de merge.
