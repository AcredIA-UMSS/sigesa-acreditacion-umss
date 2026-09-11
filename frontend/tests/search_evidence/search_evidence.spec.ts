import { test, expect } from '@playwright/test';

test.describe('Search Evidences E2E Tests - Production Route /evidencias/buscar', () => {
  const SEARCH_ROUTE = '/evidencias/buscar';
  const SEARCH_ALIAS_ROUTE = '/evidencias/search';
  const API_SEARCH_URL = '**/api/v1/evidences/search*';

  // Mock standard keyword search payload (exact text matching)
  const mockStandardSearchResults = {
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
    ],
    total: 1,
    page: 0,
    size: 20,
  };

  // Mock AI Synonym Expansion payload (returns semantically expanded hits matching synonyms like infra/labs instead of literal words)
  const mockAiSynonymSearchResults = {
    items: [
      {
        evidenceId: 'ev-201',
        subphaseId: 'sub-01',
        subphaseName: 'Infraestructura y Aulas',
        phaseId: 'phase-01',
        phaseName: 'Fase 1: Autoevaluación',
        processId: 'proc-01',
        indicatorId: 'ind-04',
        indicatorCode: 'CRT-04',
        indicatorTitle: 'Infraestructura Académica',
        version: 1,
        description: 'Planos aprobados y distribución de laboratorios de computación e infraestructura física',
        originalFilename: 'planos_distribucion_infraestructura.pdf',
        uploadedAt: '2025-08-05T10:00:00Z',
        uploadedBy: 'tecnico@umss.edu.bo',
        blobAvailable: true,
      },
      {
        evidenceId: 'ev-202',
        subphaseId: 'sub-02',
        subphaseName: 'Equipamiento',
        phaseId: 'phase-01',
        phaseName: 'Fase 1: Autoevaluación',
        processId: 'proc-01',
        indicatorId: 'ind-04',
        indicatorCode: 'CRT-04',
        indicatorTitle: 'Infraestructura Académica',
        version: 2,
        description: 'Inventario valorado de activos fijos tecnológicos y equipamiento interactivo',
        originalFilename: 'inventario_equipos_tecnologicos.pdf',
        uploadedAt: '2025-08-10T14:30:00Z',
        uploadedBy: 'admin@umss.edu.bo',
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
  test('Complete E2E User Journey: Login -> Navigate via Sidebar -> Standard Search -> Toggle AI ON (Synonym Expansion) -> Verify AI Header -> Reset', async ({ page }) => {
    let lastQuery = '';
    let lastAiHeader = '';
    let lastAiParam = '';

    await page.route(API_SEARCH_URL, async (route) => {
      const request = route.request();
      const url = new URL(request.url());
      lastQuery = url.searchParams.get('q') || '';
      lastAiParam = url.searchParams.get('aiEnabled') || '';
      lastAiHeader = request.headers()['x-ai-enabled'] || '';

      // Return AI synonym expansion results if X-AI-Enabled header is present, else standard results
      const responsePayload = lastAiHeader === 'true' ? mockAiSynonymSearchResults : mockStandardSearchResults;

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: responsePayload }),
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

    // 3. Step A: Standard exact text search without AI (AI OFF)
    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('informe');

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await searchButton.click();

    expect(lastQuery).toBe('informe');
    expect(lastAiHeader).toBe(''); // AI header was NOT sent
    await expect(page.getByText('informe_autoevaluacion.pdf')).toBeVisible();

    // 4. Step B: Enable AI Toggle (AI ON) and search query "aulas de clase"
    const aiToggle = page.locator('#evidence-search-ai-toggle');
    await aiToggle.check();
    await expect(aiToggle).toBeChecked();

    await searchInput.fill('aulas de clase');
    await searchButton.click();

    // Verification 1: Confirm X-AI-Enabled header and query param WERE sent to backend API
    expect(lastQuery).toBe('aulas de clase');
    expect(lastAiHeader).toBe('true');
    expect(lastAiParam).toBe('true');

    // Verification 2: Confirm AI Synonym Expansion badge and result indicators in DOM
    await expect(page.getByText(/Modo IA MCP Activo/i)).toBeVisible();
    await expect(page.getByText(/ampliados vía Asistente MCP IA/i)).toBeVisible();

    // Verification 3: Confirm evidence hits containing expanded synonyms ("infraestructura", "tecnologicos") are displayed
    await expect(page.getByText('planos_distribucion_infraestructura.pdf')).toBeVisible();
    await expect(page.getByText('inventario_equipos_tecnologicos.pdf')).toBeVisible();

    // 5. Step C: Reset search
    const resetButton = page.getByRole('button', { name: /limpiar/i });
    await resetButton.click();
    await expect(searchInput).toHaveValue('');
    await expect(aiToggle).not.toBeChecked();
  });

  // Granular Spec: Verify AI Synonym Expansion Function Call
  test('should execute AI Synonym Expansion when AI toggle is enabled', async ({ page }) => {
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
        body: JSON.stringify({ data: mockAiSynonymSearchResults }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('aulas de clase');

    const aiToggle = page.locator('#evidence-search-ai-toggle');
    await aiToggle.check();

    const searchButton = page.getByRole('button', { name: /buscar/i });
    await searchButton.click();

    // 1. Confirm AI network protocol contract
    expect(interceptedQuery).toBe('aulas de clase');
    expect(interceptedAiHeader).toBe('true');

    // 2. Confirm AI expanded synonym results appear in DOM (not requiring exact literal match)
    await expect(page.getByText(/Modo IA MCP Activo/i)).toBeVisible();
    await expect(page.getByText('planos_distribucion_infraestructura.pdf')).toBeVisible();
    await expect(page.getByText('inventario_equipos_tecnologicos.pdf')).toBeVisible();
    await expect(page.getByText(/ampliados vía Asistente MCP IA/i)).toBeVisible();
  });

  // Granular Spec: Scenario 2 Preset AI MCP Button
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
        body: JSON.stringify({ data: mockAiSynonymSearchResults }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const scenario2Button = page.getByRole('button', { name: /Escenario 2/i });
    await scenario2Button.click();

    expect(interceptedQuery).toBe('aulas de clase');
    expect(interceptedAiHeader).toBe('true');
    await expect(page.locator('#evidence-search-ai-toggle')).toBeChecked();
    await expect(page.getByText('planos_distribucion_infraestructura.pdf')).toBeVisible();
  });

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
        body: JSON.stringify({ data: mockStandardSearchResults }),
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
        body: JSON.stringify({ data: mockStandardSearchResults }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const scenario1Button = page.getByRole('button', { name: /Escenario 1/i });
    await scenario1Button.click();

    expect(interceptedQuery).toBe('infraestructura');
    expect(interceptedAiHeader).toBe('');
    await expect(page.locator('#evidence-search-q')).toHaveValue('infraestructura');
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
        body: JSON.stringify({ data: mockStandardSearchResults }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('informe');
    await searchInput.press('Enter');

    expect(interceptedQuery).toBe('informe');
    await expect(page.getByText('informe_autoevaluacion.pdf')).toBeVisible();
  });

  test('should clear search filters and results when clicking reset button', async ({ page }) => {
    await page.route(API_SEARCH_URL, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: mockStandardSearchResults }),
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
        body: JSON.stringify({ data: mockStandardSearchResults }),
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
        body: JSON.stringify({ data: mockStandardSearchResults }),
      });
    });

    await page.goto(SEARCH_ROUTE);

    const searchInput = page.locator('#evidence-search-q');
    await searchInput.fill('plan');
    await page.getByRole('button', { name: /buscar/i }).click();

    await expect(page.getByText('informe_autoevaluacion.pdf')).toBeVisible();
  });
});
