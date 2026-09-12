// spec: specs/plan_evidencia_cc.md — caso 1.1
import { test, expect } from '@playwright/test';
import { blockAssistantApi, loginCc } from './helpers/auth';
import { evidencePdfUpload } from './helpers/evidenceFixture';

test.describe('Evidencia CC — página Cargar evidencia', () => {
  test('CC sube evidencia en /evidencias/cargar', async ({ page }) => {
    test.setTimeout(120_000);
    await blockAssistantApi(page);
    await loginCc(page);
    await page.goto('/evidencias/cargar');
    await page.waitForResponse(
      (r) => r.url().includes('/api/v1/processes') && r.request().method() === 'GET' && r.ok(),
      { timeout: 30_000 },
    );
    await expect(page.getByRole('heading', { name: 'Cargar Evidencia' })).toBeVisible();

    const proceso = page.getByLabel('Proceso');
    await expect(proceso).toBeVisible();
    const hasProcess = await proceso.locator('option').count();
    test.skip(hasProcess <= 1, 'Sin procesos ACTIVE para la carrera del CC en seed');

    await proceso.selectOption({ index: 1 });
    const indicador = page.getByLabel('Indicador normativo');
    await expect(indicador).toBeEnabled();
    const indicatorValues = await indicador.locator('option').evaluateAll((opts) =>
      opts.map((o) => o.value).filter((v) => v.length > 0),
    );
    test.skip(indicatorValues.length === 0, 'Proceso sin indicadores normativos v2 en seed');
    await indicador.selectOption(indicatorValues[0]!);

    const desc = `E2E evidencia ${Date.now()}`;
    await page.getByLabel(/DESCRIPCIÓN/i).fill(desc);
    await page.locator('#evidence-file').setInputFiles(evidencePdfUpload());
    await expect(page.getByText('evidencia-e2e.pdf')).toBeVisible();

    await expect(page).toHaveURL(/\/evidencias\/cargar/);
    await page.waitForFunction(
      (key) => localStorage.getItem(key) !== null,
      'sigesa_auth_session',
    );

    const uploadDone = page.waitForResponse(
      (r) =>
        /\/api\/v1\/indicators\/[0-9a-f-]+\/evidences/i.test(r.url()) &&
        r.request().method() === 'POST',
      { timeout: 90_000 },
    );
    await page.getByRole('button', { name: 'Subir evidencia' }).click();
    const response = await uploadDone;
    const responseBody = await response.text();
    const authHeader = response.request().headers()['authorization'];
    expect(
      response.ok(),
      `Upload HTTP ${response.status()} (auth sent: ${Boolean(authHeader)}): ${responseBody.slice(0, 500)}`,
    ).toBeTruthy();
    await expect(page).not.toHaveURL(/\/login/);
    await expect(page.getByText('Carga exitosa')).toBeVisible();
  });
});
