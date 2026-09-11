import { test } from '@playwright/test';
import { stubBackend } from './helpers/apiStub';

/**
 * Seed del Planner: sesión vacía en /login.
 * Cada escenario de `e2e/specs/gestion-evidencias.plan.md` parte de aquí.
 */
test('seed', async ({ page }) => {
  await stubBackend(page);
  await page.goto('/login');
});
