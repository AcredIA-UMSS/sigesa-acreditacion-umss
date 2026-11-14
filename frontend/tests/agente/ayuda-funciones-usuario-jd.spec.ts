// spec: specs/plan_ayuda.md — caso 1.1
// seed: tests/seed.spec.ts (login JD → /ayuda)
import { test, expect } from '@playwright/test';
import type { Page } from '@playwright/test';

const JD_EMAIL = 'jd@umss.edu.bo';
const JD_PASSWORD = 'JefeDemo2026!';
const FUNCIONES_PROMPT = 'listame las funciones que puedo hacer como usuario';

/** El toggle usa aria-label "Mostrar contraseña"; getByLabel('Contraseña') matchea ambos. */
function passwordField(page: Page) {
  return page.getByRole('textbox', { name: 'Contraseña' });
}

function queryField(page: Page) {
  return page.getByRole('textbox', { name: /Escriba su consulta/ });
}

async function loginAsJdAndOpenAyuda(page: Page) {
  await page.goto('/login');
  await page.getByLabel('Correo Institucional').fill(JD_EMAIL);
  await passwordField(page).fill(JD_PASSWORD);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
  await expect(page).toHaveURL(/\/admin\/users/);
  await page.goto('/ayuda');
  await expect(page).toHaveURL(/\/ayuda/);
}

test.describe('Asistente /ayuda — agent=general', () => {
  test('JD pregunta funciones disponibles y recibe respuesta con trazabilidad', async ({
    page,
  }) => {
    // 1. Iniciar sesión JD y navegar a /ayuda
    await loginAsJdAndOpenAyuda(page);
    await expect(page.getByRole('heading', { name: 'Asistente virtual' })).toBeVisible();
    await expect(page.getByText('SIGESA · Ayuda')).toBeVisible();

    // 2. Escribir consulta de funciones
    const sendButton = page.getByRole('button', { name: 'Enviar' });
    await queryField(page).fill(FUNCIONES_PROMPT);
    await expect(sendButton).toBeEnabled();

    // 3. Enviar — el modal de historial se abre automáticamente
    await sendButton.click();
    const dialog = page.getByRole('dialog');
    await expect(
      dialog.getByRole('heading', { name: 'Historial de acciones del asistente' }),
    ).toBeVisible();
    await expect(dialog.getByText('Mensaje enviado al backend (/assistant/chat)')).toBeVisible();
    await expect(dialog.getByText('Respuesta formateada entregada al chat')).toBeVisible();
    await expect(dialog.getByText('OK', { exact: true })).toBeVisible({ timeout: 15_000 });

    // 4. Cerrar modal
    await dialog.getByRole('button', { name: 'Cerrar' }).first().click();
    await expect(dialog).toBeHidden();

    // Conversación: mensaje del usuario y respuesta del asistente
    await expect(page.getByText(FUNCIONES_PROMPT)).toBeVisible();
    const main = page.getByRole('main');
    await expect(main.getByText(/SIGESA|Funciones|acreditación/i).first()).toBeVisible();

    // 5. Respuesta informativa y metadata (sin redacción exacta del LLM)
    await expect(main.getByText(/Camino:\s*KEYWORD/)).toBeVisible();
    await expect(main.getByText(/Puedo ayudarte con:|•/i).first()).toBeVisible();
    await expect(page.getByRole('button', { name: /Historial de acciones \(1\)/ })).toBeVisible();
  });
});
