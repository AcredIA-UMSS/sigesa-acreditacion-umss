import type { AuditLogResponse } from '../../../api/model/auditLogResponse';
import { list2 } from '../../../api/endpoints/audit/audit';
import type { AuditHistoryView } from '../historyRoutes';
import {
  actorLabel,
  actionLabel,
  resourceLabel,
  resultLabel,
  splitOccurredAt,
  type ActorDirectory,
} from './auditDisplay';
import { AUDIT_FETCH_CAP, toListParams, type AuditFilterValues } from './auditFilters';

const EXPORT_PAGE_SIZE = 100;
const MAX_EXPORT_PAGES = 10;

export interface CsvExportResult {
  rowCount: number;
  truncated: boolean;
}

export async function exportFilteredAuditCsv(
  view: AuditHistoryView,
  filters: AuditFilterValues,
  directory: ActorDirectory,
): Promise<CsvExportResult> {
  const rows: AuditLogResponse[] = [];
  let truncated = false;

  for (let page = 0; page < MAX_EXPORT_PAGES; page += 1) {
    const response = await list2(toListParams(filters, page, EXPORT_PAGE_SIZE));
    const content = response.data.content ?? [];
    rows.push(...content);
    const total = response.data.totalElements ?? rows.length;
    const last = response.data.last ?? content.length < EXPORT_PAGE_SIZE;
    if (last || content.length === 0) {
      truncated = total > rows.length;
      break;
    }
    if (page === MAX_EXPORT_PAGES - 1) {
      truncated = total > rows.length;
    }
  }

  downloadCsv(fileName(view), rows, directory);
  return { rowCount: rows.length, truncated };
}

export function exportAuditRows(
  view: AuditHistoryView,
  rows: AuditLogResponse[],
  directory: ActorDirectory,
  truncated: boolean,
): CsvExportResult {
  const limited = rows.slice(0, AUDIT_FETCH_CAP * 2);
  downloadCsv(fileName(view), limited, directory);
  return { rowCount: limited.length, truncated: truncated || rows.length > limited.length };
}

function downloadCsv(name: string, rows: AuditLogResponse[], directory: ActorDirectory): void {
  const header = ['Fecha', 'Hora', 'Usuario', 'Acción', 'Módulo', 'Identificador', 'Descripción', 'Resultado', 'IP'];
  const lines = [
    header.map(csvCell).join(','),
    ...rows.map((row) => {
      const when = splitOccurredAt(row.occurredAt);
      return [
        when.date,
        when.time,
        actorLabel(row, directory),
        actionLabel(row.action),
        resourceLabel(row.resourceType),
        row.resourceId ?? '',
        row.description ?? '',
        resultLabel(row.result),
        row.ipAddress ?? '',
      ].map(csvCell).join(',');
    }),
  ];
  const blob = new Blob([`\uFEFF${lines.join('\n')}`], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = name;
  anchor.click();
  URL.revokeObjectURL(url);
}

function fileName(view: AuditHistoryView): string {
  const day = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'America/La_Paz',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date());
  return `historico-${view.id}-${day}.csv`;
}

function csvCell(value: string): string {
  const guarded = /^[=+\-@]/.test(value) ? `'${value}` : value;
  return `"${guarded.replaceAll('"', '""')}"`;
}
