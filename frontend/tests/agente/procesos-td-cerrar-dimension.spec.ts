// spec: specs/plan_aprobacion_td.md — caso 1.2
import { test } from '@playwright/test';

test.describe('Revisión TD — cerrar dimensión', () => {
  test.skip(
    true,
    'Requiere subárbol completo con indicadores APROBADO; ejecutar manualmente cuando el seed lo permita',
  );
});
