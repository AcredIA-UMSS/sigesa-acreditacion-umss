import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { ProcessOperationalModeBadge } from './ProcessOperationalModeBadge';

describe('ProcessOperationalModeBadge', () => {
  it('muestra etiqueta de freeze AUDIT_READONLY', () => {
    render(<ProcessOperationalModeBadge mode="AUDIT_READONLY" />);
    expect(screen.getByTestId('operational-mode-badge')).toHaveTextContent('Congelado');
  });

  it('muestra monitoreo de mejoras', () => {
    render(<ProcessOperationalModeBadge mode="IMPROVEMENT_MONITORING" />);
    expect(screen.getByTestId('operational-mode-badge')).toHaveTextContent('Monitoreo');
  });
});
