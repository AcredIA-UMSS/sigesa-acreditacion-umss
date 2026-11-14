// Integration: Notifications bell UI + Orval endpoints (mocked network)
import { test, expect } from '@playwright/test';

const JD_USER_ID = 'a0000000-0000-4000-8000-000000000001';
const PROCESS_CLOSED = '950e8400-e29b-41d4-a716-446655440021';
const NOTIF_ID = 'b0000000-0000-4000-8000-000000000001';

test.describe('Integration — Notification bell (mocked API)', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('**/api/v1/auth/login', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          accessToken: 'mock-jwt-notif-e2e',
          tokenType: 'Bearer',
          expiresIn: 3600,
          role: 'JD',
          email: 'jd@umss.edu.bo',
          userId: JD_USER_ID,
          programScope: [],
        }),
      });
    });

    await page.route('**/api/v1/notifications/me**', async (route) => {
      if (route.request().method() !== 'GET') {
        await route.continue();
        return;
      }
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [
            {
              id: NOTIF_ID,
              eventType: 'ProcessClosed',
              title: 'Proceso cerrado',
              body: 'Ingeniería de Sistemas cerró el proceso de acreditación.',
              deepLink: `/executive/semaphore?processId=${PROCESS_CLOSED}`,
              programId: '550e8400-e29b-41d4-a716-446655440000',
              processId: PROCESS_CLOSED,
              readAt: null,
              createdAt: '2026-11-12T12:00:00',
            },
            {
              id: 'b0000000-0000-4000-8000-000000000002',
              eventType: 'SystemNotice',
              title: 'Bienvenido a SIGESA',
              body: 'Revise el semáforo institucional.',
              deepLink: '/executive/semaphore',
              programId: null,
              processId: null,
              readAt: null,
              createdAt: '2026-11-11T12:00:00',
            },
          ],
          totalElements: 2,
          totalPages: 1,
          size: 15,
          number: 0,
        }),
      });
    });

    await page.route(`**/api/v1/notifications/${NOTIF_ID}/read`, async (route) => {
      await route.fulfill({ status: 204, body: '' });
    });

    await page.route('**/api/v1/notifications/me/read-all', async (route) => {
      await route.fulfill({ status: 204, body: '' });
    });

    // Soft-stub dashboard calls so /admin/users or /executive/semaphore do not 401.
    await page.route('**/api/v1/dashboards/**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          userId: JD_USER_ID,
          grantedPermissions: ['READ_EXECUTIVE_DASHBOARD'],
          executiveSection: {
            totalProgramasEnAcreditacion: 1,
            porcentajeAvanceInstitucional: 40,
            criticalObservations: 0,
            alertPrograms: 0,
            semaforoProgramas: [
              {
                programaId: '550e8400-e29b-41d4-a716-446655440000',
                nombre: 'Ingeniería de Sistemas',
                facultad: 'FCT',
                estado: 'AMARILLO',
                avance: 40,
                processId: '950e8400-e29b-41d4-a716-446655440020',
              },
            ],
          },
        }),
      });
    });

    await page.route('**/api/v1/admin/users**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });

    await page.route('**/api/v1/processes**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: '950e8400-e29b-41d4-a716-446655440020',
            careerId: '550e8400-e29b-41d4-a716-446655440000',
            name: 'CEUB Sistemas',
            status: 'ACTIVE',
          },
        ]),
      });
    });
  });

  test('login JD → popover muestra ProcessClosed → click navega a semáforo', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel(/Correo Institucional/i).fill('jd@umss.edu.bo');
    await page.getByRole('textbox', { name: /Contraseña/i }).fill('JefeDemo2026!');
    await page.getByRole('button', { name: /Iniciar sesión/i }).click();
    await expect(page).toHaveURL(/\/admin\/users/);

    const bell = page.getByRole('button', { name: /Notificaciones/i }).first();
    await expect(bell).toBeVisible();
    await bell.click();

    const dialog = page.getByRole('dialog', { name: /Notificaciones recientes/i });
    await expect(dialog.getByText('Proceso cerrado')).toBeVisible();
    await dialog.getByRole('button', { name: /Proceso cerrado/i }).click();
    await expect(page).toHaveURL(/\/executive\/semaphore/);
    await expect(page.getByRole('heading', { name: /Semáforo institucional/i })).toBeVisible();
  });

  test('desde semáforo, click en carrera navega a /procesos/{processId}', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel(/Correo Institucional/i).fill('jd@umss.edu.bo');
    await page.getByRole('textbox', { name: /Contraseña/i }).fill('JefeDemo2026!');
    await page.getByRole('button', { name: /Iniciar sesión/i }).click();
    await expect(page).toHaveURL(/\/admin\/users/);

    await page.goto('/executive/semaphore');
    await expect(page.getByRole('heading', { name: /Semáforo institucional/i })).toBeVisible();
    await page.getByRole('button', { name: /En alerta/i }).click();
    await expect(page).toHaveURL(/\/procesos\/950e8400-e29b-41d4-a716-446655440020/);
  });
});
