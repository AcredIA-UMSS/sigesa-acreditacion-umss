import { useEffect, useState } from 'react';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { Select } from '../../../components/ui/Select';
import { isApiError } from '../../../lib/api/apiError';
import { evidenceLabel } from '../../observations/model/observationView';
import { ProcessEvidenceTrace } from '../../observations/components/ProcessEvidenceTrace';
import { useProcessObservations } from '../../observations/hooks/useProcessObservations';

interface StartDocumentsProps {
  processId: string;
  documents: string[];
}

export function StartDocuments({ processId, documents }: StartDocumentsProps) {
  const [completed, setCompleted] = useState<Record<string, boolean>>({});
  const [openDocument, setOpenDocument] = useState<string | null>(null);
  const [mode, setMode] = useState<'view' | 'observe' | null>(null);
  const [evidenceId, setEvidenceId] = useState('');
  const [description, setDescription] = useState('');
  const [formError, setFormError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const observations = useProcessObservations(processId);
  const evidenceData = observations.evidences.data;
  const evidences = evidenceData ?? [];

  useEffect(() => {
    if (evidenceData?.length === 1) {
      setEvidenceId((current) => current || evidenceData[0].evidenceId);
    }
  }, [evidenceData]);

  function toggle(document: string, nextMode: 'view' | 'observe') {
    if (openDocument === document && mode === nextMode) {
      setOpenDocument(null);
      setMode(null);
      return;
    }
    setOpenDocument(document);
    setMode(nextMode);
    setFormError(null);
    setNotice(null);
    setDescription('');
    setEvidenceId(evidences.length === 1 ? evidences[0].evidenceId : '');
  }

  async function registerObservation(document: string) {
    const text = description.trim();
    if (!evidenceId) {
      setFormError('Seleccione el documento subido por el coordinador.');
      setNotice(null);
      return;
    }
    if (text.length < 10) {
      setFormError('La observación debe tener al menos 10 caracteres.');
      setNotice(null);
      return;
    }
    setFormError(null);
    setNotice(null);
    try {
      await observations.create.mutateAsync({
        evidenceId,
        description: `${document}. ${text}`,
        kind: 'CONTENIDO',
        priority: 'MEDIA',
        compliance: 'NO_CUMPLE',
        recommendation: text,
      });
      setDescription('');
      setNotice('Observación registrada. Quedó en borrador para enviarla al coordinador.');
    } catch (error) {
      setFormError(isApiError(error) ? error.message : 'No se pudo registrar la observación.');
    }
  }

  return (
    <div className="mt-3">
      <p className="text-label-md font-medium uppercase text-gray-600">Documentos a registrar</p>
      <ul className="mt-2 space-y-3">
        {documents.map((document) => {
          const isCompleted = Boolean(completed[document]);
          const isOpen = openDocument === document;
          return (
            <li key={document} className="rounded-lg border border-gray-200 bg-body px-3 py-3">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <span className="text-body-md text-gray-800">{document}</span>
                <div className="flex flex-wrap gap-2">
                  <button
                    type="button"
                    aria-pressed={isCompleted}
                    onClick={() => setCompleted((current) => ({ ...current, [document]: !current[document] }))}
                    className={`shrink-0 rounded-full border px-2.5 py-0.5 text-label-md font-medium ${
                      isCompleted
                        ? 'border-success/30 bg-success/10 text-success'
                        : 'border-gray-300 bg-body text-gray-700 hover:bg-gray-50'
                    }`}
                  >
                    Completado
                  </button>
                  <button
                    type="button"
                    aria-expanded={isOpen && mode === 'view'}
                    onClick={() => toggle(document, 'view')}
                    className={actionClass(isOpen && mode === 'view')}
                  >
                    Ver
                  </button>
                  <button
                    type="button"
                    aria-expanded={isOpen && mode === 'observe'}
                    onClick={() => toggle(document, 'observe')}
                    className={actionClass(isOpen && mode === 'observe')}
                  >
                    Observación
                  </button>
                </div>
              </div>

              {isOpen && mode === 'view' && (
                <div className="mt-3 border-t border-gray-200 pt-3">
                  <ProcessEvidenceTrace processId={processId} mode="technician" />
                </div>
              )}

              {isOpen && mode === 'observe' && (
                <form
                  className="mt-3 space-y-3 border-t border-gray-200 pt-3"
                  onSubmit={(event) => {
                    event.preventDefault();
                    void registerObservation(document);
                  }}
                >
                  <Select
                    id={`observation-evidence-${document}`}
                    label="Documento subido"
                    value={evidenceId}
                    options={evidences.length === 0
                      ? [{ value: '', label: observations.evidences.isLoading ? 'Cargando…' : 'No hay documentos para observar' }]
                      : [{ value: '', label: 'Seleccione un documento' }, ...evidences.map((item) => ({
                        value: item.evidenceId,
                        label: evidenceLabel(item),
                      }))]}
                    onChange={(event) => setEvidenceId(event.target.value)}
                  />
                  <label className="block space-y-1" htmlFor={`observation-text-${document}`}>
                    <span className="block text-label-md text-gray-700">Observación</span>
                    <textarea
                      id={`observation-text-${document}`}
                      rows={3}
                      value={description}
                      onChange={(event) => setDescription(event.target.value)}
                      className="w-full rounded-lg border border-gray-300 bg-body px-3 py-3 text-body-md text-gray-900 outline-none transition-colors focus:border-primary-500"
                    />
                  </label>
                  {notice && <Alert variant="success">{notice}</Alert>}
                  {formError && <Alert variant="error">{formError}</Alert>}
                  <Button type="submit" disabled={observations.create.isPending} isLoading={observations.create.isPending}>
                    Registrar observación
                  </Button>
                </form>
              )}
            </li>
          );
        })}
      </ul>
    </div>
  );
}

function actionClass(active: boolean): string {
  return `shrink-0 rounded-full border px-2.5 py-0.5 text-label-md font-medium ${
    active
      ? 'border-primary-300 bg-primary-100 text-primary-800'
      : 'border-gray-300 bg-body text-gray-700 hover:bg-gray-50'
  }`;
}
