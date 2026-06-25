# Skill: Generar Feature Frontend (@generate-frontend-feature)

**Descripción:** Utiliza este skill para crear nuevos componentes o páginas en React que consuman la API del backend.

**Instrucciones para el Agente:**
1. **Verificación de API:** Antes de escribir código UI, verifica si existen los hooks necesarios en `frontend/src/api/`. Si el backend fue modificado recientemente, pide al usuario que ejecute `pnpm run generate:api`.
2. **Cero Red:** Tienes ESTRICTAMENTE PROHIBIDO crear archivos para llamadas a la API (ni `axios`, ni `fetch`). Usa únicamente los hooks exportados por Orval (ej. `useGetProcesses()`, `useCreateProcessMutation()`).
3. **Estructura:** Coloca el código dentro de `frontend/src/features/[nombre-feature]/`.
4. **UI vs Lógica:** Mantén los componentes de presentación "tontos". Extrae la lógica de llamadas a React Query en hooks personalizados dentro de la carpeta `/hooks` del feature si es muy compleja.
5. **Calidad:** Asegúrate de tipar todo estrictamente usando las interfaces generadas en `frontend/src/api/model/`.