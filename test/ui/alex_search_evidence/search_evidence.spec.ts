import { test, expect } from '@playwright/test';

test.describe('Search Evidences E2E Tests - Alex', () => {
  const SEARCH_ENDPOINT = '/evidencias/buscar';
  const API_SEARCH_URL = '**/api/v1/evidences/search*';

  // Mock payload data for consistent E2E test assertions
  const mockSearchResults = {
    items: [
      {
        evidenceId: 'ev-101',
        subphaseId: 'sub-01',
        subphaseName: 'Recopilación de Información',
        phaseId: 'phase-01',
        phaseName: 'Fase 1: Autoevaluación',
        processId: 'proc-01',
        indicatorId: 'ind-01',
        indicatorCode: 'IND-1.1',
        indicatorTitle: 'Plan de Estudios',
        version: 1,
        description: 'Informe de autoevaluación anual 2025',
        originalFilename: 'informe_autoevaluacion.pdf',
        uploadedAt: '2025-06-15T10:00:00Z',
        uploadedBy: 'coordinador@umss.edu.bo',
        blobAvailable: true,
      },
      {
        evidenceId: 'ev-102',
        subphaseId: 'sub-02',
        subphaseName: 'Verificación Documental',
        phaseId: 'phase-01',
        phaseName: 'Fase 1: Autoevaluación',
        processId: 'proc-01',
        indicatorId: 'ind-02',
        indicatorCode: 'IND-1.2',
        indicatorTitle: 'Reglamento Docente',
        version: 2,
        description: 'Reglamento de docencia actualizado',
        originalFilename: 'reglamento_docente.pdf',
        uploadedAt: '2025-07-20T14:30:00Z',
        uploadedBy: 'tecnico@umss.edu.bo',
        blobAvailable: true,
      },
    ],
    total: 2,
    page: 0,
    size: 20,
  };

  test.beforeEach(async ({ page }) => {
    // Setup authenticated session state in localStorage before each test
    await page.addInitScript(() => {
      window.localStorage.setItem(
        'sigesa_auth_session',
        JSON.stringify({
          accessToken: 'mock-e2e-token',
          expiresIn: 3600,
          role: 'CC',
          programScope: ['prog-01'],
          expiresAt: Date.now() + 3600000,
        })
      );
    });
  });

  test('should navigate to search evidences endpoint and display search interface', async ({ page }) => {
    await page.goto(SEARCH_ENDPOINT);

    // Verify page URL matches search evidence endpoint
    await expect(page).toHaveURL(new RegExp(SEARCH_ENDPOINT));

    // Verify search input field is visible
    const searchInput = page.locator('input[type="search"], input[name="q"], #evidence-search-q').first();
    await expect(searchInput).toBeVisible();

    // Verify search button is visible
    const searchButton = page.getByRole('button', { name: /buscar/i });
    await expect(searchButton).toBeVisible();
  });

  test('should execute evidence search when typing query and submitting form', async ({ page }) => {
    let interceptedQuery = '';

    await page.route(API_SEARCH_URL, async (route) => {
      const url = new URL(route.request().url());
      interceptedQuery = url.searchParams.get('q') || '';
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockSearchResults }),
      });
    });

    await page.goto(SEARCH_ENDPOINT);

    const searchInput = page.locator('input[type="search"], input[name="q"], #evidence-search-q').first();
    await searchInput.fill('informe');

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await searchButton.click();

    // Assert search query was passed correctly to the backend endpoint
    expect(interceptedQuery).toBe('informe');

    // Assert search results rendered correctly on screen
    await expect(page.getByText('informe_autoevaluacion.pdf')).toBeVisible();
    await expect(page.getByText('Informe de autoevaluación anual 2025')).toBeVisible();
    await expect(page.getByText('IND-1.1')).toBeVisible();
  });

  test('should apply phase and subphase filters in search request', async ({ page }) => {
    await page.route(API_SEARCH_URL, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockSearchResults }),
      });
    });

    await page.goto(SEARCH_ENDPOINT);

    const phaseSelect = page.locator('select#evidence-search-phase, select[name="phaseId"]').first();
    if (await phaseSelect.isVisible()) {
      const options = await phaseSelect.locator('option').all();
      if (options.length > 1) {
        const optionValue = await options[1].getAttribute('value');
        if (optionValue) {
          await phaseSelect.selectOption(optionValue);
        }
      }
    }

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await searchButton.click();

    await page.waitForLoadState('networkidle');
  });

  test('should handle empty search results gracefully', async ({ page }) => {
    await page.route(API_SEARCH_URL, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          data: {
            items: [],
            total: 0,
            page: 0,
            size: 20,
          },
        }),
      });
    });

    await page.goto(SEARCH_ENDPOINT);

    const searchInput = page.locator('input[type="search"], input[name="q"], #evidence-search-q').first();
    await searchInput.fill('nonexistent_evidence_terms');

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await searchButton.click();

    // Assert empty results message is visible
    await expect(page.getByText(/no se encontraron/i)).toBeVisible();
  });

  test('should clear search filters when clicking reset button', async ({ page }) => {
    await page.route(API_SEARCH_URL, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockSearchResults }),
      });
    });

    await page.goto(SEARCH_ENDPOINT);

    const searchInput = page.locator('input[type="search"], input[name="q"], #evidence-search-q').first();
    await searchInput.fill('reglamento');

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await searchButton.click();

    const resetButton = page.getByRole('button', { name: /limpiar/i });
    await expect(resetButton).toBeVisible();

    await resetButton.click();

    // Verify search input value cleared
    await expect(searchInput).toHaveValue('');
  });
});
