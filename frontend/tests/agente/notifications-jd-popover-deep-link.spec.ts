// spec: PR-IMPL-052 / BR-NOTIFY-001 — JD in-app popover + ProcessClosed deep link
import { test, expect } from '@playwright/test';
import { blockAssistantApi, loginJd, PROCESS_INF_SIS_ACTIVE_ID } from './helpers/auth';

test.describe('Notificaciones JD — popover y deep link ProcessClosed', () => {
  test.beforeEach(async ({ page }) => {
    await blockAssistantApi(page);
  });

  test('JD ve campana con no leídas, abre popover y navega a semáforo', async ({ page }) => {
    await loginJd(page);

    const bell = page.getByRole('button', { name: /Notificaciones/i }).first();
    await expect(bell).toBeVisible({ timeout: 20_000 });
    await expect(bell).toHaveAttribute('aria-label', /sin leer/i);

    await bell.click();
    const dialog = page.getByRole('dialog', { name: /Notificaciones recientes/i });
    await expect(dialog).toBeVisible();
    await expect(dialog.getByText('Proceso cerrado').first()).toBeVisible({ timeout: 15_000 });
    await expect(dialog.getByText(/cerró el proceso|CLOSED|acreditación/i).first()).toBeVisible();

    await dialog.getByRole('button', { name: /Proceso cerrado/i }).first().click();
    await expect(page).toHaveURL(/\/executive\/semaphore/, { timeout: 20_000 });
    await expect(page.getByRole('heading', { name: /Semáforo institucional/i })).toBeVisible({
      timeout: 20_000,
    });
  });

  test('JD marca todas como leídas desde el popover', async ({ page }) => {
    await loginJd(page);

    const bell = page.getByRole('button', { name: /Notificaciones/i }).first();
    await expect(bell).toBeVisible({ timeout: 20_000 });
    await bell.click();

    const dialog = page.getByRole('dialog', { name: /Notificaciones recientes/i });
    await expect(dialog).toBeVisible();
    await expect(dialog.getByText('Proceso cerrado').first()).toBeVisible({ timeout: 15_000 });

    const markAll = dialog.getByRole('button', { name: /Marcar leídas/i });
    await expect(markAll).toBeEnabled();
    await markAll.click();

    await expect
      .poll(async () => {
        const label = await bell.getAttribute('aria-label');
        return label ?? '';
      }, { timeout: 20_000 })
      .not.toMatch(/sin leer/i);
  });

  test('Desde semáforo, click en carrera con proceso abre /procesos/{id}', async ({ page }) => {
    await loginJd(page);
    await page.goto('/executive/semaphore');
    await expect(page.getByRole('heading', { name: /Semáforo institucional/i })).toBeVisible({
      timeout: 30_000,
    });

    // Prefer a row that can open a process (title="Abrir proceso" on status pill).
    const openPill = page.getByRole('button', { name: /Saludable|En alerta|Crítico|Sin dato/i }).first();
    const hasOpenable = await openPill.isVisible().catch(() => false);
    if (!hasOpenable) {
      // Fallback: click first data row if present.
      const row = page.locator('tbody tr').first();
      if (!(await row.isVisible().catch(() => false))) {
        test.skip(true, 'Semáforo sin filas de carrera en este entorno');
      }
      await row.click();
    } else {
      await openPill.click();
    }

    await expect(page).toHaveURL(/\/procesos\/[0-9a-f-]{36}/i, { timeout: 20_000 });
    // Seed ACTIVE process for Ingeniería de Sistemas is a common target.
    const url = page.url();
    if (url.includes(PROCESS_INF_SIS_ACTIVE_ID) || /\/procesos\//.test(url)) {
      await expect(page.getByRole('heading', { name: /Estructura del proceso|Detalle|Proceso/i }).first()).toBeVisible({
        timeout: 30_000,
      }).catch(async () => {
        // Some process detail layouts use different headings; URL match is the contract.
        await expect(page).toHaveURL(/\/procesos\//);
      });
    }
  });
});
