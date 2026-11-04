---
id: FSD-UC-016
nombre: Portal público
estado: Implementado
release: v1.1
actor_principal: "[P] (visitante anónimo)"
trazabilidad_prd: PRD-US-016, PRD-US-020
modulo: MOD-PUBLIC
reglas: FSD-BR-10
design_doc: DD-UC-016
pr_impl: PR-IMPL-050
ultima_actualizacion: "2026-11-04"
---

# FSD-UC-016 — Portal público

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-019, 026 · PRD-US-016, 020 · NFR-005 |
| **Pantalla** | `/public/status` |
| **API** | Prefijo vivo **`/api/v1/public`** (el FSD original decía `/public/programs/{slug}` sin `/api/v1`) |
| **Invariante** | Cero borradores u observaciones internas visibles |

## Adición al UC (2026-11-04)

El Gherkin «busca por nombre de carrera» no tenía endpoint. Se añade **listado publicado-only** (nombre + slug). La UI filtra por nombre en cliente. **No** hay search server-side.

| ID | HTTP | Contrato |
|----|------|----------|
| API-PUB-01 | `GET /api/v1/public/programs/{slug}` | Estado vigente si `published=true`; si no, **404 genérico** |
| API-PUB-02 | `GET /api/v1/public/programs` | Solo `{slug,name}` de publicadas |
| API-PUB-03 | `GET /api/v1/public/programs/{slug}/certificate` | PDF si certificado publicado; **404 genérico** si no. Sin URL de archivo |

404 genérico: mismo envelope `NOT_FOUND` / «El recurso solicitado no existe.» — no revela si la carrera existe.

Slug público = `programs.code` en minúsculas (`INF-SIS` → `inf-sis`).

## Flujo principal

1. Visitante abre `/public/status` y obtiene `GET /api/v1/public/programs`.
2. Filtra por nombre en cliente y consulta `GET /api/v1/public/programs/{slug}`.
3. Sistema retorna solo registros con `published=true`.
4. Opcional: `GET .../{slug}/certificate` si [JD] marcó el certificado como Publicado (US-020).

## Excepciones y flujos alternos

| Condición | Comportamiento |
|-----------|----------------|
| Slug inexistente o no publicado | `404` genérico |
| Certificado no publicado | `404` genérico (misma respuesta) |

## Postcondiciones

Información pública coherente con lo autorizado por [JD].

## Diagramas

- [Portal público](../diagramas/AYL-SEQ-008-portal-publico.mmd)
- [Consulta portal](../diagramas/MAR-SEQ-008-portal-publico-consulta.mmd)
- [Certificados](../diagramas/AYL-SEQ-009-certificados.mmd)

> Los diagramas aún muestran rutas legacy `/publico/carreras` y emisión [JD] `POST /certificados`. La implementación sigue esta ficha y `api_contracts.md`.

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-016 @FSD-UC-016 @TC-PUB
Característica: Portal público de transparencia

  Escenario: Portal sin borradores
    Dado contenido no publicado por [JD]
    Cuando un visitante anónimo consulta el portal
    Entonces no ve borradores ni observaciones internas
    Y solo información con flag Publicado

  Escenario: Consulta de estado publicado
    Dado una carrera con estado publicado oficialmente
    Cuando el visitante busca por nombre de carrera
    Entonces ve el estado de acreditación vigente

@PRD-US-020 @FSD-UC-016 @TC-PUB2
  Escenario: Descarga solo si publicado
    Dado un certificado marcado como Publicado por [JD]
    Cuando un visitante solicita descarga
    Entonces obtiene el PDF oficial con metadatos de vigencia
    Y no puede descargar borradores
```
