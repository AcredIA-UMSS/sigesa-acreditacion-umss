import { useEvidenceUpload } from '../hooks/useEvidenceUpload';

export function EvidenceUploadPanel() {
  const {
    form,
    updateField,
    submit,
    progress,
    isLargeFile,
    isBlocked,
    result,
    error,
    isSubmitting,
  } = useEvidenceUpload();

  return (
    <section className="mx-auto max-w-2xl rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
      <h2 className="mb-1 text-xl font-semibold text-gray-900">Cargar Evidencia</h2>
      <p className="mb-6 text-sm text-gray-600">
        FSD-UC-004 — [CC] adjunta archivo con metadatos obligatorios (Indicador, Criterio, descripción).
      </p>

      <div className="grid gap-4">
        <label className="flex flex-col gap-1 text-sm">
          <span className="font-medium text-gray-700">Indicador (UUID)</span>
          <input
            type="text"
            className="rounded border border-gray-300 px-3 py-2"
            value={form.indicatorId}
            onChange={(e) => updateField('indicatorId', e.target.value)}
          />
        </label>

        <label className="flex flex-col gap-1 text-sm">
          <span className="font-medium text-gray-700">Criterio (UUID)</span>
          <input
            type="text"
            className="rounded border border-gray-300 px-3 py-2"
            value={form.criterionId}
            onChange={(e) => updateField('criterionId', e.target.value)}
          />
        </label>

        <label className="flex flex-col gap-1 text-sm">
          <span className="font-medium text-gray-700">Descripción</span>
          <textarea
            className="rounded border border-gray-300 px-3 py-2"
            rows={3}
            value={form.description}
            onChange={(e) => updateField('description', e.target.value)}
          />
        </label>

        <label className="flex flex-col gap-1 text-sm">
          <span className="font-medium text-gray-700">Archivo</span>
          <input
            type="file"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg"
            disabled={isBlocked}
            onChange={(e) => updateField('file', e.target.files?.[0] ?? null)}
          />
          {isLargeFile && (
            <span className="text-xs text-amber-700">
              Archivo &gt; 5 MB — se muestra barra de progreso (US-025).
            </span>
          )}
        </label>

        {(isSubmitting || progress > 0) && (
          <div>
            <div className="h-2 w-full overflow-hidden rounded bg-gray-200">
              <div
                className="h-full bg-emerald-600 transition-all duration-300"
                style={{ width: `${progress}%` }}
              />
            </div>
            <p className="mt-1 text-xs text-gray-600">{progress}%</p>
          </div>
        )}

        <button
          type="button"
          disabled={isSubmitting || isBlocked}
          onClick={submit}
          className="rounded bg-emerald-700 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-800 disabled:opacity-50"
        >
          {isSubmitting ? 'Cargando…' : 'Subir evidencia'}
        </button>

        {error && (
          <p className="text-sm text-red-600" role="alert">
            {error.message}
          </p>
        )}

        {result && (
          <div className="rounded border border-emerald-100 bg-emerald-50 p-4 text-sm">
            <p>
              Evidencia <strong>{result.evidenceId}</strong> v{result.version}
            </p>
            <p className="mt-1 font-mono text-xs">SHA-256: {result.contentHash}</p>
            <p className="mt-1">
              Estado indicador: <strong>{result.currentState}</strong>
            </p>
          </div>
        )}
      </div>
    </section>
  );
}
