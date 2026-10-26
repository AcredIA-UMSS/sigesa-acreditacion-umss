// spec: FEAT-11 / FSD-UC-032 — Exportación Asíncrona del Dossier Digital (ZIP)
import { test, expect } from '@playwright/test';
import {
  blockAssistantApi,
  loginCc,
  loginJd,
  loginTd,
  PROCESS_INF_SIS_ACTIVE_ID,
  stubDashboardSummary,
} from '../agente/helpers/auth';

const PROCESS_ID = PROCESS_INF_SIS_ACTIVE_ID;

async function stubProcessDetail(page: import('@playwright/test').Page) {
  await page.route(`**/api/v1/processes/${PROCESS_ID}`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        id: PROCESS_ID,
        careerId: '550e8400-e29b-41d4-a716-446655440000',
        careerCode: 'INF-SIS',
        careerName: 'Ingeniería de Sistemas',
        templateId: '850e8400-e29b-41d4-a716-446655440010',
        templateName: 'Modelo CEUB 2026',
        templateType: 'CEUB',
        evaluatorModel: 'CEUB',
        status: 'ACTIVE',
        operationalMode: 'ACTIVE',
        startDate: '2026-01-15T08:00:00',
        level1Nodes: [],
      }),
    });
  });
}

async function stubDossierExportFlow(page: import('@playwright/test').Page, options?: {
  fail?: boolean;
  downloadFilename?: string;
}) {
  const jobId = '11111111-2222-3333-4444-555555555555';

  await page.route(`**/api/v1/processes/${PROCESS_ID}/dossier/export`, async (route) => {
    if (route.request().method() === 'POST') {
      await route.fulfill({
        status: 202,
        contentType: 'application/json',
        body: JSON.stringify({
          jobId,
          status: 'PENDING',
          message: 'Dossier export has started in the background.',
          statusUrl: `/api/v1/processes/${PROCESS_ID}/dossier/export-jobs/${jobId}`,
        }),
      });
    } else {
      await route.continue();
    }
  });

  let pollingCall = 0;
  await page.route(`**/api/v1/processes/${PROCESS_ID}/dossier/export-jobs/${jobId}`, async (route) => {
    pollingCall += 1;
    if (options?.fail) {
      // First poll: PROCESSING (so disabled/progress state is observable),
      // subsequent polls: FAILED
      if (pollingCall === 1) {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            jobId,
            status: 'PROCESSING',
            progressPercentage: 50,
            downloadUrl: null,
            errorMessage: null,
          }),
        });
      } else {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            jobId,
            status: 'FAILED',
            progressPercentage: 50,
            downloadUrl: null,
            errorMessage: 'Fallo al comprimir archivos en S3.',
          }),
        });
      }
      return;
    }
    // First poll: PROCESSING, second poll: COMPLETED
    if (pollingCall === 1) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          jobId,
          status: 'PROCESSING',
          progressPercentage: 50,
          downloadUrl: null,
          errorMessage: null,
        }),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          jobId,
          status: 'COMPLETED',
          progressPercentage: 100,
          downloadUrl: `/api/v1/processes/${PROCESS_ID}/dossier/export-jobs/${jobId}/download`,
          errorMessage: null,
        }),
      });
    }
  });

  await page.route(`**/api/v1/processes/${PROCESS_ID}/dossier/export-jobs/${jobId}/download`, async (route) => {
    const filename = options?.downloadFilename ?? 'Dossier_INF-SIS.zip';
    await route.fulfill({
      status: 200,
      contentType: 'application/zip',
      headers: {
        'Content-Disposition': `attachment; filename="${filename}"`,
      },
      body: Buffer.from('PK\x03\x04', 'hex'), // minimal ZIP signature
    });
  });
}

test.describe('E2E - FEAT-11 Dossier Digital Export (ZIP) - role visibility', () => {
  const roles = [
    { name: 'JD', login: loginJd },
    { name: 'TD', login: loginTd },
    { name: 'CC', login: loginCc },
  ];

  for (const { name, login } of roles) {
    test(`Muestra el botón Exportar Dossier para rol ${name}`, async ({ page }) => {
      test.setTimeout(120_000);
      await blockAssistantApi(page);
      await stubDashboardSummary(page);
      await stubProcessDetail(page);
      await login(page);
      await page.goto(`/procesos/${PROCESS_ID}`);

      await page.waitForResponse(
        (r) => r.url().includes(`/api/v1/processes/${PROCESS_ID}`) && r.request().method() === 'GET' && r.ok(),
        { timeout: 30_000 },
      );

      const exportButton = page.getByRole('button', { name: /Exportar Dossier Completo \(ZIP\)/i });
      await expect(exportButton).toBeVisible();
      await expect(exportButton).toBeEnabled();
    });
  }

  test('Usuario no autenticado es redirigido a /login (no ve el botón)', async ({ page }) => {
    test.setTimeout(120_000);
    await stubProcessDetail(page);
    await page.goto(`/login`);
    await page.evaluate(() => {
      localStorage.removeItem('sigesa_auth_session');
    });
    await page.goto(`/procesos/${PROCESS_ID}`);
    await expect(page).toHaveURL(/login/);
    await expect(page.getByRole('button', { name: /Exportar Dossier Completo \(ZIP\)/i })).not.toBeVisible();
  });
});

test.describe('E2E - FEAT-11 Dossier Digital Export (ZIP) - full flow', () => {
  test('JD exporta el dossier y descarga el ZIP correctamente', async ({ page }) => {
    test.setTimeout(120_000);
    await blockAssistantApi(page);
    await stubDashboardSummary(page);
    await stubProcessDetail(page);
    await stubDossierExportFlow(page);
    await loginJd(page);
    await page.goto(`/procesos/${PROCESS_ID}`);

    await page.waitForResponse(
      (r) => r.url().includes(`/api/v1/processes/${PROCESS_ID}`) && r.request().method() === 'GET' && r.ok(),
      { timeout: 30_000 },
    );

    const exportButton = page.getByRole('button', { name: /Exportar Dossier Completo \(ZIP\)/i });
    await expect(exportButton).toBeVisible();

    const downloadPromise = page.waitForEvent('download');
    await exportButton.click();
    const download = await downloadPromise;

    expect(download.suggestedFilename()).toContain('.zip');
    // Wait for download to finish and verify it is a zip
    const path = await download.path();
    expect(path).toBeTruthy();
  });

  test('Muestra progreso durante la exportación y error al fallar', async ({ page }) => {
    test.setTimeout(120_000);
    await blockAssistantApi(page);
    await stubDashboardSummary(page);
    await stubProcessDetail(page);
    await stubDossierExportFlow(page, { fail: true });
    await loginJd(page);
    await page.goto(`/procesos/${PROCESS_ID}`);

    await page.waitForResponse(
      (r) => r.url().includes(`/api/v1/processes/${PROCESS_ID}`) && r.request().method() === 'GET' && r.ok(),
      { timeout: 30_000 },
    );

    const exportButton = page.getByRole('button', { name: /Exportar Dossier Completo \(ZIP\)/i });
    await exportButton.click();

    // Mientras se exporta, debe mostrarse progreso (isPending === true)
    await expect(page.getByText(/Exportando/i)).toBeVisible({ timeout: 10_000 });

    // Al fallar, debe mostrar el mensaje de error y habilitar el botón de nuevo
    await expect(page.getByText(/Fallo al comprimir archivos en S3/i)).toBeVisible();
    await expect(exportButton).toBeEnabled();
  });
});
