/**
 * Modal de trazabilidad de acciones del copiloto de fases.
 * Activado en tiempo de build con VITE_PHASES_COPILOT_DEBUG_ACTIONS=true
 * (docker-compose, frontend/.env o ARG del Dockerfile).
 *
 * @see docs/design/assistant/DD-AGENT-001.md §10.2
 */
export const PHASES_COPILOT_DEBUG_ACTIONS_ENABLED =
  import.meta.env.VITE_PHASES_COPILOT_DEBUG_ACTIONS === 'true';
