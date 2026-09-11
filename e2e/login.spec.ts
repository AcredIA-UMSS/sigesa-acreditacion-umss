import { expect, test } from '@playwright/test';
import { TestId } from './fixtures/testids';
import { stubBackend } from './helpers/apiStub';
import { loginViaForm, SeedUsers } from './helpers/auth';

test.beforeEach(async ({ page }) => {
  await stubBackend(page);
});

test('login expone anclas de formulario', async ({ page }) => {
  await page.goto('/login');

  await expect(page.getByTestId(TestId.loginPage)).toBeVisible();
  await expect(page.getByTestId(TestId.loginForm)).toBeVisible();
  await expect(page.getByTestId(TestId.loginEmail)).toBeVisible();
  await expect(page.getByTestId(TestId.loginPassword)).toBeVisible();
  await expect(page.getByTestId(TestId.loginSubmit)).toBeVisible();
});

test('login-toggle-password cambia el tipo del input', async ({ page }) => {
  await page.goto('/login');
  const password = page.getByTestId(TestId.loginPassword);
  await expect(password).toHaveAttribute('type', 'password');
  await page.getByRole('button', { name: 'Mostrar caracteres' }).click();
  await expect(password).toHaveAttribute('type', 'text');
});

test('JD inicia sesión y llega a administración de usuarios', async ({ page }) => {
  await loginViaForm(page, SeedUsers.jd);
  await expect(page.getByTestId(TestId.usersAdminPage)).toBeVisible();
  await expect(page.getByTestId(TestId.sidebar)).toBeVisible();
});

test('CC inicia sesión y llega al dashboard', async ({ page }) => {
  await loginViaForm(page, SeedUsers.cc);
  await expect(page.getByTestId(TestId.dashboardPage)).toBeVisible();
  await expect(page.getByTestId(TestId.dashboardMain)).toBeVisible();
});
