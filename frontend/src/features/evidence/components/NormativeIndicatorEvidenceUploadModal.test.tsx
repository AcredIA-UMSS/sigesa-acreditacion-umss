import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { NormativeIndicatorEvidenceUploadModal } from './NormativeIndicatorEvidenceUploadModal';

const uploadMock = vi.fn();
const linkMock = vi.fn();
const fetchAssetsMock = vi.fn();

vi.mock('../api/normativeIndicatorEvidenceApi', () => ({
  uploadNormativeIndicatorEvidence: (...args: unknown[]) => uploadMock(...args),
  linkDocumentAssetToIndicator: (...args: unknown[]) => linkMock(...args),
  fetchProcessDocumentAssets: (...args: unknown[]) => fetchAssetsMock(...args),
}));

describe('NormativeIndicatorEvidenceUploadModal', () => {
  beforeEach(() => {
    uploadMock.mockReset();
    linkMock.mockReset();
    fetchAssetsMock.mockReset();
    fetchAssetsMock.mockResolvedValue([
      {
        id: 'asset-1',
        folioCode: 'SIGESA-20261013-abcd1234',
        title: 'plan.pdf',
        contentHash: 'abcd1234ffff',
        processId: 'proc-1',
      },
    ]);
  });

  it('vincula un DocumentAsset existente y muestra folio', async () => {
    const user = userEvent.setup();
    linkMock.mockResolvedValue({
      evidenceId: 'ev-1',
      version: 1,
      contentHash: 'abcd1234ffff',
      currentState: 'SUBIDO',
      documentAssetId: 'asset-1',
      folioCode: 'SIGESA-20261013-abcd1234',
      reusedAsset: true,
    });

    render(
      <NormativeIndicatorEvidenceUploadModal
        isOpen
        onClose={vi.fn()}
        processId="proc-1"
        indicatorId="ind-1"
        indicatorLabel="IND-01"
        breadcrumb="N1 › N2 › N3"
        canUpload
      />,
    );

    await user.click(screen.getByLabelText(/Usar documento ya cargado en este proceso/i));
    const select = await screen.findByLabelText(/Documento existente/i);
    await user.selectOptions(select, 'asset-1');
    await user.click(screen.getByRole('button', { name: /Vincular documento/i }));

    expect(linkMock).toHaveBeenCalledWith({
      indicatorId: 'ind-1',
      documentAssetId: 'asset-1',
    });
    expect(await screen.findByRole('status')).toHaveTextContent('Folio SIGESA-20261013-abcd1234');
    expect(screen.getByRole('status')).toHaveTextContent('Documento reutilizado');
  });
});
