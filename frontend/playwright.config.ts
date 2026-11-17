import { defineConfig, devices } from '@playwright/test';

/**
 * E2E SIGESA — React (Vite :5173) + Spring Boot (:8080).
 *
 * Con reuseExistingServer: true podés tener backend (Docker o mvn) y `pnpm dev` ya
 * levantados; Playwright no los reinicia.
 *
 * Traza en fallo: npx playwright show-trace test-results/.../trace.zip
 */
export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL ?? 'http://127.0.0.1:5173',
    trace: 'on-first-retry',
    headless: process.env.PW_HEADLESS !== 'false',
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        ...(process.env.CHROMIUM_PATH
          ? { launchOptions: { executablePath: process.env.CHROMIUM_PATH } }
          : {}),
      },
    },
  ],
  webServer: [
    ...(process.env.PW_SKIP_BACKEND === '1'
      ? []
      : [
          {
            command:
              '../backend/./mvnw -q spring-boot:run -Dspring-boot.run.profiles=dev',
            url: 'http://127.0.0.1:8080/v3/api-docs',
            reuseExistingServer: !process.env.CI,
            timeout: 180_000,
          },
        ]),
    {
      // strictPort: si :5173 está ocupado, falla en segundos (no muta a :5174 y cuelga el timeout).
      command: 'pnpm exec vite --host 127.0.0.1 --port 5173 --strictPort',
      url: 'http://127.0.0.1:5173',
      reuseExistingServer: !process.env.CI,
      timeout: 120_000,
    },
  ],
});
