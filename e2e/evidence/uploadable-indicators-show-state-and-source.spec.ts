// spec: e2e/specs/gestion-evidencias.plan.md · 3.1 (P05 corregido → aceptado al plan)
// E2E: getByLabel/getByRole · sin waitForTimeout · estado/fuente en select, no copy del modelo · independiente · datos locales
import { expect, test } from '@playwright/test';
import { stubBackend } from '../helpers/apiStub';

test('uploadable-indicators-show-state-and-source', async ({ page }) => {
  const cc = { email: 'cc-estado@umss.edu.bo', password: 'CoordEstado2026!' };
  const indicadores = [
    {
      indicatorId: 'e2e-ind-pendiente',
      code: 'E2E-P',
      title: 'Indicador de prueba pendiente',
      criterionId: 'e2e-crit-p',
      criterionCode: 'E2E-C-P',
      criterionTitle: 'Criterio pendiente',
      currentState: 'PENDIENTE',
    },
    {
      indicatorId: 'e2e-ind-observado',
      code: 'E2E-O',
      title: 'Indicador de prueba observado',
      criterionId: 'e2e-crit-o',
      criterionCode: 'E2E-C-O',
      criterionTitle: 'Criterio observado',
      currentState: 'OBSERVADO',
    },
  ];

  await stubBackend(page, { uploadable: indicadores });
  await page.goto('/login');

  await page.getByLabel('Correo Institucional').fill(cc.email);
  await page.getByLabel('Contraseña').fill(cc.password);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
  await expect(page.getByTestId('dashboard-page')).toBeVisible();

  await page.goto('/evidencias/cargar');

  const indicador = page.getByLabel('Indicador');
  await expect(indicador).toBeVisible();
  await expect(indicador.getByRole('option', { name: /E2E-P .*PENDIENTE/ })).toBeAttached();
  await expect(indicador.getByRole('option', { name: /E2E-O .*OBSERVADO/ })).toBeAttached();

  await indicador.selectOption('e2e-ind-pendiente');
  await expect(page.getByLabel('Criterio')).toHaveValue('e2e-crit-p');
});
