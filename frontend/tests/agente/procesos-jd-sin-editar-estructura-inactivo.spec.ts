// spec: specs/plan_procesos_dimension_jd.md — caso 1.2
import { test, expect } from '@playwright/test';
import { loginJd } from './helpers/auth';

const PROCESS_CLOSED_ID = '950e8400-e29b-41d4-a716-446655440021';

test.describe('Procesos — JD sin editar estructura inactivo', () => {
  test('JD no ve editar estructura si proceso no ACTIVE', async ({ page }) => {
    await loginJd(page);
    await page.goto(`/procesos/${PROCESS_CLOSED_ID}`);
    const notFound = page.getByText(/no encontrado|404|Proceso no/i);
    if (await notFound.isVisible().catch(() => false)) {
      test.skip(true, 'Proceso CLOSED no presente en seed local');
    }
    await expect(page.getByRole('button', { name: 'Editar estructura normativa' })).toHaveCount(0);
  });
});
