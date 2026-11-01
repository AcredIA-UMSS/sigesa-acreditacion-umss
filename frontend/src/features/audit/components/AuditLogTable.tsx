import type { AuditLogResponse } from '../../../api/model/auditLogResponse';
import type { AuditHistoryView } from '../historyRoutes';
import {
  cellValue,
  columnLabel,
  columnsFor,
  resultLabel,
  type ActorDirectory,
  type AuditColumnId,
} from '../lib/auditDisplay';

interface AuditLogTableProps {
  viewId: AuditHistoryView['id'];
  rows: AuditLogResponse[];
  directory: ActorDirectory;
  isLoading: boolean;
  selectedId: string | null;
  onSelect: (id: string) => void;
}

export function AuditLogTable({
  viewId,
  rows,
  directory,
  isLoading,
  selectedId,
  onSelect,
}: AuditLogTableProps) {
  const columns = columnsFor(viewId);

  return (
    <div className="overflow-x-auto rounded-xl border border-gray-200" data-testid="audit-table">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            {columns.map((column) => (
              <th
                key={column}
                className="px-4 py-3 text-left text-label-md font-medium uppercase tracking-wide text-gray-600"
              >
                {columnLabel(column)}
              </th>
            ))}
            <th className="px-4 py-3 text-left text-label-md font-medium uppercase tracking-wide text-gray-600">
              Detalle
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100 bg-body">
          {isLoading && (
            <tr>
              <td colSpan={columns.length + 1} className="px-4 py-10 text-center text-body-md text-gray-500" data-testid="audit-loading">
                Cargando historial…
              </td>
            </tr>
          )}
          {!isLoading && rows.length === 0 && (
            <tr>
              <td colSpan={columns.length + 1} className="px-4 py-10 text-center text-body-md text-gray-500" data-testid="audit-empty">
                No hay registros para los filtros seleccionados.
              </td>
            </tr>
          )}
          {!isLoading && rows.map((row) => (
            <tr key={row.id} className={row.id === selectedId ? 'bg-primary-50' : undefined}>
              {columns.map((column) => (
                <td key={column} className="px-4 py-3 text-body-md text-gray-800">
                  <Cell column={column} value={cellValue(column, row, directory)} result={row.result} />
                </td>
              ))}
              <td className="px-4 py-3">
                <button
                  type="button"
                  className="text-label-md font-medium text-primary-600 hover:text-primary-800"
                  onClick={() => row.id && onSelect(row.id)}
                >
                  Ver
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function Cell({ column, value, result }: { column: AuditColumnId; value: string; result?: string }) {
  if (column !== 'result' || value === '—') {
    return <span className={column === 'resourceId' ? 'break-all' : undefined}>{value}</span>;
  }
  const tone = result === 'FAILURE' ? 'bg-danger/10 text-danger' : 'bg-success/15 text-success';
  return <span className={`inline-flex rounded-full px-2 py-1 text-label-md ${tone}`}>{resultLabel(result)}</span>;
}
