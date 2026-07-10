import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Loader2, Upload } from 'lucide-react';
import { Sidebar } from '../../../components/layout/Sidebar';
import { useListIndicators } from '../../../api/endpoints/indicator-controller/indicator-controller';
import { useUploadEvidence } from '../../../api/endpoints/evidence-upload-controller/evidence-upload-controller';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';
import type { IndicatorSummaryResponse } from '../../../api/model';

export function EvidenceUploadPage() {
  const navigate = useNavigate();
  const params = useParams();
  const preselectedIndicatorId = params.indicatorId;
  const isSubsanacion = Boolean(preselectedIndicatorId);

  const indicatorsQuery = useListIndicators();
  const uploadMutation = useUploadEvidence();

  const [indicatorId, setIndicatorId] = useState(preselectedIndicatorId ?? '');
  const [description, setDescription] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const indicators = indicatorsQuery.data?.data ?? [];
  const selectedIndicator = indicators.find(
    (item: IndicatorSummaryResponse) => item.id === indicatorId,
  );

  useEffect(() => {
    if (preselectedIndicatorId && !indicatorId) {
      setIndicatorId(preselectedIndicatorId);
    }
  }, [preselectedIndicatorId, indicatorId]);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setSubmitError(null);
    setSuccessMessage(null);

    if (indicatorsQuery.isError) {
      setSubmitError('No se pudo cargar el catálogo de indicadores. Verifique que el backend esté activo.');
      return;
    }

    if (!indicatorId || !file) {
      setSubmitError('Seleccione indicador y archivo PDF/imagen válido.');
      return;
    }

    if (!selectedIndicator?.criterionId) {
      setSubmitError(
        'No se encontró el criterio del indicador. Recargue la página o verifique su alcance de carrera (rol CC).',
      );
      return;
    }

    try {
      await uploadMutation.mutateAsync({
        indicatorId,
        params: { criterionId: selectedIndicator.criterionId, description: description || undefined },
        data: { file },
      });
      setSuccessMessage('Evidencia cargada correctamente. El indicador pasó a estado SUBIDO.');
      setTimeout(() => navigate('/dashboard'), 2000);
    } catch (error) {
      setSubmitError(getApiErrorMessage(error));
    }
  };

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="dashboard" />
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="border-b border-gray-200 bg-body px-8 py-4">
          <h1 className="text-heading-lg font-bold text-primary-800">
            {isSubsanacion ? 'Subsanar evidencia' : 'Cargar evidencia'}
          </h1>
          <p className="text-body-md text-gray-600">
            Asocie el archivo al indicador y criterio correspondiente (rol CC).
          </p>
        </header>

        <main className="flex-1 overflow-y-auto p-8">
          <form onSubmit={handleSubmit} className="mx-auto max-w-2xl space-y-6 rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
            {indicatorsQuery.isLoading ? (
              <div className="flex items-center gap-2 text-gray-500">
                <Loader2 className="animate-spin" size={20} />
                Cargando indicadores...
              </div>
            ) : indicatorsQuery.isError ? (
              <p className="text-body-md text-danger">
                No se pudo cargar indicadores. ¿Backend en http://localhost:8080?
              </p>
            ) : indicators.length === 0 ? (
              <p className="text-body-md text-gray-500">
                No hay indicadores disponibles para su carrera. Inicie sesión como CC asignado a INF-SIS.
              </p>
            ) : (
              <div>
                <label htmlFor="indicatorId" className="mb-2 block text-label-md font-medium text-gray-700">
                  Indicador
                </label>
                <select
                  id="indicatorId"
                  value={indicatorId}
                  disabled={isSubsanacion}
                  onChange={(event) => setIndicatorId(event.target.value)}
                  className="w-full rounded-xl border border-gray-300 px-3 py-2 text-body-md focus:border-primary-500 focus:outline-none"
                >
                  <option value="">Seleccione un indicador</option>
                  {indicators.map((indicator: IndicatorSummaryResponse) => (
                    <option key={indicator.id} value={indicator.id}>
                      {indicator.code} — {indicator.title}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div>
              <label htmlFor="description" className="mb-2 block text-label-md font-medium text-gray-700">
                Descripción (opcional)
              </label>
              <textarea
                id="description"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={3}
                className="w-full rounded-xl border border-gray-300 px-3 py-2 text-body-md focus:border-primary-500 focus:outline-none"
              />
            </div>

            <div>
              <label htmlFor="file" className="mb-2 block text-label-md font-medium text-gray-700">
                Archivo (PDF, imagen, Office)
              </label>
              <input
                id="file"
                type="file"
                accept=".pdf,.png,.jpg,.jpeg,.doc,.docx,.xls,.xlsx"
                onChange={(event) => setFile(event.target.files?.[0] ?? null)}
                className="w-full text-body-md"
              />
            </div>

            {submitError && <p className="text-body-md text-danger">{submitError}</p>}
            {successMessage && <p className="text-body-md text-success">{successMessage}</p>}

            <div className="flex gap-3">
              <button
                type="submit"
                disabled={uploadMutation.isPending}
                className="inline-flex items-center gap-2 rounded-xl bg-primary-600 px-4 py-2 text-label-md font-semibold text-body hover:bg-primary-700 disabled:opacity-50"
              >
                <Upload size={16} />
                {uploadMutation.isPending ? 'Enviando...' : 'Subir evidencia'}
              </button>
              <Link
                to="/dashboard"
                className="rounded-xl border border-gray-300 px-4 py-2 text-label-md font-semibold text-gray-700"
              >
                Cancelar
              </Link>
            </div>
          </form>
        </main>
      </div>
    </div>
  );
}
