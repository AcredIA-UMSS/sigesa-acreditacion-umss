// spec: e2e/specs/gestion-evidencias.plan.md · 1.1 (P01 aceptado)
// E2E: getByLabel/getByRole/getByTestId · sin waitForTimeout · UI no copy LLM · independiente · datos locales
import { expect, test } from '@playwright/test';
import { stubBackend } from '../helpers/apiStub';

test('cc-login-sidebar-cargar-evidencia', async ({ page }) => {
  const cc = { email: 'cc@umss.edu.bo', password: 'CoordDemo2026!' };

  await stubBackend(page);
  await page.goto('/login');

  await expect(page.getByTestId('login-page')).toBeVisible();
  await page.getByLabel('Correo Institucional').fill(cc.email);
  await page.getByLabel('Contraseña').fill(cc.password);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();

  await expect(page).toHaveURL(/\/dashboard/);
  await expect(page.getByTestId('dashboard-page')).toBeVisible();

  await page.getByRole('link', { name: 'Cargar evidencia' }).click();

  await expect(page).toHaveURL(/\/evidencias\/cargar/);
  await expect(page.getByTestId('evidence-upload-page')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Cargar Evidencia' })).toBeVisible();
  await expect(page.getByLabel('Indicador')).toBeVisible();
  await expect(page.getByLabel('Criterio')).toBeVisible();
  await expect(page.getByLabel('Descripción')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Subir evidencia' })).toBeVisible();
  await expect(page.getByLabel('Archivo de evidencia')).toBeAttached();
});
