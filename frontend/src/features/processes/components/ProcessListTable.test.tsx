import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { ProcessStatusBadge } from './ProcessStatusBadge';
import { ProcessListTable } from './ProcessListTable';
import { sampleProcess } from '../../../test/mocks/handlers';

describe('ProcessStatusBadge', () => {
  it('shouldShowLocalizedLabels', () => {
    const { rerender } = render(<ProcessStatusBadge status="ACTIVE" />);
    expect(screen.getByText('Activo')).toBeInTheDocument();
    rerender(<ProcessStatusBadge status="COMPLETED" />);
    expect(screen.getByText('Completado')).toBeInTheDocument();
  });
});

describe('ProcessListTable', () => {
  it('shouldShowEmptyState', () => {
    render(<ProcessListTable processes={[]} />);
    expect(screen.getByRole('heading', { name: 'Sin procesos visibles' })).toBeInTheDocument();
  });

  it('shouldRenderProcessRowsWithDetailLink', () => {
    render(
      <MemoryRouter>
        <ProcessListTable processes={[sampleProcess]} />
      </MemoryRouter>,
    );

    expect(screen.getByText('Ingeniería de Sistemas')).toBeInTheDocument();
    expect(screen.getByText('INF-SIS')).toBeInTheDocument();
    expect(screen.getByText('Activo')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Ver detalle/ })).toHaveAttribute(
      'href',
      '/procesos/950e8400-e29b-41d4-a716-446655440020',
    );
  });
});
