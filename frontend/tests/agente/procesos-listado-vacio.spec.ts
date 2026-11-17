// spec: specs/plan_procesos_dimension_jd.md — caso 1.3
import { test } from '@playwright/test';

test.describe('Procesos — listado vacío', () => {
  test.skip(true, 'Seed dev no incluye rol sin procesos en alcance; ampliar seed o usar EE');
});
