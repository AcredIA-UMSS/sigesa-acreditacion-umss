// spec: specs/plan_plantilla_jd.md — caso 1.1
import { test, expect } from '@playwright/test';
import { loginJd } from './helpers/auth';

test.describe('Plantillas normativas — JD crea plantilla en borrador', () => {
  test('JD crea plantilla y la ve en listado', async ({ page }) => {
    await loginJd(page);

    await page.goto('/admin/plantillas');
    await expect(page.getByRole('heading', { name: 'Gestión de plantillas' })).toBeVisible();
    await page.getByRole('link', { name: 'Nueva plantilla' }).click();
    await expect(page).toHaveURL(/\/admin\/plantillas\/nueva/);
    await expect(page.getByRole('heading', { name: 'Nueva plantilla' })).toBeVisible();

    const uniqueName = `Plantilla E2E ${Date.now()}`;
    await page.getByLabel('Nombre').fill(uniqueName);
    await page.getByLabel('Tipo normativo').selectOption('CEUB');
    await page.getByLabel('Descripción').fill('Plantilla de ejemplo para E2E');

    await page.getByRole('button', { name: 'Guardar' }).click();
    await expect(page).toHaveURL(/\/admin\/plantillas\/[0-9a-f-]{36}/i);
    await expect(page.getByText('Borrador')).toBeVisible();

    await page.goto('/admin/plantillas');
    await expect(page.getByText(uniqueName)).toBeVisible();
  });
});
