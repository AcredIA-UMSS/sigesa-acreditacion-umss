import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { MethodologicalStageTimeline } from './MethodologicalStageTimeline';

describe('MethodologicalStageTimeline — M9 E4', () => {
  it('muestra botón Generar borrador y folio en entregables E4 de DRAFTING', async () => {
    const onGenerate = vi.fn();
    const user = userEvent.setup();
    render(
      <MethodologicalStageTimeline
        stages={[
          {
            id: 'stage-4',
            code: 'DRAFTING',
            displayName: 'Elaboración documentos clave',
            order: 4,
            status: 'IN_PROGRESS',
            deliverables: [
              {
                id: 'del-1',
                deliverableCode: 'IMPROVEMENT_PLAN',
                approvalStatus: 'PENDING',
                documentAssetId: 'asset-1',
                folioCode: 'SIGESA-20261013-abcd1234',
              },
            ],
          },
        ]}
        isLoading={false}
        isError={false}
        canSubmitStage
        canApproveStage={false}
        canApproveDeliverable={false}
        isSubmitting={false}
        isApprovingStage={false}
        onSubmitStage={vi.fn()}
        onApproveStage={vi.fn()}
        onApproveDeliverable={vi.fn()}
        onRefresh={vi.fn()}
        canRegisterSurvey={false}
        surveyBatches={[]}
        isRegisteringSurvey={false}
        onRegisterSurvey={vi.fn()}
        canGenerateDeliverable
        onGenerateDeliverable={onGenerate}
      />,
    );

    expect(screen.getByText(/Folio: SIGESA-20261013-abcd1234/)).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /Generar borrador/i }));
    expect(onGenerate).toHaveBeenCalledWith('stage-4', 'del-1');
  });
});
