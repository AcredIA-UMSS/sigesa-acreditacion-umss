/**
 * Modal de trazabilidad del copiloto de usuarios (build-time).
 *
 * @see docs/design/assistant/DD-AGENT-002.md §10
 */
export const USERS_COPILOT_DEBUG_ACTIONS_ENABLED =
  import.meta.env.VITE_USERS_COPILOT_DEBUG_ACTIONS === 'true';
