/**
 * Modal de trazabilidad del copiloto de evidencias (build-time).
 *
 * @see docs/design/assistant/DD-AGENT-003.md §10
 */
export const EVIDENCE_COPILOT_DEBUG_ACTIONS_ENABLED =
  import.meta.env.VITE_EVIDENCE_COPILOT_DEBUG_ACTIONS === 'true';
