import { Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { Button } from '../../../../components/ui/Button';
import { ConfirmDialog } from '../../../../components/ui/ConfirmDialog';
import { Select } from '../../../../components/ui/Select';
import { TextInput } from '../../../../components/ui/TextInput';
import {
  createEmptyPhase,
  createEmptySubphase,
} from '../lib/templateFormMapper';
import type { TemplateFormErrors } from '../lib/templateFormValidation';
import type { TemplateFormViewModel, TemplateStatusCode } from '../lib/templateTypes';
import { TemplateStatusBadge } from './TemplateStatusBadge';

interface TemplateEditorFormUIProps {
  form: TemplateFormViewModel;
  fieldErrors: TemplateFormErrors;
  status: TemplateStatusCode;
  isSaving: boolean;
  onFormChange: (form: TemplateFormViewModel) => void;
  onSave: () => void;
  onPublish: () => void;
  onArchive: () => void;
  onDuplicate: () => void;
  onDelete: () => void;
  onCancel: () => void;
}

const TYPE_OPTIONS = [
  { value: 'CEUB', label: 'CEUB' },
  { value: 'ARCU-SUR', label: 'ARCU-SUR' },
];

export function TemplateEditorFormUI({
  form,
  fieldErrors,
  status,
  isSaving,
  onFormChange,
  onSave,
  onPublish,
  onArchive,
  onDuplicate,
  onDelete,
  onCancel,
}: TemplateEditorFormUIProps) {
  const [confirmDeleteTemplate, setConfirmDeleteTemplate] = useState(false);
  const [confirmRemovePhaseId, setConfirmRemovePhaseId] = useState<string | null>(null);
  const [confirmRemoveSubphase, setConfirmRemoveSubphase] = useState<{
    phaseClientId: string;
    subphaseClientId: string;
    name: string;
  } | null>(null);

  const updateForm = (partial: Partial<TemplateFormViewModel>) => {
    onFormChange({ ...form, ...partial });
  };

  const updatePhase = (clientId: string, partial: Partial<(typeof form.phases)[number]>) => {
    updateForm({
      phases: form.phases.map((phase) =>
        phase.clientId === clientId ? { ...phase, ...partial } : phase,
      ),
    });
  };

  const updateSubphase = (
    phaseClientId: string,
    subphaseClientId: string,
    partial: Partial<(typeof form.phases)[number]['subphases'][number]>,
  ) => {
    updateForm({
      phases: form.phases.map((phase) => {
        if (phase.clientId !== phaseClientId) {
          return phase;
        }

        return {
          ...phase,
          subphases: phase.subphases.map((subphase) =>
            subphase.clientId === subphaseClientId ? { ...subphase, ...partial } : subphase,
          ),
        };
      }),
    });
  };

  const addPhase = () => {
    updateForm({ phases: [...form.phases, createEmptyPhase(form.phases.length + 1)] });
  };

  const removePhase = (clientId: string) => {
    if (form.phases.length === 1) {
      return;
    }
    updateForm({ phases: form.phases.filter((phase) => phase.clientId !== clientId) });
  };

  const addSubphase = (phaseClientId: string) => {
    updateForm({
      phases: form.phases.map((phase) => {
        if (phase.clientId !== phaseClientId) {
          return phase;
        }
        return {
          ...phase,
          subphases: [...phase.subphases, createEmptySubphase(phase.subphases.length + 1)],
        };
      }),
    });
  };

  const removeSubphase = (phaseClientId: string, subphaseClientId: string) => {
    updateForm({
      phases: form.phases.map((phase) => {
        if (phase.clientId !== phaseClientId || phase.subphases.length === 1) {
          return phase;
        }
        return {
          ...phase,
          subphases: phase.subphases.filter((subphase) => subphase.clientId !== subphaseClientId),
        };
      }),
    });
  };

  return (
    <div className="space-y-8">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="mb-3 flex items-center gap-3">
            <h2 className="text-heading-md text-primary-800">Estructura normativa</h2>
            <TemplateStatusBadge status={status} />
          </div>
          <p className="text-body-md text-gray-600">
            Cada subfase debe incluir un enlace HTTPS a la guía o criterio normativo correspondiente.
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button type="button" variant="ghost" onClick={onCancel}>
            Volver
          </Button>
          <Button type="button" onClick={onSave} isLoading={isSaving}>
            Guardar
          </Button>
          {status === 'DRAFT' && (
            <Button type="button" variant="secondary" onClick={onPublish}>
              Publicar
            </Button>
          )}
          {status === 'PUBLISHED' && (
            <Button type="button" variant="ghost" onClick={onArchive}>
              Archivar
            </Button>
          )}
          <Button type="button" variant="ghost" onClick={onDuplicate}>
            Duplicar
          </Button>
          {status === 'DRAFT' && (
            <Button type="button" variant="danger" onClick={() => setConfirmDeleteTemplate(true)}>
              Eliminar
            </Button>
          )}
        </div>
      </div>

      <section className="rounded-2xl border border-gray-100 bg-body p-6 shadow-sm">
        <div className="grid gap-4 md:grid-cols-2">
          <TextInput
            label="Nombre de la plantilla"
            requiredMark
            value={form.name}
            error={fieldErrors.name}
            onChange={(event) => updateForm({ name: event.target.value })}
          />
          <Select
            label="Tipo normativo"
            requiredMark
            options={TYPE_OPTIONS}
            value={form.type}
            error={fieldErrors.type}
            onChange={(event) =>
              updateForm({ type: event.target.value as TemplateFormViewModel['type'] })
            }
          />
        </div>

        <div className="mt-4 space-y-1">
          <label htmlFor="template-description" className="block text-label-md text-gray-700">
            Descripción
          </label>
          <textarea
            id="template-description"
            rows={3}
            value={form.description}
            onChange={(event) => updateForm({ description: event.target.value })}
            className="w-full rounded-lg border border-gray-300 bg-body px-3 py-3 text-body-md text-gray-900 outline-none transition-colors placeholder:text-gray-400 focus:border-primary-500"
            placeholder="Resumen del propósito o convocatoria de acreditación"
          />
        </div>
      </section>

      {fieldErrors.phases && (
        <p className="text-body-md text-danger">{fieldErrors.phases}</p>
      )}

      {form.phases.map((phase, phaseIndex) => {
        const phaseError = fieldErrors.phaseErrors?.[phase.clientId];

        return (
          <section
            key={phase.clientId}
            className="rounded-2xl border border-gray-200 bg-gray-50 p-6 shadow-sm"
          >
            <div className="mb-4 flex items-center justify-between gap-4">
              <h3 className="text-heading-sm text-primary-800">Fase {phaseIndex + 1}</h3>
              <Button
                type="button"
                variant="ghost"
                className="px-3 py-2"
                disabled={form.phases.length === 1}
                onClick={() => setConfirmRemovePhaseId(phase.clientId)}
              >
                <Trash2 size={16} />
                Quitar fase
              </Button>
            </div>

            <div className="grid gap-4 md:grid-cols-3">
              <TextInput
                label="Nombre de la fase"
                requiredMark
                value={phase.name}
                error={phaseError?.name}
                onChange={(event) => updatePhase(phase.clientId, { name: event.target.value })}
              />
              <TextInput
                label="Orden"
                type="number"
                min={1}
                requiredMark
                value={String(phase.order)}
                error={phaseError?.order}
                onChange={(event) =>
                  updatePhase(phase.clientId, { order: Number.parseInt(event.target.value, 10) || 1 })
                }
              />
              <div className="md:col-span-1">
                <label htmlFor={`phase-description-${phase.clientId}`} className="block text-label-md text-gray-700">
                  Descripción
                </label>
                <input
                  id={`phase-description-${phase.clientId}`}
                  value={phase.description}
                  onChange={(event) => updatePhase(phase.clientId, { description: event.target.value })}
                  className="mt-1 w-full rounded-lg border border-gray-300 bg-body px-3 py-3 text-body-md text-gray-900 outline-none focus:border-primary-500"
                />
              </div>
            </div>

            {phaseError?.subphases && (
              <p className="mt-3 text-body-md text-danger">{phaseError.subphases}</p>
            )}

            <div className="mt-6 space-y-4">
              {phase.subphases.map((subphase, subIndex) => {
                const subphaseError = phaseError?.subphaseErrors?.[subphase.clientId];

                return (
                  <div
                    key={subphase.clientId}
                    className="rounded-xl border border-gray-200 bg-body p-4"
                  >
                    <div className="mb-4 flex items-center justify-between gap-4">
                      <h4 className="text-body-lg font-semibold text-primary-700">
                        Subfase {subIndex + 1}
                      </h4>
                      <Button
                        type="button"
                        variant="ghost"
                        className="px-3 py-2"
                        disabled={phase.subphases.length === 1}
                        onClick={() =>
                          setConfirmRemoveSubphase({
                            phaseClientId: phase.clientId,
                            subphaseClientId: subphase.clientId,
                            name: subphase.name,
                          })
                        }
                      >
                        <Trash2 size={16} />
                        Quitar
                      </Button>
                    </div>

                    <div className="grid gap-4 md:grid-cols-2">
                      <TextInput
                        label="Nombre de la subfase"
                        requiredMark
                        value={subphase.name}
                        error={subphaseError?.name}
                        onChange={(event) =>
                          updateSubphase(phase.clientId, subphase.clientId, { name: event.target.value })
                        }
                      />
                      <TextInput
                        label="Orden"
                        type="number"
                        min={1}
                        requiredMark
                        value={String(subphase.order)}
                        error={subphaseError?.order}
                        onChange={(event) =>
                          updateSubphase(phase.clientId, subphase.clientId, {
                            order: Number.parseInt(event.target.value, 10) || 1,
                          })
                        }
                      />
                      <div className="md:col-span-2">
                        <TextInput
                          label="Enlace de referencia (HTTPS)"
                          requiredMark
                          value={subphase.referenceUrl}
                          error={subphaseError?.referenceUrl}
                          placeholder="https://duea.umss.edu.bo/guia/ejemplo"
                          onChange={(event) =>
                            updateSubphase(phase.clientId, subphase.clientId, {
                              referenceUrl: event.target.value,
                            })
                          }
                        />
                      </div>
                      <div className="md:col-span-2">
                        <label
                          htmlFor={`subphase-description-${subphase.clientId}`}
                          className="block text-label-md text-gray-700"
                        >
                          Descripción auxiliar
                        </label>
                        <input
                          id={`subphase-description-${subphase.clientId}`}
                          value={subphase.description}
                          onChange={(event) =>
                            updateSubphase(phase.clientId, subphase.clientId, {
                              description: event.target.value,
                            })
                          }
                          className="mt-1 w-full rounded-lg border border-gray-300 bg-body px-3 py-3 text-body-md text-gray-900 outline-none focus:border-primary-500"
                        />
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="mt-4">
              <Button type="button" variant="ghost" onClick={() => addSubphase(phase.clientId)}>
                <Plus size={16} />
                Agregar subfase
              </Button>
            </div>
          </section>
        );
      })}

      <Button type="button" variant="ghost" onClick={addPhase}>
        <Plus size={18} />
        Agregar fase
      </Button>

      <ConfirmDialog
        isOpen={confirmDeleteTemplate}
        title="Eliminar plantilla"
        description="¿Eliminar esta plantilla en borrador? Esta acción no se puede deshacer."
        confirmLabel="Eliminar plantilla"
        onClose={() => setConfirmDeleteTemplate(false)}
        onConfirm={() => {
          setConfirmDeleteTemplate(false);
          onDelete();
        }}
      />

      <ConfirmDialog
        isOpen={confirmRemovePhaseId !== null}
        title="Quitar fase"
        description="¿Quitar esta fase del borrador de la plantilla? También se eliminarán sus subfases en el formulario."
        confirmLabel="Quitar fase"
        onClose={() => setConfirmRemovePhaseId(null)}
        onConfirm={() => {
          if (confirmRemovePhaseId) {
            removePhase(confirmRemovePhaseId);
          }
          setConfirmRemovePhaseId(null);
        }}
      />

      <ConfirmDialog
        isOpen={confirmRemoveSubphase !== null}
        title="Quitar subfase"
        description={`¿Quitar la subfase "${confirmRemoveSubphase?.name || 'sin nombre'}" del borrador?`}
        confirmLabel="Quitar subfase"
        onClose={() => setConfirmRemoveSubphase(null)}
        onConfirm={() => {
          if (confirmRemoveSubphase) {
            removeSubphase(
              confirmRemoveSubphase.phaseClientId,
              confirmRemoveSubphase.subphaseClientId,
            );
          }
          setConfirmRemoveSubphase(null);
        }}
      />
    </div>
  );
}
