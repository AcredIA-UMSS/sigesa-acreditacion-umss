import { expect, type Page } from '@playwright/test';

export const AUTH_STORAGE_KEY = 'sigesa_auth_session';

export const JD = { email: 'jd@umss.edu.bo', password: 'JefeDemo2026!' };
export const CC = { email: 'cc@umss.edu.bo', password: 'CoordDemo2026!' };
export const TD = { email: 'td@umss.edu.bo', password: 'TecnicoDemo2026!' };

/** Proceso ACTIVE seed backend (Ingeniería de Sistemas). */
export const PROCESS_INF_SIS_ACTIVE_ID = '950e8400-e29b-41d4-a716-446655440020';

export function passwordField(page: Page) {
  return page.getByRole('textbox', { name: 'Contraseña' });
}

export async function loginAs(
  page: Page,
  email: string,
  password: string,
  postLoginUrl: RegExp = /\/(admin\/users|dashboard)/,
) {
  await page.goto('/login');
  await page.getByLabel('Correo Institucional').fill(email);
  await passwordField(page).fill(password);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
  await expect(page).toHaveURL(postLoginUrl);
  await page.waitForFunction(
    (key) => {
      const raw = localStorage.getItem(key);
      if (!raw) return false;
      try {
        const parsed = JSON.parse(raw) as { accessToken?: string };
        return typeof parsed.accessToken === 'string' && parsed.accessToken.length > 20;
      } catch {
        return false;
      }
    },
    AUTH_STORAGE_KEY,
    { timeout: 15_000 },
  );
}

/** Evita que /assistant/* dispare logout global (401) durante E2E de carga. */
export async function blockAssistantApi(page: Page) {
  await page.route('**/api/v1/assistant/**', (route) => route.abort('blockedbyclient'));
}

/** Evita 401 en dashboard con sesión mock o durante navegación post-login. */
export async function stubDashboardSummary(page: Page) {
  await page.route('**/api/v1/dashboards/me/summary', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        userId: 'e2e-user',
        grantedPermissions: ['READ_CC_DASHBOARD'],
        coordinatorSection: {
          programId: '550e8400-e29b-41d4-a716-446655440000',
          programName: 'Ingeniería de Sistemas',
          totalIndicators: 1,
          overallProgressPercentage: 0,
          approvedEvidences: 0,
          rejectedEvidences: 0,
          pendingObservations: 0,
          phaseProgressList: [],
          bottlenecks: [],
        },
      }),
    });
  });
}

export async function loginJd(page: Page) {
  await loginAs(page, JD.email, JD.password, /\/admin\/users/);
}

export async function loginCc(page: Page) {
  await loginAs(page, CC.email, CC.password, /\/dashboard/);
}

const DEFAULT_API_BASE = process.env.PW_API_BASE ?? 'http://127.0.0.1:8080';

/** Primer indicador PENDIENTE/OBSERVADO del CC (GET uploadable). */
export async function fetchFirstUploadableIndicatorId(page: Page): Promise<string | null> {
  const token = await page.evaluate((key) => {
    const raw = localStorage.getItem(key);
    if (!raw) return '';
    try {
      const parsed = JSON.parse(raw) as { accessToken?: string };
      return typeof parsed.accessToken === 'string' ? parsed.accessToken : '';
    } catch {
      return '';
    }
  }, AUTH_STORAGE_KEY);
  if (!token) return null;

  const res = await page.request.get(`${DEFAULT_API_BASE}/api/v1/indicators/uploadable`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok()) return null;
  const body = (await res.json()) as Array<{ indicatorId?: string }>;
  const id = body[0]?.indicatorId;
  return id && id.length > 0 ? id : null;
}

export async function loginTd(page: Page) {
  await loginAs(page, TD.email, TD.password, /\/dashboard/);
}
