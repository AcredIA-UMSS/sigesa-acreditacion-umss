# Contexto Tecnológico - Proyecto Universidad
- **Backend:** Java 21, Spring Boot 3.x/4.x, Maven.
- **Persistencia:** Spring Data JPA, Hibernate, Base de datos H2 (en memoria/archivo para desarrollo local).
- **Productividad:** Uso estricto de Lombok (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`).
- **Arquitectura:** Limpia por capas (Controller -> Service -> Repository).
- **Regla de Oro:** Nunca exponer Entidades JPA en los Controladores. Usar siempre DTOs para Request y Response.
- **Calidad:** Cobertura de pruebas unitarias mayor al 90% utilizando JaCoCo.
# Gobernanza Documental y Mecanismos de Bloqueo (AI-SDLC)

- **Protección del Baseline (CRÍTICO):** Tienes estrictamente PROHIBIDO modificar, eliminar o renombrar cualquier archivo dentro del directorio `docs/baseline/`. Ese es un registro histórico congelado.
- **Evolución en la Capa Viva:** Cualquier actualización de alcance, reglas de negocio o diseño técnico derivada de la codificación debe reflejarse ÚNICAMENTE en `docs/product/` (PRD vivo, FSD vivo, DTP).
- **Trazabilidad Obligatoria:** Antes de escribir código para un nuevo feature, debes verificar la existencia de su documento de diseño en `docs/design/DD-UC-*.md` y seguir sus especificaciones.

# Flujo de Trabajo y Toma de Decisiones

- **Cero Divergencia Silenciosa:** Si encuentras un bloqueo técnico que obliga a desviar la implementación de lo especificado en el diseño original, DETENTE. Informa del problema y solicita la creación de un ADR (Architectural Decision Record) en la carpeta `docs/adr/`.
- **Sincronización del DTP:** Si introduces una nueva dependencia, cambias un contrato de API (DTO) o modificas el modelo de datos, debes sugerir inmediatamente la actualización del Documento Técnico del Producto (`docs/product/DTP.md`).
- **Commits y PRs:** Todo código generado debe estar respaldado por un prompt documentado en `docs/prompts/impl/` y registrado en `docs/PROMPT_MAPPING.md`.

# Reglas de Comportamiento del Agente

- **Respuestas Concisas:** Ve directo al grano. Evita explicaciones redundantes sobre qué es Java o qué es Spring Boot, asume que el usuario es un Tech Lead avanzado.
- **Código Listo para Producción:** No generes funciones vacías, placeholders (como `// TODO: implementar lógica`) o código a medias, a menos que se te pida explícitamente un esqueleto.
- **Restricción de Refactorización:** No refactorices archivos enteros si no se te ha pedido. Limítate al "scope" (alcance) de la instrucción actual.