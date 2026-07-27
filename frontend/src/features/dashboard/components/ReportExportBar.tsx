import { useState } from 'react';
import { useExportReport } from '../api/dashboardHooks';
import { Download, AlertCircle, FileSpreadsheet, FileText, Loader2 } from 'lucide-react';

interface ReportExportBarProps {
  phaseId?: number;
}

export const ReportExportBar = ({ phaseId }: ReportExportBarProps) => {
  const [format, setFormat] = useState<'xlsx' | 'csv' | 'pdf'>('xlsx');
  const { exportReport, isPending, progress, error } = useExportReport();

  const handleExport = async () => {
    try {
      await exportReport(format, phaseId);
    } catch {
      // Error handled by hook state
    }
  };

  return (
    <div className="flex flex-col gap-4 rounded-2xl border border-gray-100 bg-body p-6 shadow-sm">
      <div className="flex flex-col flex-wrap gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-heading-sm font-bold text-primary-800">Exportar Reportes del Dashboard</h3>
          <p className="text-body-md text-gray-600">
            Descarga un reporte detallado del estado de las fases e indicadores del programa actual.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center rounded-xl bg-primary-800/60 p-1 border border-primary-800">
            <button
              type="button"
              onClick={() => setFormat('xlsx')}
              disabled={isPending}
              className={`flex items-center gap-1.5 rounded-lg px-3.5 py-2 text-label-md font-medium transition-all ${
                format === 'xlsx'
                  ? 'bg-primary-600 text-body shadow-md shadow-primary-600/20'
                  : 'text-primary-200 hover:text-body'
              }`}
            >
              <FileSpreadsheet size={16} />
              Excel (XLSX)
            </button>
            <button
              type="button"
              onClick={() => setFormat('csv')}
              disabled={isPending}
              className={`flex items-center gap-1.5 rounded-lg px-3.5 py-2 text-label-md font-medium transition-all ${
                format === 'csv'
                  ? 'bg-primary-600 text-body shadow-md shadow-primary-600/20'
                  : 'text-primary-200 hover:text-body'
              }`}
            >
              <FileText size={16} />
              CSV
            </button>
            <button
              type="button"
              onClick={() => setFormat('pdf')}
              disabled={isPending}
              className={`flex items-center gap-1.5 rounded-lg px-3.5 py-2 text-label-md font-medium transition-all ${
                format === 'pdf'
                  ? 'bg-primary-600 text-body shadow-md shadow-primary-600/20'
                  : 'text-primary-200 hover:text-body'
              }`}
            >
              <FileText size={16} />
              PDF
            </button>
          </div>

          <button
            type="button"
            onClick={handleExport}
            disabled={isPending}
            className="flex items-center gap-2 rounded-xl bg-primary-600 hover:bg-primary-700 active:scale-95 disabled:bg-primary-800 disabled:text-primary-300 disabled:scale-100 disabled:cursor-not-allowed text-body px-5 py-3 text-label-md font-semibold transition-all shadow-lg shadow-primary-600/15"
          >
            {isPending ? (
              <Loader2 size={18} className="animate-spin text-primary-300" />
            ) : (
              <Download size={18} />
            )}
            {isPending ? 'Generando...' : 'Descargar'}
          </button>
        </div>
      </div>

      {isPending && (
        <div className="relative mt-2 overflow-hidden rounded-full bg-gray-150 h-2">
          <div
            className="h-full bg-secondary rounded-full transition-all duration-300 ease-out"
            style={{ width: `${progress}%` }}
          />
          <div className="absolute right-0 top-0 text-[10px] pr-2 text-gray-650 -mt-4">
            Procesando: {progress}%
          </div>
        </div>
      )}

      {error && (
        <div className="flex items-center gap-3 rounded-xl border border-secondary-200 bg-secondary-50 p-4 text-secondary-700">
          <AlertCircle size={20} className="shrink-0 text-secondary-600" />
          <span className="text-body-md font-medium">{error}</span>
        </div>
      )}
    </div>
  );
};
