# Contexto Tecnológico - Proyecto Universidad

## Backend
- **Stack:** Java 21, Spring Boot 4.x, Maven.
- **Persistencia:** Spring Data JPA, Hibernate, H2 (memoria/archivo).
- **Productividad:** Uso estricto de Lombok (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`).
- **Arquitectura:** Limpia por capas (Controller -> Service -> Repository).
- **Regla de Oro:** Nunca exponer Entidades JPA en los Controladores. Usar siempre DTOs para Request y Response.
- **Calidad:** Cobertura de pruebas unitarias > 90% (JaCoCo).

## Frontend
- **Stack:** React 19, TypeScript estricto, Vite.
- **Linting & Calidad:** OxLint (Zero-config, fast linting). Todo el código generado debe pasar sin advertencias.
- **Gestión de API:** Cliente autogenerado vía OpenAPI (ej. Orval). PROHIBIDO escribir llamadas `fetch` o `axios` manuales.
- **Arquitectura UI:** Separación estricta entre capa de presentación (Componentes UI puros) y capa de lógica (Hooks / Gestores de estado).

# Gobernanza Documental y Mecanismos de Bloqueo (AI-SDLC)
- **Protección del Baseline (CRÍTICO):** Tienes estrictamente PROHIBIDO modificar, eliminar o renombrar cualquier archivo dentro del directorio `docs/baseline/`. Ese es un registro histórico congelado.
- **Evolución en la Capa Viva:** Cualquier actualización de alcance, reglas de negocio o diseño técnico derivada de la codificación debe reflejarse ÚNICAMENTE en `docs/product/` (PRD vivo, FSD vivo, DTP).
- **Trazabilidad Obligatoria:** Antes de escribir código para un nuevo feature, debes verificar la existencia de su documento de diseño en `docs/design/DD-UC-*.md` y seguir sus especificaciones.

# Flujo de Trabajo y Toma de Decisiones
- **Cero Divergencia Silenciosa:** Si encuentras un bloqueo técnico que obliga a desviar la implementación de lo especificado, DETENTE. Informa del problema y solicita la creación de un ADR (`docs/adr/`).
- **Sincronización del DTP:** Si introduces una nueva dependencia (npm/maven), cambias un contrato de API (DTO) o modificas el modelo de datos, sugiere inmediatamente la actualización del `docs/product/DTP.md`.
- **Commits y PRs:** Todo código generado debe estar respaldado por un prompt documentado en `docs/prompts/impl/` y registrado en `docs/sprints/sprint_<N>/PROMPT_MAPPING.md` (vía `@save-prompt-mapping`).
- **Registro PM obligatorio:** Al finalizar cualquier prompt de implementación (`PR-IMPL-*`) o tarea de código, ejecutar `@save-prompt-mapping` **antes** de considerar la tarea cerrada.

# Reglas de Comportamiento del Agente
- **Respuestas Concisas:** Ve directo al grano. Asume que el usuario es un Tech Lead avanzado.
- **Código Listo para Producción:** No generes funciones vacías, placeholders (`// TODO: implementar`) o código a medias, salvo que se pida un esqueleto.
- **Restricción de Refactorización:** No refactorices archivos enteros si no se te ha pedido. Limítate al alcance ("scope") actual.
- **TypeScript Estricto:** Tipado fuerte obligatorio. PROHIBIDO el uso de `any`. Utiliza `types` o `interfaces` explícitas para las props y el estado.
- **Aislamiento de Monorepo:** Nunca mezcles imports entre los directorios `/backend` y `/frontend`.