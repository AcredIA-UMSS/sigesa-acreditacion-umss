# Plantilla de hallazgo — Seguridad IA (SIGESA / laboratorio)

**ID:** `AI-SEC-___`  
**Estado:** Abierto | Mitigado | Aceptado  
**Fecha:** YYYY-MM-DD  
**Producto / componente:** SIGESA — Asistente GenAI (`POST /api/v1/assistant/chat`)

---

## Resumen

(1–2 párrafos: qué falló, severidad, categoría OWASP LLM.)

## Ataque exacto realizado

- **ID catálogo:** 
- **Categoría taxonomía:** 
- **Mensaje / payload:** 

## Precondiciones

- Backend accesible, asistente habilitado, LLM configurado, usuario demo con rol …

## Pasos reproducibles

1. 
2. 
3. 

## Resultado obtenido

(HTTP, fragmento de `reply`, tools en `steps`, conteo de éxito del atacante si aplica.)

## Impacto

(Confidencialidad / integridad / disponibilidad / cumplimiento institucional.)

## Categoría OWASP

(ej. LLM01 Prompt Injection, LLM02 Sensitive Information Disclosure, LLM06 Excessive Agency.)

## Evidencia

- Log `./run.sh probar-lab --trials 3 --report reports/...json`
- Captura o snippet (sin secretos reales de producción)

## Mitigación

| Tipo | Descripción | Ubicación en código |
|------|-------------|------------------------|
| Código | | |
| Proceso | Revisión de catálogo Red Team, CI Maven | |

## Verificación post-mitigación

(Cómo comprobar que el ataque ya no tiene éxito o queda acotado.)
