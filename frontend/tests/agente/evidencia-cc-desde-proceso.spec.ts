// spec: specs/plan_evidencia_cc.md — caso 1.2
import { test, expect } from '@playwright/test';
import { blockAssistantApi, loginCc, PROCESS_INF_SIS_ACTIVE_ID } from './helpers/auth';
import { evidencePdfUpload } from './helpers/evidenceFixture';

async function expandNormativeTree(page: import('@playwright/test').Page) {
  const tip = page.getByText('Use el icono ▸ en cada fila');
  await expect(tip).toBeVisible();
  const closed = page.locator('button[aria-expanded="false"]');
  for (let i = 0; i < 8; i += 1) {
    const count = await closed.count();
    if (count === 0) break;
    await closed.first().click();
    const upload = page.getByRole('button', { name: 'Subir evidencia' });
    if (await upload.isVisible().catch(() => false)) return;
  }
}

test.describe('Evidencia CC — desde detalle de proceso', () => {
  test('CC sube evidencia desde el árbol normativo', async ({ page }) => {
    test.setTimeout(120_000);
    await blockAssistantApi(page);
    await loginCc(page);
    await page.goto(`/procesos/${PROCESS_INF_SIS_ACTIVE_ID}`);
    await page.waitForResponse(
      (r) => r.url().includes('/api/v1/processes/') && r.request().method() === 'GET' && r.ok(),
      { timeout: 30_000 },
    );
    await expect(page.getByRole('heading', { name: 'Estructura del proceso' })).toBeVisible();

    await expandNormativeTree(page);
    const uploadLink = page.getByRole('button', { name: 'Subir evidencia' });
    await expect(uploadLink).toBeVisible();
    await uploadLink.click();

    await expect(page.getByRole('dialog', { name: /Cargar evidencia/i })).toBeVisible();
    await page.getByLabel(/^Descripción/i).fill(`E2E modal ${Date.now()}`);
    await page.locator('input[type="file"]').last().setInputFiles(evidencePdfUpload());
    const uploadDone = page.waitForResponse(
      (r) => r.url().includes('/evidences') && r.request().method() === 'POST',
      { timeout: 90_000 },
    );
    await page.getByRole('button', { name: 'Subir evidencia' }).last().click();
    const response = await uploadDone;
    const responseBody = await response.text();
    expect(
      response.ok(),
      `Upload HTTP ${response.status()}: ${responseBody.slice(0, 500)}`,
    ).toBeTruthy();
    await expect(page.getByText(/Evidencia cargada/i)).toBeVisible();
  });
});
