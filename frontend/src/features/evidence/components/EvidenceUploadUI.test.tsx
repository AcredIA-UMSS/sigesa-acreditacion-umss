import { MemoryRouter } from 'react-router-dom';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { EvidenceUploadUI, type EvidenceUploadUIProps } from './EvidenceUploadUI';

const emptyForm = {
  processId: '',
  subphaseId: '',
  description: '',
  file: null,
};

function renderUpload(overrides: Partial<EvidenceUploadUIProps> = {}) {
  const props: EvidenceUploadUIProps = {
    form: emptyForm,
    onFieldChange: vi.fn(),
    processOptions: [],
    subphaseOptions: [],
    targetsLoading: false,
    targetsError: null,
    targetsEmpty: false,
    subphasesEmpty: false,
    onReloadTargets: vi.fn(),
    onSubmit: vi.fn(),
    onReset: vi.fn(),
    progress: 0,
    isLargeFile: false,
    isSubmitting: false,
    isBlocked: false,
    result: null,
    errorMessage: null,
    validationErrors: {},
    ...overrides,
  };

  return {
    props,
    ...render(
      <MemoryRouter>
        <EvidenceUploadUI {...props} />
      </MemoryRouter>,
    ),
  };
}

describe('EvidenceUploadUI', () => {
  it('shouldRenderUploadForm', () => {
    renderUpload();
    expect(screen.getByText('Cargar evidencia')).toBeInTheDocument();
    expect(screen.getByLabelText(/proceso/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Subir evidencia/ })).toBeEnabled();
  });

  it('shouldShowLoadingTargetsAndEmptyState', () => {
    renderUpload({ targetsLoading: true });
    expect(screen.getByText('Cargando procesos…')).toBeInTheDocument();
  });

  it('shouldShowTargetsErrorAndSuccessResult', () => {
    renderUpload({
      targetsError: 'No se pudieron cargar los procesos.',
      result: {
        evidenceId: 'ev-1',
        version: 1,
        currentState: 'SUBIDO',
        contentHash: 'abc123',
      },
    });
    expect(screen.getByText('No se pudieron cargar los procesos.')).toBeInTheDocument();
    expect(screen.getByText('Carga exitosa')).toBeInTheDocument();
    expect(screen.getByText('ev-1')).toBeInTheDocument();
  });

  it('shouldSubmitFromTheForm', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    renderUpload({ onSubmit });
    await user.click(screen.getByRole('button', { name: /Subir evidencia/ }));
    expect(onSubmit).toHaveBeenCalledOnce();
  });

  it('shouldDisableSubmitWhenBlocked', () => {
    renderUpload({ isBlocked: true });
    expect(screen.getByRole('button', { name: /Subir evidencia/ })).toBeDisabled();
  });
});
