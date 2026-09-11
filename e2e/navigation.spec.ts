import { expect, test } from '@playwright/test';
import { TestId } from './fixtures/testids';
import { stubBackend } from './helpers/apiStub';
import { seedSession } from './helpers/auth';

test.describe('navegación JD', () => {
  test.beforeEach(async ({ page }) => {
    await stubBackend(page);
    await seedSession(page, 'JD');
  });

  test('sidebar JD recorre pantallas con data-testid', async ({ page }) => {
    await page.goto('/admin/users');
    await expect(page.getByTestId(TestId.sidebarNav)).toBeVisible();
    await expect(page.getByTestId(TestId.sidebarLogout)).toBeVisible();

    await page.getByTestId(TestId.sidebarNavDashboard).click();
    await expect(page.getByTestId(TestId.dashboardPage)).toBeVisible();

    await page.getByTestId(TestId.sidebarNavProcesses).click();
    await page.getByTestId(TestId.sidebarNavProcessesList).click();
    await expect(page.getByTestId(TestId.processListPage)).toBeVisible();
    await expect(page.getByTestId(TestId.processList)).toBeVisible();

    await page.getByTestId(TestId.sidebarNavProcessNew).click();
    await expect(page.getByTestId(TestId.createProcessPage)).toBeVisible();
    await expect(page.getByTestId(TestId.createProcessForm)).toBeVisible();
    await expect(page.getByTestId(TestId.createProcessCareer)).toBeVisible();
    await expect(page.getByTestId(TestId.createProcessTemplate)).toBeVisible();
    await expect(page.getByTestId(TestId.createProcessSubmit)).toBeVisible();

    await page.getByTestId(TestId.sidebarNavTemplates).click();
    await expect(page.getByTestId(TestId.templatesListPage)).toBeVisible();
    await expect(page.getByTestId(TestId.templatesList)).toBeVisible();
    await expect(page.getByTestId(TestId.templatesNew)).toBeVisible();

    await page.getByTestId(TestId.sidebarNavUsers).click();
    await expect(page.getByTestId(TestId.usersAdminPage)).toBeVisible();

    await page.getByTestId(TestId.sidebarNavReports).click();
    await expect(page.getByTestId(TestId.executiveReportPage)).toBeVisible();
    await expect(page.getByTestId(TestId.reportExecutiveForm)).toBeVisible();
    await expect(page.getByTestId(TestId.reportExecutiveYear)).toBeVisible();
    await expect(page.getByTestId(TestId.reportExecutiveSubmit)).toBeVisible();

    await page.getByTestId(TestId.sidebarNavHelp).click();
    await expect(page.getByTestId(TestId.assistantPage)).toBeVisible();
    await expect(page.getByTestId(TestId.assistantChat)).toBeVisible();
    await expect(page.getByTestId(TestId.assistantInput)).toBeVisible();
    await expect(page.getByTestId(TestId.assistantSend)).toBeVisible();
  });

  test('listado de procesos abre nuevo proceso', async ({ page }) => {
    await page.goto('/procesos');
    await page.getByTestId(TestId.processListNew).click();
    await expect(page.getByTestId(TestId.createProcessPage)).toBeVisible();
  });

  test('plantillas abre editor nueva', async ({ page }) => {
    await page.goto('/admin/plantillas');
    await page.getByTestId(TestId.templatesNew).click();
    await expect(page.getByTestId(TestId.templateEditorPage)).toBeVisible();
  });

  test('alta de usuario abre modal con anclas', async ({ page }) => {
    await page.goto('/admin/users');
    await expect(page.getByTestId(TestId.usersTable)).toBeVisible();
    await page.getByTestId(TestId.usersAdd).click();
    await expect(page.getByTestId(TestId.usersAddModal)).toBeVisible();
    await expect(page.getByTestId(TestId.usersAddFirstName)).toBeVisible();
    await expect(page.getByTestId(TestId.usersAddLastName)).toBeVisible();
    await expect(page.getByTestId(TestId.usersAddEmail)).toBeVisible();
    await expect(page.getByTestId(TestId.usersAddRole)).toBeVisible();
    await expect(page.getByTestId(TestId.usersAddSubmit)).toBeVisible();
  });
});

test.describe('navegación CC', () => {
  test.beforeEach(async ({ page }) => {
    await stubBackend(page);
    await seedSession(page, 'CC');
  });

  test('carga de evidencia expone el formulario UC-004', async ({ page }) => {
    await page.goto('/dashboard');
    await page.getByTestId(TestId.sidebarNavEvidence).click();
    await expect(page.getByTestId(TestId.evidenceUploadPage)).toBeVisible();
    await expect(page.getByTestId(TestId.evidenceUploadForm)).toBeVisible();
    await expect(page.getByTestId(TestId.evidenceIndicator)).toBeVisible();
    await expect(page.getByTestId(TestId.evidenceCriterion)).toBeVisible();
    await expect(page.getByTestId(TestId.evidenceDescription)).toBeVisible();
    await expect(page.getByTestId(TestId.evidenceFile)).toBeAttached();
    await expect(page.getByTestId(TestId.evidenceSubmit)).toBeVisible();
  });
});
