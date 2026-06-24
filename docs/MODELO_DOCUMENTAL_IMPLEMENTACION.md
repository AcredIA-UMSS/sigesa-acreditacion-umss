# Modelo Documental y Transición a Implementación (AI-SDLC)

Este documento rige las reglas de comportamiento para el desarrollo del proyecto SIGESA con asistencia de IA.

## Reglas Inquebrantables
1. **El Baseline es Intocable:** La carpeta `docs/baseline/` contiene el DTI y FSD evaluados. Está estrictamente prohibido que agentes humanos o IA editen, eliminen o modifiquen archivos aquí.
2. **Cero Divergencia Silenciosa:** Si la implementación difiere del diseño original, el cambio se documenta PRIMERO en la capa viva (`docs/product/` y `docs/adr/`) antes de hacer *merge* del código.
3. **El DTP como Contrato Vivo:** El Documento Técnico del Producto (`docs/product/DTP.md`) es la única fuente de verdad técnica de lo que está actualmente en el código.
4. **Trazabilidad Total:** Todo código nuevo (PR/Commit) debe poder rastrearse hacia atrás en esta cadena:
   `Código → PR-IMPL-NNN → DD-UC-NNN → FSD-UC-NNN → DTP / PRD Vivo`

Cualquier agente de IA que opere en este repositorio debe detener sus operaciones si se le pide romper alguna de estas reglas.