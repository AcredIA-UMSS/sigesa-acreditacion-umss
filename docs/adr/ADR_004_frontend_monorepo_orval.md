# ADR-004: Monorepositorio y Autogeneración de API Frontend

## Contexto
El proyecto requiere sincronización perfecta entre los contratos del backend (Spring Boot) y el frontend (React 19). La IA y los desarrolladores perdían tiempo escribiendo interfaces y llamadas HTTP manuales, propensas a errores de tipado.

## Decisión
1. Adoptar arquitectura de Monorepositorio (`/backend` y `/frontend`).
2. Utilizar **Orval** en el frontend para autogenerar hooks de **React Query** y tipos de **TypeScript** a partir del OpenAPI (`/v3/api-docs`) del backend.
3. Prohibir estrictamente el uso manual de `fetch` o `axios` para consumo de APIs internas.

## Consecuencias
- **Positivas:** Tipado estricto end-to-end, eliminación de código repetitivo, la IA (Cursor) tiene contexto completo del proyecto.
- **Negativas:** El backend debe estar corriendo localmente antes de poder generar el código del frontend.