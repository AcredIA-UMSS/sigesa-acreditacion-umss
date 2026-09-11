import { test, expect } from '@playwright/test';

test.describe('Search Evidences E2E Tests - Production Route /evidencias/buscar', () => {
  const SEARCH_ROUTE = '/evidencias/buscar';
  const SEARCH_ALIAS_ROUTE = '/evidencias/search';
  const API_SEARCH_URL = '**/api/v1/evidences/search*';

  // Mock search results payload returned by /api/v1/evidences/search
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
    // Setup authenticated session state in localStorage for CC role
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

  // Single-block full E2E User Journey Test
  test('Complete E2E User Journey: Login -> Navigate via Sidebar -> Standard Search -> Toggle AI ON -> Verify AI Header -> Reset', async ({ page }) => {
    let lastQuery = '';
    let lastAiHeader = '';
    let lastAiParam = '';

    await page.route(API_SEARCH_URL, async (route) => {
      const request = route.request();
      const url = new URL(request.url());
      lastQuery = url.searchParams.get('q') || '';
      lastAiParam = url.searchParams.get('aiEnabled') || '';
      lastAiHeader = request.headers()['x-ai-enabled'] || '';
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockSearchResults }),
      });
    });

    // 1. User starts at Dashboard
    await page.goto('/dashboard');

    // 2. Navigates via Sidebar to "BUSCAR EVIDENCIAS"
    const searchNav = page.getByRole('link', { name: /buscar evidencias/i });
    await expect(searchNav).toBeVisible();
    await searchNav.click();

    await expect(page).toHaveURL(new RegExp(SEARCH_ROUTE));
    await expect(page.getByRole('heading', { name: /Buscador de Evidencias/i })).toBeVisible();

    // 3. Step A: Perform standard search (AI OFF)
    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('informe');

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await searchButton.click();

    expect(lastQuery).toBe('informe');
    expect(lastAiHeader).toBe(''); // AI header was NOT sent
    await expect(page.getByText('informe_autoevaluacion.pdf')).toBeVisible();

    // 4. Step B: Enable AI Toggle (AI ON) and search again
    const aiToggle = page.locator('#evidence-search-ai-toggle');
    await aiToggle.check();
    await expect(aiToggle).toBeChecked();

    await searchInput.fill('aulas de clase');
    await searchButton.click();

    // Verification: Confirm AI WAS called via HTTP Header + Query Param + UI Badges
    expect(lastQuery).toBe('aulas de clase');
    expect(lastAiHeader).toBe('true'); // AI header WAS sent to backend
    expect(lastAiParam).toBe('true');
    await expect(page.getByText(/Modo IA MCP Activo/i)).toBeVisible();
    await expect(page.getByText(/ampliados vía Asistente MCP IA/i)).toBeVisible();

    // 5. Step C: Reset search
    const resetButton = page.getByRole('button', { name: /limpiar/i });
    await resetButton.click();
    await expect(searchInput).toHaveValue('');
    await expect(aiToggle).not.toBeChecked();
  });

  // Granular Modular Tests for Isolated CI Debugging
  test('should navigate to production route /evidencias/buscar and verify main elements', async ({ page }) => {
    await page.goto(SEARCH_ROUTE);

    await expect(page).toHaveURL(new RegExp(SEARCH_ROUTE));
    await expect(page.getByRole('heading', { name: /Buscador de Evidencias/i })).toBeVisible();

    const searchInput = page.locator('#evidence-search-q');
    await expect(searchInput).toBeVisible();

    const aiToggle = page.locator('#evidence-search-ai-toggle');
    await expect(aiToggle).toBeVisible();
    await expect(aiToggle).not.toBeChecked();

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await expect(searchButton).toBeVisible();

    await expect(page.getByText('Casos de Prueba (Demo)')).toBeVisible();
  });

  test('should support production alias route /evidencias/search', async ({ page }) => {
    await page.goto(SEARCH_ALIAS_ROUTE);

    await expect(page.getByRole('heading', { name: /Buscador de Evidencias/i })).toBeVisible();
    const searchInput = page.locator('#evidence-search-q');
    await expect(searchInput).toBeVisible();
  });

  test('should execute standard text search without AI mode on /evidencias/buscar', async ({ page }) => {
    let interceptedAiHeader = '';
    let interceptedQuery = '';

    await page.route(API_SEARCH_URL, async (route) => {
      const request = route.request();
      const url = new URL(request.url());
      interceptedQuery = url.searchParams.get('q') || '';
      interceptedAiHeader = request.headers()['x-ai-enabled'] || '';
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockSearchResults }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('informe');

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await searchButton.click();

    expect(interceptedQuery).toBe('informe');
    expect(interceptedAiHeader).toBe('');

    await expect(page.getByText('informe_autoevaluacion.pdf')).toBeVisible();
    await expect(page.getByText('Informe de autoevaluación anual 2025')).toBeVisible();
  });

  test('should execute AI-assisted search when checking AI toggle in frontend UI', async ({ page }) => {
    let interceptedAiHeader = '';
    let interceptedAiParam = '';

    await page.route(API_SEARCH_URL, async (route) => {
      const request = route.request();
      const url = new URL(request.url());
      interceptedAiParam = url.searchParams.get('aiEnabled') || '';
      interceptedAiHeader = request.headers()['x-ai-enabled'] || '';
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockSearchResults }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('aulas de clase');

    const aiToggle = page.locator('#evidence-search-ai-toggle');
    await aiToggle.check();
    await expect(aiToggle).toBeChecked();

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await searchButton.click();

    expect(interceptedAiHeader).toBe('true');
    expect(interceptedAiParam).toBe('true');

    await expect(page.getByText(/Modo IA MCP Activo/i)).toBeVisible();
    await expect(page.getByText('informe_autoevaluacion.pdf')).toBeVisible();
  });

  test('should run Scenario 1: Direct match via scenario demo button', async ({ page }) => {
    let interceptedQuery = '';
    let interceptedAiHeader = '';

    await page.route(API_SEARCH_URL, async (route) => {
      const request = route.request();
      const url = new URL(request.url());
      interceptedQuery = url.searchParams.get('q') || '';
      interceptedAiHeader = request.headers()['x-ai-enabled'] || '';
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockSearchResults }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const scenario1Button = page.getByRole('button', { name: /Escenario 1/i });
    await scenario1Button.click();

    expect(interceptedQuery).toBe('infraestructura');
    expect(interceptedAiHeader).toBe('');
    await expect(page.locator('#evidence-search-q')).toHaveValue('infraestructura');
  });

  test('should run Scenario 2: Multi-Token AI MCP synonym expansion via scenario demo button', async ({ page }) => {
    let interceptedQuery = '';
    let interceptedAiHeader = '';

    await page.route(API_SEARCH_URL, async (route) => {
      const request = route.request();
      const url = new URL(request.url());
      interceptedQuery = url.searchParams.get('q') || '';
      interceptedAiHeader = request.headers()['x-ai-enabled'] || '';
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockSearchResults }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const scenario2Button = page.getByRole('button', { name: /Escenario 2/i });
    await scenario2Button.click();

    expect(interceptedQuery).toBe('aulas de clase');
    expect(interceptedAiHeader).toBe('true');
    await expect(page.locator('#evidence-search-ai-toggle')).toBeChecked();
  });

  test('should run Scenario 3: Out-of-scope query via scenario demo button', async ({ page }) => {
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

    await page.goto(SEARCH_ROUTE);

    const scenario3Button = page.getByRole('button', { name: /Escenario 3/i });
    await scenario3Button.click();

    await expect(page.getByText(/no se encontraron resultados/i)).toBeVisible();
  });

  test('should execute search by pressing Enter key on search input field', async ({ page }) => {
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

    await page.goto(SEARCH_ROUTE);

    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('reglamento');
    await searchInput.press('Enter');

    expect(interceptedQuery).toBe('reglamento');
    await expect(page.getByText('reglamento_docente.pdf')).toBeVisible();
  });

  test('should clear search filters and results when clicking reset button', async ({ page }) => {
    await page.route(API_SEARCH_URL, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockSearchResults }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('informe');

    const aiToggle = page.locator('#evidence-search-ai-toggle');
    await aiToggle.check();

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await searchButton.click();

    const resetButton = page.getByRole('button', { name: /limpiar/i });
    await expect(resetButton).toBeVisible();
    await resetButton.click();

    await expect(searchInput).toHaveValue('');
    await expect(aiToggle).not.toBeChecked();
  });

  test('should render metadata-only badge for evidences without downloadable blob', async ({ page }) => {
    const mockMetadataOnly = {
      items: [
        {
          evidenceId: 'ev-103',
          subphaseId: 'sub-01',
          subphaseName: 'Recopilación de Información',
          phaseName: 'Fase 1: Autoevaluación',
          version: 1,
          description: 'Documento histórico de autoevaluación',
          originalFilename: 'documento_historico.pdf',
          uploadedAt: '2024-01-01T00:00:00Z',
          uploadedBy: 'admin@umss.edu.bo',
          blobAvailable: false,
        },
      ],
      total: 1,
      page: 0,
      size: 20,
    };

    await page.route(API_SEARCH_URL, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockMetadataOnly }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('historico');

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await searchButton.click();

    await expect(page.getByText('documento_historico.pdf')).toBeVisible();
    await expect(page.getByText(/solo metadatos/i).first()).toBeVisible();
  });

  test('should display backend error banner when search API fails', async ({ page }) => {
    await page.route(API_SEARCH_URL, async (route) => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Error interno al consultar vector de búsqueda' }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('error_query');

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await searchButton.click();

    const alert = page.locator('p[role="alert"]').filter({ hasText: /Error/i }).first();
    await expect(alert).toBeVisible();
  });

  test('should support evidence search UI when logged in as Technical Director (TD)', async ({ page }) => {
    await page.addInitScript(() => {
      window.localStorage.setItem(
        'sigesa_auth_session',
        JSON.stringify({
          accessToken: 'mock-td-token',
          expiresIn: 3600,
          role: 'TD',
          programScope: ['prog-01'],
          expiresAt: Date.now() + 3600000,
        })
      );
    });

    await page.route(API_SEARCH_URL, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockSearchResults }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('plan');
    await page.getByRole('button', { name: /buscar/i }).click();

    await expect(page.getByText('informe_autoevaluacion.pdf')).toBeVisible();
  });

  test('should support evidence search UI when logged in as Department Head (JD)', async ({ page }) => {
    await page.addInitScript(() => {
      window.localStorage.setItem(
        'sigesa_auth_session',
        JSON.stringify({
          accessToken: 'mock-jd-token',
          expiresIn: 3600,
          role: 'JD',
          programScope: ['prog-01'],
          expiresAt: Date.now() + 3600000,
        })
      );
    });

    await page.route(API_SEARCH_URL, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockSearchResults }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('plan');
    await page.getByRole('button', { name: /buscar/i }).click();

    await expect(page.getByText('informe_autoevaluacion.pdf')).toBeVisible();
  });
});
