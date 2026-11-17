import { resolveAccessToken } from '../../../lib/auth/authBridge';
import { ApiError } from '../../../lib/api/apiError';

export type Level1CompleteResponse = {
  level1Id: string;
  previousState: string;
  newState: string;
  event: string;
};

export type PendingIndicatorItem = {
  indicatorId: string;
  code: string;
  name: string;
  status: string;
  order?: number;
};

export class Level1ClosureBlockedError extends Error {
  readonly code = 'NIVEL1_CIERRE_BLOQUEADO';
  readonly status = 409;
  readonly pendingIndicators: PendingIndicatorItem[];

  constructor(message: string, pendingIndicators: PendingIndicatorItem[]) {
    super(message);
    this.name = 'Level1ClosureBlockedError';
    this.pendingIndicators = pendingIndicators;
  }
}

export function isLevel1ClosureBlockedError(error: unknown): error is Level1ClosureBlockedError {
  return error instanceof Level1ClosureBlockedError;
}

export async function closeLevel1(
  processId: string,
  level1Id: string,
): Promise<Level1CompleteResponse> {
  const baseUrl = import.meta.env.VITE_API_URL ?? '';
  const url = `${baseUrl}/api/v1/processes/${encodeURIComponent(processId)}/level1-nodes/${encodeURIComponent(level1Id)}/complete`;
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
    pendingIndicators?: PendingIndicatorItem[];
  } = {};

  if (rawBody) {
    try {
      parsed = JSON.parse(rawBody) as typeof parsed;
    } catch {
      /* ignore */
    }
  }

  if (!response.ok) {
    if (response.status === 409 && parsed.error === 'NIVEL1_CIERRE_BLOQUEADO') {
      throw new Level1ClosureBlockedError(
        parsed.message ?? 'No se puede cerrar el Nivel 1: hay indicadores pendientes.',
        parsed.pendingIndicators ?? [],
      );
    }
    throw new ApiError(
      response.status,
      parsed.error ?? 'UNKNOWN_ERROR',
      parsed.message ?? 'No se pudo cerrar el Nivel 1.',
    );
  }

  return parsed as Level1CompleteResponse;
}
