// spec: specs/plan_aprobacion_td.md — caso 1.3
import { test, expect } from '@playwright/test';
import { loginTd, PROCESS_INF_SIS_ACTIVE_ID } from './helpers/auth';

test.describe('Revisión TD — sin evidencia', () => {
  test('TD no aprueba indicador sin evidencias', async ({ page }) => {
    await loginTd(page);
    await page.goto(`/procesos/${PROCESS_INF_SIS_ACTIVE_ID}`);
    await expect(page.getByRole('heading', { name: 'Estructura del proceso' })).toBeVisible();

    const closed = page.locator('button[aria-expanded="false"]');
    for (let i = 0; i < 12; i += 1) {
      const emptyReview = page
        .locator('div.mt-4.rounded-lg.p-4')
        .filter({ hasText: 'Revisión técnica del indicador' })
        .filter({ hasText: 'No hay evidencias cargadas' });
      if ((await emptyReview.count()) > 0) {
        await expect(
          emptyReview.first().getByRole('button', { name: 'Aprobar', exact: true }),
        ).toHaveCount(0);
        return;
      }
      if ((await closed.count()) === 0) break;
      await closed.first().click();
    }
    test.skip(true, 'No se encontró indicador sin evidencias en el árbol');
  });
});
