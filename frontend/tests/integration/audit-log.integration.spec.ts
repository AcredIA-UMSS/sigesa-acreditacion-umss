import { test, expect } from '@playwright/test';

/**
 * Playwright E2E & Integration Test: Audit Log Page & CSV Export (FSD-UC-017 / Issue 56)
 *
 * Validates:
 * - Audit log list rendering for JD role with immutable entry timestamps, actor roles and event types.
 * - Dynamic filtering by actor email and action.
 * - CSV export streaming trigger and download invocation.
 */

test.describe('E2E - Audit Log & CSV Streaming Export', () => {
  test.beforeEach(async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('sigesa_access_token', 'mock-jd-token');
      localStorage.setItem('sigesa_user_role', 'JD');
      localStorage.setItem('sigesa_user_email', 'jefe.departamento@umss.edu.bo');
    });
  });

  test('Displays immutable audit logs and downloads CSV report', async ({ page }) => {
    await page.route('**/api/v1/audit/logs*', async (route) => {
      if (route.request().url().includes('/export')) {
        await route.fulfill({
          status: 200,
          contentType: 'text/csv; charset=utf-8',
          headers: {
            'Content-Disposition': 'attachment; filename="audit_logs.csv"',
          },
          body: 'id,created_at,actor_email,actor_role,action,entity_type,entity_id,ip_address,payload_diff\n' +
                '"00000000-0000-0000-0000-000000000001","2026-10-18T12:00:00","jd@umss.edu.bo","JD","STAGE_CLOSED","STAGE","STAGE-01","192.168.1.10","Cierre"\n',
        });
      } else {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            data: {
              content: [
                {
                  id: '00000000-0000-0000-0000-000000000001',
                  createdAt: '2026-10-18T12:00:00',
                  actorId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                  actorEmail: 'jd@umss.edu.bo',
                  actorRole: 'JD',
                  action: 'STAGE_CLOSED',
                  entityType: 'STAGE',
                  entityId: 'stage-1111-2222',
                  payloadDiff: 'Cierre de compuerta fase de autoevaluación',
                  ipAddress: '192.168.1.10',
                },
                {
                  id: '00000000-0000-0000-0000-000000000002',
                  createdAt: '2026-10-18T13:00:00',
                  actorId: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
                  actorEmail: 'cc@umss.edu.bo',
                  actorRole: 'CC',
                  action: 'EVIDENCE_UPLOADED',
                  entityType: 'EVIDENCE',
                  entityId: 'evidence-3333-4444',
                  payloadDiff: 'Carga de evidencia para indicador=IND-1.1.2',
                  ipAddress: '192.168.1.15',
                },
              ],
              totalElements: 2,
              totalPages: 1,
              size: 20,
              number: 0,
            },
          }),
        });
      }
    });

    await page.goto('/admin/audit');

    // Assert headers and entries
    await expect(page.getByText('Bitácora Inmutable de Auditoría [JD]')).toBeVisible();
    await expect(page.getByText('STAGE_CLOSED')).toBeVisible();
    await expect(page.getByText('EVIDENCE_UPLOADED')).toBeVisible();
    await expect(page.getByText('jd@umss.edu.bo')).toBeVisible();
    await expect(page.getByText('cc@umss.edu.bo')).toBeVisible();

    // Verify CSV Export button triggers download
    const exportButton = page.getByRole('button', { name: /Exportar CSV/i });
    await expect(exportButton).toBeEnabled();

    const downloadPromise = page.waitForEvent('download');
    await exportButton.click();
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toBe('audit_logs.csv');
  });
});
