import { ArrowLeft, Download } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { CoverageMatrixResponseDto } from '../../../api/model';
import { Button } from '../../../components/ui/Button';

export interface CoverageMatrixViewProps {
  processId: string;
  matrix: CoverageMatrixResponseDto | undefined;
  isLoading: boolean;
  errorMessage: string | null;
  isDownloading: boolean;
  onDownloadPdf: () => void;
}

export function CoverageMatrixView({
  processId,
  matrix,
  isLoading,
  errorMessage,
  isDownloading,
  onDownloadPdf,
}: CoverageMatrixViewProps) {
  const totals = matrix?.totals;
  const dimensions = matrix?.dimensions ?? [];

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <Link
            to={`/procesos/${processId}`}
            className="inline-flex items-center gap-2 text-body-md text-primary-700 hover:text-primary-500"
          >
            <ArrowLeft size={16} />
            Volver al proceso
          </Link>
          <h1 className="mt-3 text-heading-xl font-semibold text-primary-800">
            Matriz de cobertura
          </h1>
          <p className="mt-1 text-body-md text-gray-700">
            Criterio → indicador → evidencia → estado. Informe imprimible, no el dossier ZIP.
          </p>
          {matrix?.careerName && (
            <p className="mt-1 text-body-md text-gray-600">{matrix.careerName}</p>
          )}
        </div>
        <Button onClick={onDownloadPdf} disabled={isLoading || isDownloading || !matrix}>
          <Download size={16} />
          {isDownloading ? 'Generando PDF…' : 'Descargar PDF'}
        </Button>
      </div>

      {isLoading && <div className="h-48 animate-pulse rounded-2xl bg-gray-200" />}

      {errorMessage && (
        <p className="rounded-xl border border-danger/30 bg-secondary-50 p-4 text-body-md text-secondary-800">
          {errorMessage}
        </p>
      )}

      {!isLoading && !errorMessage && totals && (
        <section className="grid grid-cols-2 gap-3 md:grid-cols-5">
          <Stat label="Indicadores" value={totals.indicators ?? 0} />
          <Stat label="Con evidencia" value={totals.withEvidence ?? 0} />
          <Stat label="Aprobados" value={totals.approved ?? 0} />
          <Stat label="Observados" value={totals.observed ?? 0} />
          <Stat label="Cobertura" value={`${totals.coveragePercent ?? 0}%`} />
        </section>
      )}

      {!isLoading && !errorMessage && dimensions.length === 0 && (
        <p className="rounded-2xl border border-gray-200 bg-body p-6 text-body-md text-gray-600">
          Este proceso aún no tiene indicadores en el árbol normativo.
        </p>
      )}

      {!isLoading && !errorMessage && dimensions.map((dimension) => (
        <section
          key={dimension.id ?? dimension.name}
          className="overflow-x-auto rounded-2xl border border-gray-200 bg-body shadow-sm"
        >
          <h2 className="border-b border-gray-200 px-4 py-3 text-heading-sm font-semibold text-primary-800">
            {dimension.name}
          </h2>
          <table className="min-w-full text-left text-body-md">
            <thead className="bg-gray-50 text-label-md text-gray-700">
              <tr>
                <th className="px-4 py-2 font-medium">Componente (N2)</th>
                <th className="px-4 py-2 font-medium">Criterio (N3)</th>
                <th className="px-4 py-2 font-medium">Indicador</th>
                <th className="px-4 py-2 font-medium">Estado</th>
                <th className="px-4 py-2 font-medium">Evidencias</th>
              </tr>
            </thead>
            <tbody>
              {(dimension.components ?? []).flatMap((component) =>
                (component.criteria ?? []).flatMap((criterion) =>
                  (criterion.indicators ?? []).map((indicator) => (
                    <tr key={indicator.id ?? indicator.code} className="border-t border-gray-100">
                      <td className="px-4 py-2 text-gray-700">{component.name}</td>
                      <td className="px-4 py-2 text-gray-700">{criterion.name}</td>
                      <td className="px-4 py-2 font-medium text-primary-800">
                        {indicator.code}
                        {indicator.description ? (
                          <span className="block text-body-md font-normal text-gray-600">
                            {indicator.description}
                          </span>
                        ) : null}
                      </td>
                      <td className="px-4 py-2">{indicator.status}</td>
                      <td className="px-4 py-2">{indicator.evidenceCount ?? 0}</td>
                    </tr>
                  )),
                ),
              )}
            </tbody>
          </table>
        </section>
      ))}
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-xl border border-gray-200 bg-body p-4">
      <p className="text-label-md text-gray-600">{label}</p>
      <p className="mt-1 text-heading-md font-semibold text-primary-800">{value}</p>
    </div>
  );
}
