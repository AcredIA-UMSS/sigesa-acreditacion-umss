/**
 * Badge «Modo desarrollo» en el modal de trazabilidad del asistente general (/ayuda).
 * El historial de acciones está siempre disponible; este flag solo controla el badge.
 *
 * @see docs/design/DD-SYS-002.md §11
 */
export const ASSISTANT_COPILOT_DEBUG_ACTIONS_ENABLED =
  import.meta.env.VITE_ASSISTANT_DEBUG_ACTIONS === 'true';
