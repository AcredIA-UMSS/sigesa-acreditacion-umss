import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import type { ReactNode } from 'react';
import { describe, expect, it, vi } from 'vitest';
import { NormativeIndicatorCollaborationSection } from './NormativeIndicatorCollaborationSection';

vi.mock('../../evidence/hooks/useNormativeIndicatorCollaboration', () => ({
  useNormativeIndicatorCollaboration: () => ({
    evidences: [
      {
        evidenceId: 'ev-1',
        indicatorId: 'ind-1',
        version: 1,
        description: 'Acta',
        contentHash: 'abc',
        originalFilename: 'acta.pdf',
        uploadedAt: '2026-10-22T00:00:00',
        uploadedBy: 'cc-1',
      },
    ],
    eligibility: null,
    hasOpenObservation: false,
    isLoading: false,
    error: null,
    reload: vi.fn(),
  }),
}));

function renderWithClient(ui: ReactNode) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(<QueryClientProvider client={client}>{ui}</QueryClientProvider>);
}

describe('NormativeIndicatorCollaborationSection', () => {
  it('permite quitar evidencia si el indicador no está aprobado', () => {
    renderWithClient(
      <NormativeIndicatorCollaborationSection
        processId="proc-1"
        indicatorId="ind-1"
        indicatorLabel="I-01"
        indicatorStatus="SUBIDO"
        breadcrumb="N1 › N2 › N3"
        canUpload
        canSubsanate={false}
        canReview={false}
      />,
    );

    expect(screen.getByRole('button', { name: /Quitar \/ reemplazar/i })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Subir evidencia/i })).not.toBeInTheDocument();
  });

  it('no ofrece quitar si el indicador está aprobado', () => {
    renderWithClient(
      <NormativeIndicatorCollaborationSection
        processId="proc-1"
        indicatorId="ind-1"
        indicatorLabel="I-01"
        indicatorStatus="APROBADO"
        breadcrumb="N1 › N2 › N3"
        canUpload
        canSubsanate={false}
        canReview={false}
      />,
    );

    expect(screen.queryByRole('button', { name: /Quitar \/ reemplazar/i })).not.toBeInTheDocument();
  });
});
