// spec: PR-IMPL-052 — ProcessUnassigned: text-only, no deep-link navigation
import { test, expect } from '@playwright/test';
import { blockAssistantApi, loginCc, stubDashboardSummary } from './helpers/auth';

test.describe('Notificaciones CC — responsabilidad retirada (sin deep link)', () => {
  test.beforeEach(async ({ page }) => {
    await blockAssistantApi(page);
    await stubDashboardSummary(page);
  });

  test('CC ve seed Unassigned y al click solo marca leída (sin navegar a proceso)', async ({ page }) => {
    await loginCc(page);

    const bell = page.getByRole('button', { name: /Notificaciones/i }).first();
    await expect(bell).toBeVisible({ timeout: 20_000 });
    await bell.click();

    const dialog = page.getByRole('dialog', { name: /Notificaciones recientes/i });
    await expect(dialog).toBeVisible();
    await expect(dialog.getByText(/Responsabilidad retirada/i).first()).toBeVisible({ timeout: 15_000 });

    const urlBefore = page.url();
    await dialog.getByRole('button', { name: /Responsabilidad retirada/i }).first().click();

    await expect(page.getByRole('dialog', { name: /Notificaciones recientes/i })).toHaveCount(0, {
      timeout: 10_000,
    });
    await expect(page).not.toHaveURL(/\/procesos\/[0-9a-f-]+$/i);
    expect(new URL(page.url()).pathname).toBe(new URL(urlBefore).pathname);
  });
});
