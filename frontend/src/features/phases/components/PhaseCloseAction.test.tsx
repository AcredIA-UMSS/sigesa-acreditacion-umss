import { http, HttpResponse } from 'msw';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { PhaseCloseAction } from './PhaseCloseAction';
import { server } from '../../../test/mocks/server';

describe('PhaseCloseAction', () => {
  it('shouldHideWhenPhaseIsAlreadyCompleted', () => {
    const { container } = render(
      <PhaseCloseAction
        processId="proc-1"
        phaseId="phase-1"
        phaseName="Fase 1"
        phaseStatus="COMPLETADA"
        onCompleted={vi.fn()}
      />,
    );
    expect(container).toBeEmptyDOMElement();
  });

  it('shouldClosePhaseAfterConfirmation', async () => {
    const user = userEvent.setup();
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const onCompleted = vi.fn();
    render(
      <PhaseCloseAction
        processId="proc-1"
        phaseId="phase-1"
        phaseName="Autoevaluación"
        phaseStatus="ABIERTA"
        onCompleted={onCompleted}
      />,
    );

    await user.click(screen.getByRole('button', { name: /Cerrar fase/ }));
    expect(await screen.findByText(/completada correctamente/)).toBeInTheDocument();
    expect(onCompleted).toHaveBeenCalledOnce();
  });

  it('shouldShowPendingSubphasesWhenClosureIsBlocked', async () => {
    const user = userEvent.setup();
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    server.use(
      http.post('/api/v1/processes/:processId/phases/:phaseId/complete', () =>
        HttpResponse.json(
          {
            error: 'FASE_CIERRE_BLOQUEADO',
            message: 'Hay subfases pendientes.',
            pendingSubphases: [{ subphaseId: 's1', name: 'Diagnóstico', status: 'SUBIDO' }],
          },
          { status: 409 },
        ),
      ),
    );

    render(
      <PhaseCloseAction
        processId="proc-1"
        phaseId="phase-1"
        phaseName="Autoevaluación"
        phaseStatus="ABIERTA"
        onCompleted={vi.fn()}
      />,
    );

    await user.click(screen.getByRole('button', { name: /Cerrar fase/ }));
    expect(await screen.findByText('Hay subfases pendientes.')).toBeInTheDocument();
    expect(screen.getByText(/Diagnóstico/)).toBeInTheDocument();
  });
});
