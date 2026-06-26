import { useExecutiveReport } from '../hooks/useExecutiveReport';

export function ExecutiveReportPanel() {
  const {
    form,
    updateField,
    submit,
    isSubmitting,
    submitError,
    jobStatus,
    isPolling,
    download,
    isDownloading,
  } = useExecutiveReport();

  const progressPercent =
    jobStatus?.status === 'COMPLETED'
      ? 100
      : jobStatus?.status === 'IN_PROGRESS'
        ? 60
        : jobStatus?.status === 'PENDING'
          ? 20
          : 0;

  return (
    <section className="mx-auto max-w-2xl rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
      <h2 className="mb-1 text-xl font-semibold text-gray-900">Reporte ejecutivo PDF</h2>
      <p className="mb-6 text-sm text-gray-600">
        FSD-UC-014 — Generación asíncrona para [JD]. Aplica filtros y descarga el PDF.
      </p>

      <div className="grid gap-4">
        <label className="flex flex-col gap-1 text-sm">
          <span className="font-medium text-gray-700">Año de gestión</span>
          <input
            type="number"
            className="rounded border border-gray-300 px-3 py-2"
            value={form.managementYear}
            onChange={(e) => updateField('managementYear', Number(e.target.value))}
          />
        </label>

        <label className="flex flex-col gap-1 text-sm">
          <span className="font-medium text-gray-700">Facultad (UUID, opcional)</span>
          <input
            type="text"
            className="rounded border border-gray-300 px-3 py-2"
            value={form.facultyId}
            onChange={(e) => updateField('facultyId', e.target.value)}
            placeholder="Todos si vacío"
          />
        </label>

        <label className="flex flex-col gap-1 text-sm">
          <span className="font-medium text-gray-700">Programa (UUID, opcional)</span>
          <input
            type="text"
            className="rounded border border-gray-300 px-3 py-2"
            value={form.programId}
            onChange={(e) => updateField('programId', e.target.value)}
            placeholder="Todos si vacío"
          />
        </label>

        <button
          type="button"
          disabled={isSubmitting || isPolling}
          onClick={submit}
          className="rounded bg-blue-700 px-4 py-2 text-sm font-medium text-white hover:bg-blue-800 disabled:opacity-50"
        >
          {isSubmitting ? 'Encolando…' : 'Generar reporte PDF'}
        </button>

        {submitError && (
          <p className="text-sm text-red-600" role="alert">
            {submitError.message}
          </p>
        )}

        {jobStatus && (
          <div className="rounded border border-gray-100 bg-gray-50 p-4">
            <p className="text-sm text-gray-700">
              Estado: <strong>{jobStatus.status}</strong>
            </p>
            {jobStatus.errorCode && (
              <p className="mt-1 text-sm text-red-600">Error: {jobStatus.errorCode}</p>
            )}
            <div className="mt-3 h-2 w-full overflow-hidden rounded bg-gray-200">
              <div
                className="h-full bg-blue-600 transition-all duration-500"
                style={{ width: `${progressPercent}%` }}
              />
            </div>
            {jobStatus.status === 'COMPLETED' && (
              <button
                type="button"
                disabled={isDownloading}
                onClick={() => void download()}
                className="mt-4 text-sm font-medium text-blue-700 underline disabled:opacity-50"
              >
                {isDownloading ? 'Descargando…' : 'Descargar PDF'}
              </button>
            )}
          </div>
        )}
      </div>
    </section>
  );
}
