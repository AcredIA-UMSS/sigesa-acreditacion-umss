import type { ExecutiveReportFormState } from '../hooks/useExecutiveReport';

export type SemaphoreTone = 'GREEN' | 'YELLOW' | 'RED';

export type ReportPreviewRow = {
  programName: string;
  semaphore: SemaphoreTone;
  totalIndicators: number;
  approvedIndicators: number;
  progressPercent: number;
};

export type ReportPreviewModel = {
  title: string;
  generatedAtLabel: string | null;
  managementYear: number;
  facultyLabel: string;
  programLabel: string;
  rows: ReportPreviewRow[];
};

function progressOf(approved: number, total: number): number {
  if (total <= 0) return 0;
  return Math.round((approved / total) * 100);
}

/**
 * Vista previa alineada al snapshot del reporte ejecutivo (stub/PDF UC-014).
 * No sustituye el PDF; permite visualizar en UI la misma estructura.
 */
export function buildReportPreview(
  form: ExecutiveReportFormState,
  options?: { generatedAt?: Date | null },
): ReportPreviewModel {
  const programId = form.programId.trim();
  const facultyId = form.facultyId.trim();
  const year = form.managementYear;

  let rows: ReportPreviewRow[];

  if (programId) {
    rows = [
      {
        programName: `Carrera ${programId.slice(0, 8)}…`,
        semaphore: 'YELLOW',
        totalIndicators: 42,
        approvedIndicators: 28,
        progressPercent: progressOf(28, 42),
      },
    ];
  } else if (facultyId) {
    rows = [
      {
        programName: 'Programa Facultad A',
        semaphore: 'GREEN',
        totalIndicators: 30,
        approvedIndicators: 25,
        progressPercent: progressOf(25, 30),
      },
      {
        programName: 'Programa Facultad B',
        semaphore: 'RED',
        totalIndicators: 18,
        approvedIndicators: 6,
        progressPercent: progressOf(6, 18),
      },
    ];
  } else {
    rows = [
      {
        programName: `Resumen institucional ${year}`,
        semaphore: 'YELLOW',
        totalIndicators: 120,
        approvedIndicators: 78,
        progressPercent: progressOf(78, 120),
      },
    ];
  }

  const generatedAt = options?.generatedAt ?? null;

  return {
    title: 'Reporte Ejecutivo de Acreditación',
    generatedAtLabel: generatedAt
      ? formatGeneratedAt(generatedAt)
      : null,
    managementYear: year,
    facultyLabel: facultyId ? facultyId.slice(0, 8) + '…' : 'Todos',
    programLabel: programId ? programId.slice(0, 8) + '…' : 'Todos',
    rows,
  };
}

function formatGeneratedAt(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

export const SEMAPHORE_LABELS: Record<SemaphoreTone, string> = {
  GREEN: 'Verde',
  YELLOW: 'Amarillo',
  RED: 'Rojo',
};
