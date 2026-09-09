import { useCallback, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../../../lib/auth/useAuth';
import {
  getListStagesQueryKey,
  useApproveDeliverable,
  useApproveStage,
  useListStages,
  useSubmitStage,
} from '../../../api/endpoints/stage-workflow/stage-workflow';
import { MethodologicalStageTimeline } from './MethodologicalStageTimeline';

interface MethodologicalStageTimelineContainerProps {
  processId: string;
}

function extractErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  return 'Ocurrió un error inesperado.';
}

export function MethodologicalStageTimelineContainer({
  processId,
}: MethodologicalStageTimelineContainerProps) {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const [approvingDeliverableId, setApprovingDeliverableId] = useState<string | undefined>();

  const stagesQuery = useListStages(processId, {
    query: { enabled: Boolean(processId) },
  });

  const submitMutation = useSubmitStage({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: getListStagesQueryKey(processId) });
      },
    },
  });

  const approveStageMutation = useApproveStage({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: getListStagesQueryKey(processId) });
      },
    },
  });

  const approveDeliverableMutation = useApproveDeliverable({
    mutation: {
      onSuccess: () => {
        setApprovingDeliverableId(undefined);
        void queryClient.invalidateQueries({ queryKey: getListStagesQueryKey(processId) });
      },
      onError: () => {
        setApprovingDeliverableId(undefined);
      },
    },
  });

  const stages = stagesQuery.data?.status === 200 ? stagesQuery.data.data : [];
  const activeStageId = stages.find(
    (stage) =>
      stage.status === 'IN_PROGRESS'
      || stage.status === 'OBSERVED'
      || stage.status === 'SUBMITTED_FOR_REVIEW',
  )?.id;

  const canSubmitStage = session?.role === 'CC';
  const canApproveStage = session?.role === 'TD' || session?.role === 'JD';
  const canApproveDeliverable = session?.role === 'TD';

  const handleSubmitStage = useCallback(
    (stageId: string) => {
      submitMutation.mutate({ processId, stageId });
    },
    [processId, submitMutation],
  );

  const handleApproveStage = useCallback(
    (stageId: string) => {
      approveStageMutation.mutate({ processId, stageId });
    },
    [approveStageMutation, processId],
  );

  const handleApproveDeliverable = useCallback(
    (stageId: string, deliverableId: string) => {
      setApprovingDeliverableId(deliverableId);
      approveDeliverableMutation.mutate({ processId, stageId, deliverableId });
    },
    [approveDeliverableMutation, processId],
  );

  const mutationError =
    submitMutation.error ?? approveStageMutation.error ?? approveDeliverableMutation.error;

  return (
    <MethodologicalStageTimeline
      stages={stages}
      isLoading={stagesQuery.isLoading}
      isError={stagesQuery.isError || Boolean(mutationError)}
      errorMessage={
        mutationError
          ? extractErrorMessage(mutationError)
          : stagesQuery.isError
            ? extractErrorMessage(stagesQuery.error)
            : undefined
      }
      canSubmitStage={canSubmitStage}
      canApproveStage={canApproveStage}
      canApproveDeliverable={canApproveDeliverable}
      activeStageId={activeStageId}
      isSubmitting={submitMutation.isPending}
      isApprovingStage={approveStageMutation.isPending}
      approvingDeliverableId={approvingDeliverableId}
      onSubmitStage={handleSubmitStage}
      onApproveStage={handleApproveStage}
      onApproveDeliverable={handleApproveDeliverable}
      onRefresh={() => void stagesQuery.refetch()}
    />
  );
}
