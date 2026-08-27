import { useMemo } from 'react';
import { useProcessDetail } from '../../processes/hooks/useProcessDetail';
import { useProcessList } from '../../processes/hooks/useProcessList';

export type SubphaseUploadOption = {
  subphaseId: string;
  subphaseName: string;
  phaseName: string;
  processId: string;
  processLabel: string;
};

export type ProcessUploadOption = {
  processId: string;
  label: string;
};

export function useSubphaseUploadTargets(selectedProcessId: string | undefined) {
  const {
    processes,
    isLoading: processesLoading,
    errorMessage: processesErrorMessage,
    refetch: refetchProcesses,
  } = useProcessList();

  const {
    process,
    isLoading: detailLoading,
    isError: detailError,
    errorMessage: detailErrorMessage,
    refetch: refetchDetail,
  } = useProcessDetail(selectedProcessId);

  const activeProcesses = useMemo(
    () => processes.filter((item) => item.status === 'ACTIVE' && item.id),
    [processes],
  );

  const processOptions: ProcessUploadOption[] = useMemo(
    () =>
      activeProcesses.map((item) => ({
        processId: item.id!,
        label: `${item.careerName ?? 'Carrera'} · ${item.templateName ?? 'Proceso'}`,
      })),
    [activeProcesses],
  );

  const subphaseOptions: SubphaseUploadOption[] = useMemo(() => {
    if (!process?.id) return [];
    const processLabel = `${process.careerName ?? 'Carrera'} · ${process.templateName ?? 'Proceso'}`;
    return (process.phases ?? []).flatMap((phase) =>
      (phase.subphases ?? [])
        .filter((sub) => sub.id)
        .map((sub) => ({
          subphaseId: sub.id!,
          subphaseName: sub.name ?? 'Subfase',
          phaseName: phase.name ?? 'Fase',
          processId: process.id!,
          processLabel,
        })),
    );
  }, [process]);

  const isLoading = processesLoading || (Boolean(selectedProcessId) && detailLoading);
  const errorMessage =
    processesErrorMessage ??
    (selectedProcessId && detailError ? detailErrorMessage : null);

  return {
    processOptions,
    subphaseOptions,
    selectedProcess: process,
    isLoading,
    isEmpty: !isLoading && !errorMessage && processOptions.length === 0,
    subphasesEmpty:
      Boolean(selectedProcessId) &&
      !detailLoading &&
      !detailError &&
      subphaseOptions.length === 0,
    errorMessage,
    reload: () => {
      refetchProcesses();
      if (selectedProcessId) refetchDetail();
    },
  };
}
