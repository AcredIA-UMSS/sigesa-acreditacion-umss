/**
 * Patrón a mano que el Generator debe imitar.
 *
 * Reglas:
 *  - getByRole / getByLabel / getByTestId (nunca CSS ni nth-child)
 *  - Sin waitForTimeout
 *  - Cada test arranca en página nueva (page.goto)
 *  - No verificar textos volátiles del servidor/LLM; sí URLs, roles, tablas, alertas estables
 */
import { test, expect } from '@playwright/test';

const JD_EMAIL = 'jd@umss.edu.bo';
const JD_PASSWORD = 'JefeDemo2026!';

/** El toggle usa aria-label "Mostrar contraseña"; getByLabel('Contraseña') matchea ambos. */
function passwordField(page: import('@playwright/test').Page) {
  return page.getByRole('textbox', { name: 'Contraseña' });
}

test.describe('Autenticación UC-001', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test('JD inicia sesión y llega a gestión de usuarios', async ({ page }) => {
    await page.getByLabel('Correo Institucional').fill(JD_EMAIL);
    await passwordField(page).fill(JD_PASSWORD);
    await page.getByRole('button', { name: 'Iniciar sesión' }).click();

    await expect(page).toHaveURL(/\/admin\/users/);
    await expect(page.getByRole('heading', { name: 'Gestión de usuarios' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Usuarios registrados' })).toBeVisible();
  });

  test('credenciales inválidas muestran alerta y no navegan', async ({ page }) => {
    await page.getByLabel('Correo Institucional').fill('jd@umss.edu.bo');
    await passwordField(page).fill('contraseña-incorrecta');
    await page.getByRole('button', { name: 'Iniciar sesión' }).click();

    await expect(page).toHaveURL(/\/login/);
    await expect(page.getByRole('alert')).toBeVisible();
  });

  test('formulario vacío no sale de login', async ({ page }) => {
    await page.getByRole('button', { name: 'Iniciar sesión' }).click();
    await expect(page).toHaveURL(/\/login/);
    await expect(page.getByRole('heading', { name: 'Bienvenido' })).toBeVisible();
  });
});
