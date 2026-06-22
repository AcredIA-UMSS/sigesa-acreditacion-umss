---
id: FSD-UC-016
nombre: Portal público
estado: Pendiente
release: v1.1
actor_principal: "[P] (visitante anónimo)"
trazabilidad_prd: PRD-US-016, PRD-US-020
modulo: MOD-PUBLIC
reglas: FSD-BR-10
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-016 — Portal público

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-019, 026 · PRD-US-016, 020 · NFR-005 |
| **Pantalla** | `/public/status` |
| **API** | `GET /public/programs/{slug}` |
| **Invariante** | Cero borradores u observaciones internas visibles |

## Flujo principal

1. Visitante consulta `GET /public/programs/{slug}`.
2. Sistema retorna solo registros con `published=true`.
3. Opcional: descarga certificado si [JD] publicó (US-020).

## Excepciones y flujos alternos

| Condición | Comportamiento |
|-----------|----------------|
| Slug inexistente o no publicado | `404` genérico |
| Certificado no publicado | Descarga denegada |

## Postcondiciones

Información pública coherente con lo autorizado por [JD].

## Diagramas

- [Portal público](../diagramas/AYL-SEQ-008-portal-publico.mmd)
- [Consulta portal](../diagramas/MAR-SEQ-008-portal-publico-consulta.mmd)
- [Certificados](../diagramas/AYL-SEQ-009-certificados.mmd)

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
