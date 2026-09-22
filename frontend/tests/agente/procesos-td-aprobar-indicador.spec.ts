// spec: specs/plan_aprobacion_td.md — caso 1.1
import { test, expect } from '@playwright/test';
import { loginTd, PROCESS_INF_SIS_ACTIVE_ID } from './helpers/auth';

async function expandUntilApprove(page: import('@playwright/test').Page) {
  const closed = page.locator('button[aria-expanded="false"]');
  for (let i = 0; i < 12; i += 1) {
    const approve = page.getByRole('button', { name: 'Aprobar' });
    if (await approve.isVisible().catch(() => false)) return approve;
    const count = await closed.count();
    if (count === 0) break;
    await closed.first().click();
  }
  return page.getByRole('button', { name: 'Aprobar' });
}

test.describe('Revisión TD — aprobar indicador', () => {
  test('TD aprueba indicador con evidencia', async ({ page }) => {
    await loginTd(page);
    await page.goto(`/procesos/${PROCESS_INF_SIS_ACTIVE_ID}`);
    await expect(page.getByRole('heading', { name: 'Estructura del proceso' })).toBeVisible();

    const approve = await expandUntilApprove(page);
    await expect(page.getByText('Revisión técnica del indicador').first()).toBeVisible();
    const noEvidence = page.getByText('No hay evidencias cargadas').first();
    if (await noEvidence.isVisible().catch(() => false)) {
      test.skip(true, 'Ejecutar antes evidencia-cc-cargar-pagina.spec.ts (workers=1)');
    }
    const approveBtn = approve.first();
    await expect(approveBtn).toBeVisible();
    await approveBtn.click();
    await expect(page.getByText(/aprobado/i)).toBeVisible({ timeout: 30_000 });
  });
});
