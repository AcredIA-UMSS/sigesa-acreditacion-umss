// spec: specs/plan_proceso_responsable_jd.md — caso 1.2
import { test, expect } from '@playwright/test';
import { loginJd } from './helpers/auth';

const PROCESS_CLOSED_ID = '950e8400-e29b-41d4-a716-446655440021';

test.describe('Proceso — JD sin gestionar responsable inactivo', () => {
  test('JD no ve asignar responsable si proceso no ACTIVE', async ({ page }) => {
    await loginJd(page);
    await page.goto(`/procesos/${PROCESS_CLOSED_ID}`);
    const notFound = page.getByText(/no encontrado|404|Proceso no/i);
    if (await notFound.isVisible().catch(() => false)) {
      test.skip(true, 'Proceso CLOSED no presente en seed local');
    }
    await expect(page.getByRole('button', { name: /Asignar responsable|Cambiar responsable/ })).toHaveCount(
      0,
    );
  });
});
