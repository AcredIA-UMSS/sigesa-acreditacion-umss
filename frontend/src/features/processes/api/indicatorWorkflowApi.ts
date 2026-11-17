import { customFetch } from '../../../lib/api/customFetch';

export type IndicatorWorkflowResponse = {
  indicatorId: string;
  previousState: string;
  newState: string;
  stateHistoryId?: string;
  observationId?: string;
  event?: string;
};

export async function rejectNormativeIndicator(
  indicatorId: string,
  justification: string,
): Promise<IndicatorWorkflowResponse> {
  const response = await customFetch<{ data: IndicatorWorkflowResponse }>(
    `/api/v1/indicators/${encodeURIComponent(indicatorId)}/reject`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ justification }),
    },
  );
  return response.data;
}

export async function approveNormativeIndicator(
  indicatorId: string,
): Promise<IndicatorWorkflowResponse> {
  const response = await customFetch<{ data: IndicatorWorkflowResponse }>(
    `/api/v1/indicators/${encodeURIComponent(indicatorId)}/approve`,
    {
      method: 'POST',
    },
  );
  return response.data;
}
