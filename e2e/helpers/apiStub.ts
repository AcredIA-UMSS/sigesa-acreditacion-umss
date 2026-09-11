import type { Page, Route } from '@playwright/test';

/** Fixture TC-04: solo PENDIENTE / OBSERVADO (GET /api/v1/indicators/uploadable). */
export const UPLOADABLE_INDICATORS = [
  {
    indicatorId: 'ind-pendiente',
    code: 'I-01',
    title: 'Plan de estudios',
    criterionId: 'crit-1',
    criterionCode: 'C-01',
    criterionTitle: 'Pertinencia curricular',
    currentState: 'PENDIENTE',
  },
  {
    indicatorId: 'ind-observado',
    code: 'I-02',
    title: 'Infraestructura',
    criterionId: 'crit-2',
    criterionCode: 'C-02',
    criterionTitle: 'Recursos físicos',
    currentState: 'OBSERVADO',
  },
] as const;

function json(route: Route, body: unknown, status = 200): Promise<void> {
  return route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify(body),
  });
}

function isBackendApiRequest(route: Route): boolean {
  const request = route.request();
  let pathname: string;
  try {
    pathname = new URL(request.url()).pathname;
  } catch {
    return false;
  }

  if (!pathname.startsWith('/api/v1/')) {
    return false;
  }

  const resourceType = request.resourceType();
  if (resourceType === 'script' || resourceType === 'stylesheet' || resourceType === 'font') {
    return false;
  }

  return true;
}

export type StubBackendOptions = {
  uploadable?: readonly Record<string, string>[];
};

/**
 * Evita depender del backend Java: las specs validan anclas `data-testid`.
 * `E2E_LIVE=1` deja pasar las llamadas reales (stack local 5173 + 8080).
 *
 * Nunca interceptar `/src/api/**` (cliente Orval servido por Vite).
 */
export async function stubBackend(
  page: Page,
  options: StubBackendOptions = {},
): Promise<void> {
  if (process.env.E2E_LIVE === '1') {
    return;
  }

  const uploadable = options.uploadable ?? UPLOADABLE_INDICATORS;

  await page.route('**/api/v1/**', async (route) => {
    if (!isBackendApiRequest(route)) {
      await route.continue();
      return;
    }

    const url = route.request().url();
    const method = route.request().method();

    if (url.includes('/auth/login') && method === 'POST') {
      const payload = (route.request().postDataJSON() ?? {}) as { email?: string };
      const email = payload.email ?? '';
      const role = email.startsWith('cc') ? 'CC' : email.startsWith('td') ? 'TD' : 'JD';
      await json(route, {
        accessToken: 'e2e-token',
        expiresIn: 3600,
        role,
        programScope: [],
      });
      return;
    }

    if (method === 'GET' && /\/users(\?|$)/.test(url)) {
      await json(route, []);
      return;
    }

    if (method === 'GET' && url.includes('/processes')) {
      await json(route, []);
      return;
    }

    if (method === 'GET' && url.includes('/templates')) {
      await json(route, []);
      return;
    }

    if (method === 'GET' && url.includes('/programs')) {
      await json(route, []);
      return;
    }

    if (method === 'GET' && url.includes('/indicators/uploadable')) {
      await json(route, uploadable);
      return;
    }

    if (method === 'GET' && url.includes('/indicators')) {
      await json(route, []);
      return;
    }

    if (method === 'GET' && url.includes('/assistant')) {
      await json(route, {
        enabled: false,
        model: 'stub',
        llmEnabled: false,
        capabilities: [],
        demoScenarios: [],
      });
      return;
    }

    await json(route, {});
  });
}
