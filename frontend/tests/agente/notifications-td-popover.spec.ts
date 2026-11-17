// spec: PR-IMPL-052 / BR-NOTIFY-001 — TD in-app popover (EvidenceUploaded / EvidenceSubsanated)
import { test, expect } from '@playwright/test';
import { blockAssistantApi, loginTd, stubDashboardSummary } from './helpers/auth';

test.describe('Notificaciones TD — popover evidencias', () => {
  test.beforeEach(async ({ page }) => {
    await blockAssistantApi(page);
    await stubDashboardSummary(page);
  });

  test('TD ve evidencias nuevas y subsanadas en el popover', async ({ page }) => {
    await loginTd(page);

    const bell = page.getByRole('button', { name: /Notificaciones/i }).first();
    await expect(bell).toBeVisible({ timeout: 20_000 });
    await expect(bell).toHaveAttribute('aria-label', /sin leer/i);

    await bell.click();
    const dialog = page.getByRole('dialog', { name: /Notificaciones recientes/i });
    await expect(dialog).toBeVisible();

    await expect(dialog.getByText(/Nueva evidencia/i).first()).toBeVisible({ timeout: 15_000 });
    await expect(dialog.getByText(/Evidencia subsanada/i).first()).toBeVisible();
  });

  test('TD click en Nueva evidencia navega a bandeja técnico', async ({ page }) => {
    await loginTd(page);

    const bell = page.getByRole('button', { name: /Notificaciones/i }).first();
    await expect(bell).toBeVisible({ timeout: 20_000 });
    await bell.click();

    const dialog = page.getByRole('dialog', { name: /Notificaciones recientes/i });
    await expect(dialog.getByText(/Nueva evidencia/i).first()).toBeVisible({ timeout: 15_000 });
    await dialog.getByRole('button', { name: /Nueva evidencia/i }).first().click();

    await expect(page).toHaveURL(/\/(dashboard\/technician\/inbox|technician\/inbox)/, { timeout: 20_000 });
  });
});
