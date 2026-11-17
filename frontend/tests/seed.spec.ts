/**
 * Semilla para Planner / Generator (Playwright agents o prompts manuales).
 * No prueba lógica de negocio: solo demuestra cómo abrir la app.
 */
import { test, expect } from '@playwright/test';

test.describe('SIGESA', () => {
  test('semilla — login visible', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByRole('heading', { name: 'Bienvenido' })).toBeVisible();
    await expect(page.getByLabel('Correo Institucional')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Iniciar sesión' })).toBeVisible();
  });
});
