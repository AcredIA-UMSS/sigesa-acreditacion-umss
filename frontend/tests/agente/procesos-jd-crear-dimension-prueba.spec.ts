// spec: specs/plan_procesos_dimension_jd.md — caso 1.1
import { test, expect } from '@playwright/test';
import type { Page } from '@playwright/test';

const JD_EMAIL = 'jd@umss.edu.bo';
const JD_PASSWORD = 'JefeDemo2026!';

/** Nombre único por run (evita fallo si dimensionPrueba ya existe en seed). */
const DIMENSION_NAME = `dimensionPrueba-e2e-${Date.now()}`;

function passwordField(page: Page) {
  return page.getByRole('textbox', { name: 'Contraseña' });
}

async function loginAsJd(page: Page) {
  await page.goto('/login');
  await page.getByLabel('Correo Institucional').fill(JD_EMAIL);
  await passwordField(page).fill(JD_PASSWORD);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
  await expect(page).toHaveURL(/\/admin\/users/);
}

test.describe('Estructura normativa — JD crea dimensión (Administración de Empresas)', () => {
  test('JD crea dimensión en estructura normativa y la ve en detalle', async ({ page }) => {
    // 1. Login JD
    await loginAsJd(page);

    // 2. Listado de procesos
    await page.goto('/procesos');
    await expect(page.getByRole('heading', { name: 'Procesos de acreditación' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Carrera' })).toBeVisible();

    // 3. Detalle Administración de Empresas
    await page
      .getByRole('link', { name: 'Ver detalle del proceso Administración de Empresas' })
      .click();
    await expect(page).toHaveURL(/\/procesos\/[0-9a-f-]{36}$/i);
    await expect(page.getByRole('heading', { name: 'Administración de Empresas' })).toBeVisible();
    await expect(page.getByText('Activo', { exact: true })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Estructura del proceso' })).toBeVisible();

    // 4. Editor de estructura
    await page.getByRole('button', { name: 'Editar estructura normativa' }).click();
    await expect(page).toHaveURL(/\/estructura$/);
    await expect(page.getByText('Solo se listan las dimensiones (nivel 1)')).toBeVisible();

    // 5. Alta dimensión nivel 1
    await page.getByRole('button', { name: /Agregar nueva dimensión \(nivel 1\)/ }).click();
    await page.getByRole('textbox', { name: 'Nombre' }).fill(DIMENSION_NAME);
    await page.getByRole('button', { name: 'Agregar nivel 1' }).click();
    await expect(page.getByRole('button', { name: new RegExp(DIMENSION_NAME) })).toBeVisible();

    // 6. Volver al detalle
    await page.getByRole('link', { name: 'Volver al detalle' }).click();
    await expect(page).toHaveURL(/\/procesos\/[0-9a-f-]{36}$/i);
    await expect(page.getByRole('heading', { name: 'Estructura del proceso' })).toBeVisible();

    // 7. Dimensión visible en árbol (nivel 1 es botón colapsable, no link)
    await expect(page.getByRole('button', { name: new RegExp(DIMENSION_NAME) })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Editar estructura normativa' })).toBeVisible();
  });
});
