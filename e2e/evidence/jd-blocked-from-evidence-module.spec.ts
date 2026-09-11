// spec: e2e/specs/gestion-evidencias.plan.md · 2.1 (P04 aceptado)
// E2E: getByLabel/getByRole/getByTestId · sin waitForTimeout · UI no copy LLM · independiente · datos locales
import { expect, test } from '@playwright/test';
import { stubBackend } from '../helpers/apiStub';

test('jd-blocked-from-evidence-module', async ({ page }) => {
  const jd = { email: 'jd@umss.edu.bo', password: 'JefeDemo2026!' };

  await stubBackend(page);
  await page.goto('/login');

  await page.getByLabel('Correo Institucional').fill(jd.email);
  await page.getByLabel('Contraseña').fill(jd.password);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();

  await expect(page).toHaveURL(/\/admin\/users/);
  await expect(page.getByTestId('users-admin-page')).toBeVisible();
  await expect(page.getByRole('link', { name: 'Cargar evidencia' })).toHaveCount(0);

  await page.goto('/evidencias/cargar');

  await expect(page).not.toHaveURL(/\/evidencias\/cargar/);
  await expect(page.getByTestId('evidence-upload-page')).toHaveCount(0);
  await expect(page.getByRole('heading', { name: 'Cargar Evidencia' })).toHaveCount(0);
});
