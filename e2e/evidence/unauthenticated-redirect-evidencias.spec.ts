// spec: e2e/specs/gestion-evidencias.plan.md · 1.3 (P03 aceptado)
// E2E: getByRole/getByTestId · sin waitForTimeout · UI no copy LLM · independiente · sin datos de sesión
import { expect, test } from '@playwright/test';
import { stubBackend } from '../helpers/apiStub';

test('unauthenticated-redirect-evidencias', async ({ page }) => {
  await stubBackend(page);
  await page.goto('/evidencias/cargar');

  await expect(page).toHaveURL(/\/login/);
  await expect(page.getByTestId('login-page')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Iniciar sesión' })).toBeVisible();
  await expect(page.getByTestId('evidence-upload-page')).toHaveCount(0);
});
