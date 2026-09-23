// spec: specs/plan_login.md — caso 1.4
// seed: tests/seed.spec.ts
import { test, expect } from '@playwright/test';

/** El toggle usa aria-label "Mostrar contraseña"; getByLabel('Contraseña') matchea ambos. */
function passwordField(page: import('@playwright/test').Page) {
  return page.getByRole('textbox', { name: 'Contraseña' });
}

test.describe('Autenticación UC-001 — Login', () => {
  test('Correo fuera de dominio UMSS es rechazado en cliente', async ({ page }) => {
    // 1. Navegar a `/login`
    await page.goto('/login');
    await expect(page.getByRole('heading', { name: 'Bienvenido' })).toBeVisible();
    await expect(page.getByLabel('Correo Institucional')).toBeVisible();

    // 2. Ingresar `usuario@gmail.com` en correo y cualquier contraseña
    await page.getByRole('textbox', { name: 'Correo Institucional' }).fill('usuario@gmail.com');
    await passwordField(page).fill('cualquier');

    // 3. Clic en «Iniciar sesión»
    await page.getByRole('button', { name: 'Iniciar sesión' }).click();

    await expect(page).toHaveURL(/\/login/);
    await expect(
      page.getByText('Solo se permiten correos institucionales @umss.edu.bo.'),
    ).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Bienvenido' })).toBeVisible();
  });
});
