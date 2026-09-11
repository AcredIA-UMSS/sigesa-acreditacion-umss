import { defineConfig, devices } from '@playwright/test';

const port = process.env.E2E_PORT ?? '5174';
const baseURL = process.env.E2E_BASE_URL ?? `http://localhost:${port}`;

/**
 * Config ESM mutable (defineConfig). Un `playwright.config.ts` vacío
 * exporta el namespace del módulo; Playwright 1.63 asigna `metadata` y falla.
 */
export default defineConfig({
  metadata: {},
  testDir: '../e2e',
  testMatch: '**/*.spec.ts',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  timeout: 45_000,
  expect: { timeout: 15_000 },
  outputDir: '../e2e/artifacts/test-results',
  reporter: process.env.CI
    ? [['github'], ['html', { open: 'never', outputFolder: '../e2e/artifacts/html-report' }]]
    : [['list'], ['html', { open: 'never', outputFolder: '../e2e/artifacts/html-report' }]],
  use: {
    baseURL,
    testIdAttribute: 'data-testid',
    trace: 'on',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  webServer: {
    command: `pnpm exec vite --port ${port} --strictPort`,
    url: baseURL,
    reuseExistingServer: process.env.E2E_REUSE === '1',
    timeout: 120_000,
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
});
