import { resolveAccessToken } from '../../../lib/auth/authBridge';
import { ApiError } from '../../../lib/api/apiError';

export type PhaseCompleteResponse = {
  phaseId: string;
  previousState: string;
  newState: string;
  event: string;
};

export type PendingSubphaseItem = {
  subphaseId: string;
  name: string;
  status: string;
  order?: number;
};

export class PhaseClosureBlockedError extends Error {
  readonly code = 'FASE_CIERRE_BLOQUEADO';
  readonly status = 409;
  readonly pendingSubphases: PendingSubphaseItem[];

  constructor(message: string, pendingSubphases: PendingSubphaseItem[]) {
    super(message);
    this.name = 'PhaseClosureBlockedError';
    this.pendingSubphases = pendingSubphases;
  }
}

export function isPhaseClosureBlockedError(error: unknown): error is PhaseClosureBlockedError {
  return error instanceof PhaseClosureBlockedError;
}

export async function closePhase(
  processId: string,
  phaseId: string,
): Promise<PhaseCompleteResponse> {
  const baseUrl = import.meta.env.VITE_API_URL ?? '';
  const url = `${baseUrl}/api/v1/processes/${encodeURIComponent(processId)}/phases/${encodeURIComponent(phaseId)}/complete`;
  const headers = new Headers({ 'Content-Type': 'application/json' });
  const token = resolveAccessToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(url, { method: 'POST', headers });

  const rawBody = await response.text();
  let parsed: {
    error?: string;
    message?: string;
    pendingSubphases?: PendingSubphaseItem[];
  } = {};

  if (rawBody) {
    try {
      parsed = JSON.parse(rawBody) as typeof parsed;
    } catch {
      /* ignore */
    }
  }

  if (!response.ok) {
    if (response.status === 409 && parsed.error === 'FASE_CIERRE_BLOQUEADO') {
      throw new PhaseClosureBlockedError(
        parsed.message ?? 'No se puede cerrar la fase: hay subfases pendientes.',
        parsed.pendingSubphases ?? [],
      );
    }
    throw new ApiError(
      response.status,
      parsed.error ?? 'UNKNOWN_ERROR',
      parsed.message ?? 'No se pudo cerrar la fase.',
    );
  }

  return (parsed as PhaseCompleteResponse);
}
