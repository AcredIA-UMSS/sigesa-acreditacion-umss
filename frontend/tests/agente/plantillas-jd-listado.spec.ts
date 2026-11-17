// spec: specs/plan_plantilla_jd.md — caso 1.2
import { test, expect } from '@playwright/test';
import { loginJd } from './helpers/auth';

test.describe('Plantillas normativas — listado JD', () => {
  test('JD ve gestión de plantillas', async ({ page }) => {
    await loginJd(page);
    await page.goto('/admin/plantillas');
    await expect(page.getByRole('heading', { name: 'Gestión de plantillas' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Nueva plantilla' })).toBeVisible();
  });
});
