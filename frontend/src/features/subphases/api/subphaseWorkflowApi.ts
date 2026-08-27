import { customFetch } from '../../../lib/api/customFetch';

export type SubphaseTransitionResult = {
  subphaseId: string;
  previousState: string;
  newState: string;
};

export type SubphaseRejectResponse = {
  subphaseId: string;
  observationId: string;
  transition: SubphaseTransitionResult;
};

export type SubphaseApproveResponse = {
  subphaseId: string;
  transition: SubphaseTransitionResult;
};

export async function rejectSubphase(
  subphaseId: string,
  justification: string,
): Promise<SubphaseRejectResponse> {
  const response = await customFetch<{ data: SubphaseRejectResponse }>(
    `/api/v1/subphases/${encodeURIComponent(subphaseId)}/reject`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ justification }),
    },
  );
  return response.data;
}

export async function approveSubphase(subphaseId: string): Promise<SubphaseApproveResponse> {
  const response = await customFetch<{ data: SubphaseApproveResponse }>(
    `/api/v1/subphases/${encodeURIComponent(subphaseId)}/approve`,
    {
      method: 'POST',
    },
  );
  return response.data;
}
