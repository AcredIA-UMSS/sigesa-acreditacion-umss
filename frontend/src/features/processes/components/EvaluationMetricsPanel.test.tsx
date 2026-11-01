import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { EvaluationMetricsPanel } from './EvaluationMetricsPanel';

describe('EvaluationMetricsPanel', () => {
  it('muestra ratios de evidencia, FODA y lotes', () => {
    render(
      <EvaluationMetricsPanel
        isLoading={false}
        isError={false}
        onRefresh={vi.fn()}
        metrics={{
          totalIndicators: 10,
          indicatorsWithEvidence: 10,
          evidenceCompletenessRatio: 1,
          fodaComplete: 4,
          fodaCompletenessRatio: 0.4,
          primarySurveyBatchCount: 1,
        }}
      />,
    );

    expect(screen.getByText('Completitud de evaluación')).toBeInTheDocument();
    expect(screen.getByText('10/10')).toBeInTheDocument();
    expect(screen.getByText('4/10')).toBeInTheDocument();
    expect(screen.getByText('40%')).toBeInTheDocument();
    expect(screen.getByText('1')).toBeInTheDocument();
  });

  it('permite refrescar métricas', async () => {
    const onRefresh = vi.fn();
    const user = userEvent.setup();
    render(
      <EvaluationMetricsPanel
        isLoading={false}
        isError={false}
        onRefresh={onRefresh}
        metrics={{
          totalIndicators: 2,
          indicatorsWithEvidence: 1,
          evidenceCompletenessRatio: 0.5,
          fodaComplete: 0,
          fodaCompletenessRatio: 0,
          primarySurveyBatchCount: 0,
        }}
      />,
    );

    await user.click(screen.getByRole('button', { name: 'Actualizar métricas' }));
    expect(onRefresh).toHaveBeenCalled();
  });
});
