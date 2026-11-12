import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { customFetch } from '../../../lib/api/customFetch';

export interface ObservationMessage {
  id: string;
  authorId: string;
  authorRole: string;
  body: string;
  createdAt: string;
}

export interface ProcessObservation {
  id: string;
  processId: string;
  evidenceId: string;
  indicatorId: string;
  description: string;
  kind: string;
  priority: string;
  compliance: string;
  recommendation: string | null;
  authorId: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  messages: ObservationMessage[];
}

export interface EvidenceOption {
  evidenceId: string;
  indicatorId: string;
  code: string | null;
  title: string | null;
}

export interface ObservationDraftInput {
  evidenceId: string;
  description: string;
  kind: string;
  priority: string;
  compliance: string;
  recommendation: string;
}

interface Envelope<T> {
  data: T;
}

function observationsKey(processId: string) {
  return ['process-observations', processId] as const;
}

async function unwrap<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await customFetch<Envelope<T>>(path, options);
  return response.data;
}

export function useProcessObservations(processId: string) {
  const queryClient = useQueryClient();

  const list = useQuery({
    queryKey: observationsKey(processId),
    queryFn: () => unwrap<ProcessObservation[]>(`/api/v1/processes/${processId}/observations`),
  });

  const evidences = useQuery({
    queryKey: ['process-observation-evidences', processId],
    queryFn: () => unwrap<EvidenceOption[]>(`/api/v1/processes/${processId}/observations/evidences`),
  });

  function invalidate() {
    return queryClient.invalidateQueries({ queryKey: ['process-observations', processId] });
  }

  const create = useMutation({
    mutationFn: (draft: ObservationDraftInput) =>
      unwrap<ProcessObservation>(`/api/v1/processes/${processId}/observations`, {
        method: 'POST',
        body: JSON.stringify(draft),
      }),
    onSuccess: invalidate,
  });

  const update = useMutation({
    mutationFn: (input: { observationId: string; draft: ObservationDraftInput }) =>
      unwrap<ProcessObservation>(
        `/api/v1/processes/${processId}/observations/${input.observationId}`,
        { method: 'PUT', body: JSON.stringify(input.draft) },
      ),
    onSuccess: invalidate,
  });

  const act = useMutation({
    mutationFn: (input: { observationId: string; action: string; body?: string }) =>
      unwrap<ProcessObservation>(
        `/api/v1/processes/${processId}/observations/${input.observationId}/${input.action}`,
        {
          method: 'POST',
          body: input.body === undefined ? undefined : JSON.stringify({ body: input.body }),
        },
      ),
    onSuccess: invalidate,
  });

  return { list, evidences, create, update, act };
}
