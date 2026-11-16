import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { describe, expect, it, vi } from 'vitest';
import { server } from '../../../test/mocks/server';
import { IndicatorObservationModal } from './IndicatorObservationModal';

function renderWithClient(ui: ReactNode) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(<QueryClientProvider client={client}>{ui}</QueryClientProvider>);
}

describe('IndicatorObservationModal', () => {
  it('lists previous observations and lets the technician send a new one', async () => {
    const user = userEvent.setup();
    server.use(
      http.get('/api/v1/processes/proc-1/observations', () =>
        HttpResponse.json([
          {
            id: 'obs-1',
            processId: 'proc-1',
            evidenceId: 'ev-1',
            indicatorId: 'ind-1',
            description: 'Falta el plano actualizado del laboratorio',
            kind: 'CONTENIDO',
            priority: 'MEDIA',
            compliance: 'NO_CUMPLE',
            recommendation: 'Cargar plano vigente',
            authorId: 'td-1',
            status: 'ENVIADA',
            createdAt: '2026-11-15T12:00:00Z',
            updatedAt: '2026-11-15T12:00:00Z',
            messages: [],
          },
        ]),
      ),
      http.post('/api/v1/processes/proc-1/observations', async ({ request }) => {
        const body = (await request.json()) as { description: string };
        return HttpResponse.json({
          id: 'obs-2',
          processId: 'proc-1',
          evidenceId: 'ev-1',
          indicatorId: 'ind-1',
          description: body.description,
          kind: 'CONTENIDO',
          priority: 'MEDIA',
          compliance: 'NO_CUMPLE',
          recommendation: 'Corregir',
          authorId: 'td-1',
          status: 'BORRADOR',
          createdAt: '2026-11-15T13:00:00Z',
          updatedAt: '2026-11-15T13:00:00Z',
          messages: [],
        }, { status: 201 });
      }),
      http.post('/api/v1/processes/proc-1/observations/obs-2/submit', () =>
        HttpResponse.json({
          id: 'obs-2',
          processId: 'proc-1',
          evidenceId: 'ev-1',
          indicatorId: 'ind-1',
          description: 'Nueva observación del técnico DUEA',
          kind: 'CONTENIDO',
          priority: 'MEDIA',
          compliance: 'NO_CUMPLE',
          recommendation: 'Corregir',
          authorId: 'td-1',
          status: 'ENVIADA',
          createdAt: '2026-11-15T13:00:00Z',
          updatedAt: '2026-11-15T13:00:00Z',
          messages: [],
        }),
      ),
    );

    renderWithClient(
      <IndicatorObservationModal
        isOpen
        processId="proc-1"
        indicatorId="ind-1"
        indicatorLabel="I-01 Infraestructura"
        evidenceId="ev-1"
        canWrite
        onClose={vi.fn()}
      />,
    );

    expect(await screen.findByText('Falta el plano actualizado del laboratorio')).toBeInTheDocument();
    await user.type(screen.getByLabelText(/Nueva observación/), 'Nueva observación del técnico DUEA');
    await user.type(screen.getByLabelText(/Recomendación/), 'Corregir el plano');
    await user.click(screen.getByRole('button', { name: 'Enviar observación' }));
    expect(await screen.findByText(/El coordinador fue notificado/)).toBeInTheDocument();
  });
});
