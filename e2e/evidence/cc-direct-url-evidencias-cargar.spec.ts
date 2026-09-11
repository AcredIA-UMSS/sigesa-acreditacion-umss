// spec: e2e/specs/gestion-evidencias.plan.md · 1.2 (P02 aceptado)
// E2E: getByLabel/getByRole/getByTestId · sin waitForTimeout · UI no copy LLM · independiente · datos locales
import { expect, test } from '@playwright/test';
import { stubBackend } from '../helpers/apiStub';

test('cc-direct-url-evidencias-cargar', async ({ page }) => {
  const cc = { email: 'cc-url@umss.edu.bo', password: 'CoordUrl2026!' };

  await stubBackend(page);
  await page.goto('/login');

  await page.getByLabel('Correo Institucional').fill(cc.email);
  await page.getByLabel('Contraseña').fill(cc.password);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
  await expect(page.getByTestId('dashboard-page')).toBeVisible();

  await page.goto('/evidencias/cargar');

  await expect(page).toHaveURL(/\/evidencias\/cargar/);
  await expect(page).not.toHaveURL(/\/login/);
  await expect(page.getByTestId('evidence-upload-page')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Cargar Evidencia' })).toBeVisible();
  await expect(page.getByLabel('Indicador')).toBeVisible();
});
