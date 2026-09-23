import { useMemo } from 'react';
import type {
  NormativeIndicatorDto,
  NormativeLevel1NodeDto,
  NormativeLevel2NodeDto,
  NormativeLevel3NodeDto,
} from '../../../api/model';
import { useProcessDetail } from '../../processes/hooks/useProcessDetail';
import { useProcessList } from '../../processes/hooks/useProcessList';

export type IndicatorUploadOption = {
  indicatorId: string;
  indicatorCode: string;
  indicatorDescription: string;
  breadcrumb: string;
  processId: string;
  processLabel: string;
};

export type ProcessUploadOption = {
  processId: string;
  label: string;
};

function flattenIndicators(
  level1Nodes: NormativeLevel1NodeDto[],
  processId: string,
  processLabel: string,
): IndicatorUploadOption[] {
  const items: IndicatorUploadOption[] = [];

  const sortedLevel1 = [...level1Nodes].sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
  for (const level1 of sortedLevel1) {
    const level1Name = level1.name ?? 'N1';
    const level2Nodes = [...(level1.level2Nodes ?? [])].sort(
      (a, b) => (a.order ?? 0) - (b.order ?? 0),
    );
    for (const level2 of level2Nodes) {
      const level2Name = level2.name ?? 'N2';
      const level3Nodes = [...(level2.level3Nodes ?? [])].sort(
        (a, b) => (a.order ?? 0) - (b.order ?? 0),
      );
      for (const level3 of level3Nodes) {
        const level3Name = level3.name ?? 'N3';
        const indicators = [...(level3.indicators ?? [])].sort(
          (a, b) => (a.order ?? 0) - (b.order ?? 0),
        );
        for (const indicator of indicators) {
          if (!indicator.id) continue;
          items.push({
            indicatorId: indicator.id,
            indicatorCode: indicator.code ?? 'IND',
            indicatorDescription: indicator.description ?? 'Indicador',
            breadcrumb: [level1Name, level2Name, level3Name].join(' › '),
            processId,
            processLabel,
          });
        }
      }
    }
  }

  return items;
}

export function useNormativeIndicatorUploadTargets(selectedProcessId: string | undefined) {
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

  const indicatorOptions: IndicatorUploadOption[] = useMemo(() => {
    if (!process?.id) return [];
    const processLabel = `${process.careerName ?? 'Carrera'} · ${process.templateName ?? 'Proceso'}`;
    return flattenIndicators(process.level1Nodes ?? [], process.id, processLabel);
  }, [process]);

  const isLoading = processesLoading || (Boolean(selectedProcessId) && detailLoading);
  const errorMessage =
    processesErrorMessage ??
    (selectedProcessId && detailError ? detailErrorMessage : null);

  return {
    processOptions,
    indicatorOptions,
    selectedProcess: process,
    isLoading,
    isEmpty: !isLoading && !errorMessage && processOptions.length === 0,
    indicatorsEmpty:
      Boolean(selectedProcessId) &&
      !detailLoading &&
      !detailError &&
      indicatorOptions.length === 0,
    errorMessage,
    reload: () => {
      refetchProcesses();
      if (selectedProcessId) refetchDetail();
    },
  };
}

export type { NormativeIndicatorDto, NormativeLevel2NodeDto, NormativeLevel3NodeDto };
