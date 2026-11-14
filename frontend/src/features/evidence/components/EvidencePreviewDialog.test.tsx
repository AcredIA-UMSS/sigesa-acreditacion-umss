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

  it('muestra mensaje de error amigable cuando falla la carga del blob', () => {
    render(
      <EvidencePreviewDialog
        isOpen
        title="guia.pdf"
        objectUrl={null}
        contentType={null}
        isLoading={false}
        error="El archivo de la evidencia no está disponible en el servidor."
        onClose={vi.fn()}
        onDownload={vi.fn()}
      />,
    );

    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(
      screen.getByText('El archivo de la evidencia no está disponible en el servidor.'),
    ).toBeInTheDocument();
    expect(
      screen.getByText(/Verifique que el archivo haya sido cargado correctamente/),
    ).toBeInTheDocument();
  });
});
