import { test, expect } from '@playwright/test';

test.describe('E2E / Integration Test — Public Portal & News Flow (FSD-UC-016 / FSD-UC-035)', () => {
  test('Anonymous visitor can search accreditation status and download official certificate', async ({ page }) => {
    // Intercept public programs list
    await page.route('**/api/v1/public/programs', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { slug: 'inf-sis', name: 'Ingeniería de Sistemas' },
          { slug: 'med', name: 'Medicina' },
        ]),
      });
    });

    // Intercept program detail
    await page.route('**/api/v1/public/programs/inf-sis', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          slug: 'inf-sis',
          name: 'Ingeniería de Sistemas',
          accreditationStatus: 'ACREDITADA',
          validFrom: '2025-01-01',
          validUntil: '2029-12-31',
          certificateAvailable: true,
        }),
      });
    });

    // Intercept certificate download
    let certificateDownloaded = false;
    await page.route('**/api/v1/public/programs/inf-sis/certificate', async (route) => {
      certificateDownloaded = true;
      await route.fulfill({
        status: 200,
        contentType: 'application/pdf',
        headers: {
          'Content-Disposition': 'attachment; filename="certificado-inf-sis.pdf"',
        },
        body: '%PDF-1.4 Mock Certificate Content',
      });
    });

    // Navigate to public portal status page
    await page.goto('/public/status');

    // Verify header and published list
    await expect(page.getByRole('heading', { name: /Estado de acreditación/i })).toBeVisible();
    await expect(page.getByText('Ingeniería de Sistemas')).toBeVisible();

    // Search and select program
    const searchInput = page.getByPlaceholder(/Buscar carrera por nombre/i);
    await searchInput.fill('Sistemas');
    await page.getByRole('button', { name: /Ingeniería de Sistemas/i }).click();

    // Verify program status badge and validity period
    await expect(page.getByText('ACREDITADA')).toBeVisible();
    await expect(page.getByText(/2025/)).toBeVisible();
    await expect(page.getByText(/2029/)).toBeVisible();

    // Click download certificate button
    const downloadBtn = page.getByRole('button', { name: /Descargar certificado oficial/i });
    await expect(downloadBtn).toBeVisible();
    await downloadBtn.click();

    expect(certificateDownloaded).toBe(true);
  });

  test('Public news listing renders published articles and navigates to detail', async ({ page }) => {
    // Intercept news list
    await page.route('**/api/v1/public/news?*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [
            {
              id: '990e8400-e29b-41d4-a716-446655440001',
              title: 'Acreditación Internacional ARCU-SUR',
              slug: 'acreditacion-internacional-arcu-sur',
              category: 'LOGRO',
              summary: 'La carrera de Sistemas obtuvo la acreditación MERCOSUR.',
              publishedAt: '2026-11-01T08:00:00Z',
              featured: true,
            },
          ],
          totalElements: 1,
          totalPages: 1,
          size: 10,
          number: 0,
        }),
      });
    });

    // Intercept news article detail
    await page.route('**/api/v1/public/news/acreditacion-internacional-arcu-sur', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: '990e8400-e29b-41d4-a716-446655440001',
          title: 'Acreditación Internacional ARCU-SUR',
          slug: 'acreditacion-internacional-arcu-sur',
          category: 'LOGRO',
          summary: 'La carrera de Sistemas obtuvo la acreditación MERCOSUR.',
          body: '### Dictamen Favorable\n\nEl comité de pares evaluadores emitió dictamen de acreditación plena.',
          publishedAt: '2026-11-01T08:00:00Z',
          featured: true,
        }),
      });
    });

    // Navigate to public news page
    await page.goto('/public/news');

    // Verify news card
    await expect(page.getByText('Acreditación Internacional ARCU-SUR')).toBeVisible();
    await expect(page.getByText('Logro')).toBeVisible();

    // Click to open article detail
    await page.getByRole('link', { name: /Acreditación Internacional ARCU-SUR/i }).click();

    // Verify detail page content
    await expect(page).toHaveURL(/\/public\/news\/acreditacion-internacional-arcu-sur/);
    await expect(page.getByText('Dictamen Favorable')).toBeVisible();
  });
});
