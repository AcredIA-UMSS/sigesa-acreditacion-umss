import { type Page, expect } from '@playwright/test';
import { TestId } from '../fixtures/testids';

export const AUTH_STORAGE_KEY = 'sigesa_auth_session';

export const SeedUsers = {
  jd: { email: 'jd@umss.edu.bo', password: 'JefeDemo2026!', role: 'JD' as const },
  cc: { email: 'cc@umss.edu.bo', password: 'CoordDemo2026!', role: 'CC' as const },
};

type SeedRole = 'JD' | 'CC' | 'TD' | 'EE';

export async function seedSession(page: Page, role: SeedRole): Promise<void> {
  const session = {
    accessToken: `e2e-${role}`,
    expiresIn: 3600,
    role,
    programScope: [],
    expiresAt: Date.now() + 60 * 60 * 1000,
  };

  await page.addInitScript(
    ({ key, value }) => {
      window.localStorage.setItem(key, value);
    },
    { key: AUTH_STORAGE_KEY, value: JSON.stringify(session) },
  );
}

export async function loginViaForm(
  page: Page,
  credentials: { email: string; password: string },
): Promise<void> {
  await page.goto('/login');
  await expect(page.getByTestId(TestId.loginPage)).toBeVisible();
  await page.getByTestId(TestId.loginEmail).fill(credentials.email);
  await page.getByTestId(TestId.loginPassword).fill(credentials.password);
  await page.getByTestId(TestId.loginSubmit).click();
}
