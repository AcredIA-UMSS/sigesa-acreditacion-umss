### ¿Cómo agilizar esto con Cursor?

Para no escribir todo esto a mano, puedes crear un atajo de automatización. Cuando vayas a iniciar un feature, simplemente abre Cursor Composer (Ctrl+I / Cmd+I) y dile:

> "Lee el caso de uso `FSD-UC-001` de la capa viva (`docs/product/FSD.md`) y el DTP actual. A partir de esa información, lléname la plantilla `FEATURE_DESIGN_DOC_TEMPLATE.md` y guárdala como `docs/design/DD-UC-001.md`. Recuerda respetar nuestra arquitectura hexagonal en Spring Boot."

El agente hará el trabajo pesado de formato, y tú solo tendrás que revisar y aprobar el diseño antes de pedirle que escriba el código real en Java.

<FollowUp label="¿Tienes las plantillas del FSD Vivo (LFSD) o de los Prompts?" query="¿Puedes compartir la plantilla PROMPT_TEMPLATE.md o la del FSD_TEMPLATE.md para integrarlas al sistema?"/>