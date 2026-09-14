import { test, expect } from '@playwright/test';

/**
 * Frontend Integration Tests: Authentication UI Flow
 * Target File: frontend/tests/integration/auth-flow.integration.spec.ts
 *
 * Verifies UI integration with backend API contracts, asynchronous states (loading, error, success),
 * and payload parsing using Playwright network interception (page.route).
 */

test.describe('Integration Test - Auth UI Flow & API Interception', () => {
  test.beforeEach(async ({ page }) => {
    // Clear storage before each test for test isolation
    await page.goto('/login');
    await page.evaluate(() => localStorage.clear());
  });

  test('AAA - Login Success: intercepts POST /api/v1/auth/login -> parses token & redirects', async ({ page }) => {
    // ARRANGE: Intercept API endpoint and return successful auth payload
    let capturedRequestBody: any = null;

    await page.route('**/api/v1/auth/login', async (route) => {
      capturedRequestBody = JSON.parse(route.request().postData() || '{}');
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          accessToken: 'mock-jwt-token-123456',
          tokenType: 'Bearer',
          role: 'JD',
          email: 'jd@umss.edu.bo',
          userId: 'a0000000-0000-4000-8000-000000000001',
        }),
      });
    });

    // ACT: Fill login form and click submit
    await page.getByLabel(/Correo Institucional/i).fill('jd@umss.edu.bo');
    await page.getByRole('textbox', { name: /Contraseña/i }).fill('JefeDemo2026!');
    await page.getByRole('button', { name: /Iniciar sesión/i }).click();

    // ASSERT: Verify request payload sent to backend
    expect(capturedRequestBody).toEqual({
      email: 'jd@umss.edu.bo',
      password: 'JefeDemo2026!',
    });

    // ASSERT UI STATE: Navigation to protected dashboard/users route upon success
    await expect(page).toHaveURL(/\/admin\/users/);
  });

  test('AAA - Async Loading State: verifies submit button loading state during pending API request', async ({ page }) => {
    // ARRANGE: Delay API response by 1000ms to test loading state
    await page.route('**/api/v1/auth/login', async (route) => {
      await page.waitForTimeout(1000);
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          accessToken: 'mock-token',
          role: 'JD',
          email: 'jd@umss.edu.bo',
        }),
      });
    });

    // ACT: Submit credentials
    await page.getByLabel(/Correo Institucional/i).fill('jd@umss.edu.bo');
    await page.getByRole('textbox', { name: /Contraseña/i }).fill('JefeDemo2026!');
    await page.getByRole('button', { name: /Iniciar sesión/i }).click();

    // ASSERT: Verify loading state (disabled button / spinner) while request is pending
    const loginButton = page.getByRole('button', { name: /Iniciar sesión|Cargando/i });
    await expect(loginButton).toBeDisabled();
  });

  test('AAA - Login Failure (401): parses error response payload and displays alert banner', async ({ page }) => {
    // ARRANGE: Intercept API endpoint to return 401 Unauthorized error response
    await page.route('**/api/v1/auth/login', async (route) => {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({
          error: 'AUTH_INVALID_CREDENTIALS',
          message: 'Credenciales inválidas',
        }),
      });
    });

    // ACT: Fill form with invalid credentials
    await page.getByLabel(/Correo Institucional/i).fill('invalid@umss.edu.bo');
    await page.getByRole('textbox', { name: /Contraseña/i }).fill('wrongpass');
    await page.getByRole('button', { name: /Iniciar sesión/i }).click();

    // ASSERT: Stays on login page and displays error alert with parsed message
    await expect(page).toHaveURL(/\/login/);
    const alertBanner = page.getByRole('alert');
    await expect(alertBanner).toBeVisible();
    await expect(alertBanner).toContainText(/Credenciales inválidas/i);
  });
});
