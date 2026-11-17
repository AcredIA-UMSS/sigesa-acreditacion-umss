import { MemoryRouter } from 'react-router-dom';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { ExecutiveReportUI, type ExecutiveReportUIProps } from './ExecutiveReportUI';
import { buildReportPreview } from '../lib/reportPreview';

const preview = buildReportPreview({
  facultyId: '',
  programId: '',
  managementYear: 2026,
});

function renderReport(overrides: Partial<ExecutiveReportUIProps> = {}) {
  const props: ExecutiveReportUIProps = {
    form: { facultyId: '', programId: '', managementYear: 2026 },
    onFieldChange: vi.fn(),
    onSubmit: vi.fn(),
    onDownload: vi.fn(),
    onReset: vi.fn(),
    activeJobId: null,
    jobStatus: undefined,
    preview,
    validationErrors: {},
    submitErrorMessage: null,
    statusErrorMessage: null,
    downloadErrorMessage: null,
    isSubmitting: false,
    isDownloading: false,
    isPolling: false,
    isBlocked: false,
    ...overrides,
  };
  return {
    props,
    ...render(
      <MemoryRouter>
        <ExecutiveReportUI {...props} />
      </MemoryRouter>,
    ),
  };
}

describe('ExecutiveReportUI', () => {
  it('shouldRenderGenerateActionAndPreviewTitle', () => {
    renderReport();
    expect(screen.getByText('Reporte Ejecutivo PDF')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Generar reporte PDF/ })).toBeEnabled();
    expect(screen.getByText('Reporte Ejecutivo de Acreditación')).toBeInTheDocument();
  });

  it('shouldShowSubmitErrorAndDisableWhenBlocked', () => {
    renderReport({
      submitErrorMessage: 'Acceso denegado. Solo el rol JD puede generar reportes ejecutivos.',
      isBlocked: true,
    });
    expect(
      screen.getByText('Acceso denegado. Solo el rol JD puede generar reportes ejecutivos.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Generar reporte PDF/ })).toBeDisabled();
  });

  it('shouldEnableDownloadWhenJobCompleted', async () => {
    const user = userEvent.setup();
    const onDownload = vi.fn();
    renderReport({
      activeJobId: 'job-1',
      jobStatus: { jobId: 'job-1', status: 'COMPLETED' },
      onDownload,
    });
    const downloadButtons = screen.getAllByRole('button', { name: /Descargar PDF/ });
    await user.click(downloadButtons[0]);
    expect(onDownload).toHaveBeenCalled();
  });
});
