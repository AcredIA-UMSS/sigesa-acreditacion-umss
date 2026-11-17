// spec: PR-IMPL-052 / BR-NOTIFY-001 — CC in-app popover (reject / approve / deadline seeds)
import { test, expect } from '@playwright/test';
import { blockAssistantApi, loginCc, stubDashboardSummary } from './helpers/auth';

test.describe('Notificaciones CC — popover con eventos de revisión', () => {
  test.beforeEach(async ({ page }) => {
    await blockAssistantApi(page);
    await stubDashboardSummary(page);
  });

  test('CC ve observaciones/aprobaciones seed y puede abrir el popover', async ({ page }) => {
    await loginCc(page);

    const bell = page.getByRole('button', { name: /Notificaciones/i }).first();
    await expect(bell).toBeVisible({ timeout: 20_000 });
    await expect(bell).toHaveAttribute('aria-label', /sin leer/i);

    await bell.click();
    const dialog = page.getByRole('dialog', { name: /Notificaciones recientes/i });
    await expect(dialog).toBeVisible();

    await expect(dialog.getByText(/Indicador observado|Atención: Indicador observado/i).first()).toBeVisible({
      timeout: 15_000,
    });
    await expect(dialog.getByText(/Indicador aprobado/i).first()).toBeVisible();
    await expect(dialog.getByText(/Plazo próximo/i).first()).toBeVisible();
  });

  test('CC hace click en observación y navega al deep link del proceso/indicador', async ({ page }) => {
    await loginCc(page);

    const bell = page.getByRole('button', { name: /Notificaciones/i }).first();
    await expect(bell).toBeVisible({ timeout: 20_000 });
    await bell.click();

    const dialog = page.getByRole('dialog', { name: /Notificaciones recientes/i });
    await expect(dialog.getByText(/Indicador observado|Atención: Indicador observado/i).first()).toBeVisible({
      timeout: 15_000,
    });

    await dialog.getByRole('button', { name: /Indicador observado|Atención: Indicador observado/i }).first().click();
    await expect(page).toHaveURL(/\/procesos\/[0-9a-f-]+\/indicadores\/[0-9a-f-]+/i, { timeout: 20_000 });
  });
});

  test('CC ve seed Proceso asignado y navega al proceso', async ({ page }) => {
    await loginCc(page);

    const bell = page.getByRole('button', { name: /Notificaciones/i }).first();
    await expect(bell).toBeVisible({ timeout: 20_000 });
    await bell.click();

    const dialog = page.getByRole('dialog', { name: /Notificaciones recientes/i });
    await expect(dialog.getByText(/Proceso asignado/i).first()).toBeVisible({ timeout: 15_000 });

    await dialog.getByRole('button', { name: /Proceso asignado/i }).first().click();
    await expect(page).toHaveURL(/\/procesos\/[0-9a-f-]+/i, { timeout: 20_000 });
  });
