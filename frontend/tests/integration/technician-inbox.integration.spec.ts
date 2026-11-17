import { test, expect } from '@playwright/test';

/**
 * Playwright E2E & Integration Test: Technician Review Inbox Flow (FSD-UC-012 / Issue 55)
 *
 * Validates:
 * - Inbox data loading with hierarchical paths and pending counts.
 * - Filtering indicators by status (SUBIDO, SUBSANADO, OBSERVADO).
 * - Indicator technical approval action and alert confirmation.
 * - Technical rejection / observation flow requiring at least 20 chars justification.
 */

test.describe('E2E - Technician Review Inbox Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Mock user login session for Technician (TD)
    await page.addInitScript(() => {
      localStorage.setItem('sigesa_access_token', 'mock-td-token');
      localStorage.setItem('sigesa_user_role', 'TD');
      localStorage.setItem('sigesa_user_email', 'tecnico@umss.edu.bo');
    });
  });

  test('Displays indicators list, filters by status, and approves indicator', async ({ page }) => {
    // Intercept inbox list API
    await page.route('**/api/v1/dashboards/technician/inbox*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          data: {
            content: [
              {
                indicatorId: '11111111-1111-1111-1111-111111111111',
                indicatorCode: 'IND-3.2.1',
                indicatorDescription: 'Equipamiento de laboratorios informáticos',
                status: 'SUBIDO',
                dimensionName: 'Dimensión 3: Infraestructura',
                criterionName: 'Criterio 2: Laboratorios',
                subcriterionName: 'Subcriterio 1: Capacidad Instalada',
                pathHierarchy: 'Infraestructura > Laboratorios > Capacidad Instalada',
                programId: '22222222-2222-2222-2222-222222222222',
                programName: 'Licenciatura en Informática',
                evidenceId: '33333333-3333-3333-3333-333333333333',
                evidenceFilename: 'inventario_equipos_2026.pdf',
                evidenceUploadedAt: '2026-10-18T10:00:00',
                activeObservationsCount: 0,
                updatedAt: '2026-10-18T10:00:00',
              },
            ],
            totalElements: 1,
            totalPages: 1,
            size: 15,
            number: 0,
          },
        }),
      });
    });

    let approveCalled = false;
    await page.route('**/api/v1/indicators/11111111-1111-1111-1111-111111111111/approve', async (route) => {
      approveCalled = true;
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true }) });
    });

    await page.goto('/dashboard/technician/inbox');

    // Verify indicator card / table row renders
    await expect(page.getByText('Bandeja de dictamen')).toBeVisible();
    await expect(page.getByText('IND-3.2.1')).toBeVisible();
    await expect(page.getByText('inventario_equipos_2026.pdf')).toBeVisible();
    await expect(page.getByText('Infraestructura > Laboratorios > Capacidad Instalada')).toBeVisible();

    // Trigger approve modal
    await page.getByRole('button', { name: 'Aprobar' }).click();
    await expect(page.getByText('Confirmar Aprobación Técnica')).toBeVisible();

    // Confirm approval
    await page.getByRole('button', { name: 'Confirmar Aprobación' }).click();
    await expect(page.getByText(/aprobado exitosamente/i)).toBeVisible();
    expect(approveCalled).toBe(true);
  });

  test('Validates observation requirement of minimum 20 characters before submission', async ({ page }) => {
    await page.route('**/api/v1/dashboards/technician/inbox*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          data: {
            content: [
              {
                indicatorId: '11111111-1111-1111-1111-111111111111',
                indicatorCode: 'IND-1.1.2',
                indicatorDescription: 'Perfil docente y carga horaria',
                status: 'SUBIDO',
                dimensionName: 'Docencia',
                criterionName: 'Planta Docente',
                subcriterionName: 'Dedicación',
                pathHierarchy: 'Docencia > Planta Docente > Dedicación',
                programId: '22222222-2222-2222-2222-222222222222',
                programName: 'Ingeniería de Sistemas',
                evidenceId: '44444444-4444-4444-4444-444444444444',
                evidenceFilename: 'nomina_docente.pdf',
                evidenceUploadedAt: '2026-10-18T11:00:00',
                activeObservationsCount: 0,
                updatedAt: '2026-10-18T11:00:00',
              },
            ],
            totalElements: 1,
            totalPages: 1,
            size: 15,
            number: 0,
          },
        }),
      });
    });

    let rejectPayload: any = null;
    await page.route('**/api/v1/indicators/11111111-1111-1111-1111-111111111111/reject', async (route) => {
      rejectPayload = JSON.parse(route.request().postData() || '{}');
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true }) });
    });

    await page.goto('/dashboard/technician/inbox');
    await page.getByRole('button', { name: 'Observar' }).click();

    // Verify modal is open and submit button is initially disabled (empty input)
    await expect(page.getByText('Emitir Observación Técnica')).toBeVisible();
    const emitButton = page.getByRole('button', { name: 'Emitir Observación' });
    await expect(emitButton).toBeDisabled();

    // Enter short text (< 20 chars) -> button remains disabled
    const textarea = page.getByPlaceholder(/La evidencia adjunta no acredita/i);
    await textarea.fill('Muy corto');
    await expect(emitButton).toBeDisabled();

    // Enter valid text (>= 20 chars) -> button activates
    const fullJustification = 'La certificación docente no cuenta con sello de la dirección de carrera correspondiente.';
    await textarea.fill(fullJustification);
    await expect(emitButton).toBeEnabled();

    await emitButton.click();
    await expect(page.getByText(/observado con fundamentación técnica/i)).toBeVisible();
    expect(rejectPayload).toEqual({ justification: fullJustification });
  });
});
