---
id: PR-IMPL-004
feature_asociado: DD-UC-004
fsd_uc:
  - "FSD-UC-004"
fecha: "2026-07-10"
version: "1.0"
estado: Borrador
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
skill_origen: sigesa-prompt-contract-architect
---

# Prompt de Implementación `PR-IMPL-004`

## 1. Propósito y Objetivo
Generar el código fuente en Java para el módulo `MOD-EVIDENCE` que permita cargar evidencias versionadas para un indicador y criterio específicos. El código debe garantizar las reglas de negocio **FSD-BR-01** (evidencia siempre ligada a indicador/criterio), **FSD-BR-03** (solo CC carga evidencias), y **FSD-BR-09** (CC sólo accede y carga a su propia carrera) utilizando una estructura estricta de puertos y adaptadores (Arquitectura Hexagonal).

## 2. Rol y Persona
Actúa como un **Desarrollador Backend Senior experto en SIGESA**. Escribes código para Java 21 usando Spring Boot 3.x/4.x. Dominas la **Arquitectura Hexagonal**, el diseño guiado por el dominio (DDD), la seguridad con JWT y la creación de código limpio, transaccional y testeable.

## 3. Límites y Restricciones
- **Hexagonal puro**: La capa de dominio y aplicación no debe tener anotaciones de Spring (como `@Service`, `@Autowired`, `@Component`) ni imports de JPA.
- **DTOs**: Usa `records` de Java 21 para transferir datos en adaptadores de entrada.
- **Evitar entities en API**: Está prohibido exponer clases de entidad JPA (`@Entity`) en los controladores REST o DTOs. Mapea hacia/desde modelos de dominio.
- **Almacenamiento Local**: Para guardar físicamente los archivos subidos, crea un adaptador `LocalFileStorageAdapter` que implemente un puerto `FileStoragePort`, guardando el archivo en un directorio del workspace y retornando su clave de almacenamiento e ID SHA-256.

## 4. Contexto de Entrada
- **Diseño a seguir**: Sigue la especificación de contratos y puertos del archivo [DD-UC-004](file:///mnt/sda2/informatica/maestria/sigesa-docs/app/sigesa-backend/docs/design/DD-UC-004.md).
- **Entidades**: `Evidence`, `EvidenceVersion`.
- **Casos de Uso Core**: `UploadEvidenceUseCase` implementado por `UploadEvidenceService`.
- **API Endpoint**: `POST /api/v1/indicators/{indicatorId}/evidences` (MultipartForm).

## 5. Salida Esperada
Proporciona el código Java completo de los siguientes componentes, organizados por capas:
1. **Dominio**: Modelos `Evidence`, `EvidenceVersion` y excepciones personalizadas.
2. **Puertos**: Inbound (`UploadEvidenceUseCase`) y Outbound (`EvidenceRepositoryPort`, `FileStoragePort`, `IndicatorStateHistoryPort`).
3. **Aplicación**: Servicio `UploadEvidenceService` implementando el caso de uso y validando las reglas de negocio (especialmente el aislamiento de programa/carrera `FSD-BR-09`).
4. **Adaptadores (Infra)**: `EvidenceUploadController`, `EvidenceJpaAdapter`, `LocalFileStorageAdapter`.
5. **Configuración**: Configurar el Bean del servicio en una clase de configuración del módulo.
6. **Tests unitarios e integración**: Esbozos y aserciones para validar el caso de uso y el controlador.
