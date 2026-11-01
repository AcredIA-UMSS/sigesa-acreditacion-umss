import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { EvidencePreviewDialog } from './EvidencePreviewDialog';

describe('EvidencePreviewDialog', () => {
  it('muestra estado de carga y permite descargar cuando hay blob', () => {
    const onDownload = vi.fn();
    render(
      <EvidencePreviewDialog
        isOpen
        title="guia.pdf"
        objectUrl="blob:http://localhost/preview"
        contentType="application/pdf"
        isLoading={false}
        error={null}
        onClose={vi.fn()}
        onDownload={onDownload}
      />,
    );

    expect(screen.getByRole('dialog', { name: 'guia.pdf' })).toBeInTheDocument();
    expect(screen.getByTitle('guia.pdf')).toBeInTheDocument();
    screen.getByRole('button', { name: 'Descargar' }).click();
    expect(onDownload).toHaveBeenCalled();
  });
});
