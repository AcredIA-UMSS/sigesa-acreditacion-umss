import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { SubphaseReviewActions } from './SubphaseReviewActions';

describe('SubphaseReviewActions', () => {
  it('shouldBlockActionsWhenThereIsNoEvidence', () => {
    render(
      <SubphaseReviewActions
        subphaseId="sub-1"
        subphaseName="Diagnóstico"
        hasEvidences={false}
        hasOpenObservation={false}
        onCompleted={vi.fn()}
      />,
    );
    expect(screen.getByText(/No hay evidencias cargadas/)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Aprobar/ })).not.toBeInTheDocument();
  });

  it('shouldApproveWhenEvidenceExists', async () => {
    const user = userEvent.setup();
    const onCompleted = vi.fn();
    render(
      <SubphaseReviewActions
        subphaseId="sub-1"
        subphaseName="Diagnóstico"
        hasEvidences
        hasOpenObservation={false}
        onCompleted={onCompleted}
      />,
    );

    await user.click(screen.getByRole('button', { name: /Aprobar/ }));
    expect(await screen.findByText(/aprobada correctamente/)).toBeInTheDocument();
    expect(onCompleted).toHaveBeenCalledOnce();
  });

  it('shouldRequireJustificationBeforeReject', async () => {
    const user = userEvent.setup();
    render(
      <SubphaseReviewActions
        subphaseId="sub-1"
        subphaseName="Diagnóstico"
        hasEvidences
        hasOpenObservation={false}
        onCompleted={vi.fn()}
      />,
    );

    await user.click(screen.getByRole('button', { name: /Rechazar/ }));
    await user.type(screen.getByPlaceholderText(/Justificación/), 'corta');
    await user.click(screen.getByRole('button', { name: /Confirmar rechazo/ }));
    expect(screen.getByText(/al menos 20 caracteres/)).toBeInTheDocument();
  });
});
