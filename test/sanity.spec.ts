import { test, expect } from '@playwright/test';

test.describe('Frontend Root Verification', () => {
  test('should load the root frontend page and verify the main heading is visible', async ({ page }) => {
    // Navigate to the root URL (configured as baseURL in playwright.config.ts)
    await page.goto('/');

    // Check that the main heading "Accede a SIGESA" on the login page is visible
    const heading = page.getByRole('heading', { name: /Accede a SIGESA/i });
    await expect(heading).toBeVisible();

    // Check that the brand label "SIGESA" is also visible
    const brandLabel = page.getByText('SIGESA').first();
    await expect(brandLabel).toBeVisible();
  });
});
