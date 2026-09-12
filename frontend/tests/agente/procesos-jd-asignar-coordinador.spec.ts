// spec: specs/plan_proceso_responsable_jd.md — caso 1.1
import { test, expect } from '@playwright/test';
import { loginJd, PROCESS_INF_SIS_ACTIVE_ID } from './helpers/auth';

test.describe('Proceso — JD asigna coordinador', () => {
  test('JD asigna CC responsable en proceso ACTIVE', async ({ page }) => {
    await loginJd(page);
    await page.goto(`/procesos/${PROCESS_INF_SIS_ACTIVE_ID}`);
    await expect(page.getByText('Activo', { exact: true })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Responsable del proceso' })).toBeVisible();

    const assignBtn = page.getByRole('button', {
      name: /Asignar responsable|Cambiar responsable/,
    });
    await assignBtn.click();
    await expect(page.getByRole('heading', { name: 'Asignar responsable' })).toBeVisible();

    const select = page.getByLabel('Coordinador responsable');
    await expect(select.locator('option').nth(1)).toBeAttached({ timeout: 15_000 });
    const values = await select.locator('option').evaluateAll((opts) =>
      opts.map((o) => o.value).filter((v) => v.length > 0),
    );
    test.skip(values.length === 0, 'No hay CC elegible para la carrera del proceso en seed');

    await select.selectOption(values[0]!);
    await page.getByRole('button', { name: 'Confirmar asignación' }).click();
    await expect(page.getByRole('heading', { name: 'Asignar responsable' })).toBeHidden();
    await expect(page.getByText(/@umss\.edu\.bo/)).toBeVisible();
  });
});
