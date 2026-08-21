/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL?: string;
  readonly VITE_PHASES_COPILOT_DEBUG_ACTIONS?: string;
  readonly VITE_USERS_COPILOT_DEBUG_ACTIONS?: string;
  readonly VITE_EVIDENCE_COPILOT_DEBUG_ACTIONS?: string;
  readonly DEV: boolean;
  readonly PROD: boolean;
  readonly MODE: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
