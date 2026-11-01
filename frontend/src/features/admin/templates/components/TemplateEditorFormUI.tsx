import { useState } from 'react';
import { Button } from '../../../../components/ui/Button';
import { ConfirmDialog } from '../../../../components/ui/ConfirmDialog';
import { Select } from '../../../../components/ui/Select';
import { TextInput } from '../../../../components/ui/TextInput';
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

  const updateForm = (partial: Partial<TemplateFormViewModel>) => {
    onFormChange({ ...form, ...partial });
  };

  return (
    <section className="rounded-2xl border border-gray-100 bg-body p-8 shadow-sm">
      <div className="mb-6 flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 className="text-heading-md text-primary-800">Metadatos de la plantilla</h2>
          <p className="mt-1 text-body-md text-gray-600">
            Nombre, tipo normativo y descripción. La jerarquía N1→N2→N3→Indicador se edita en el
            panel inferior.
          </p>
        </div>
        <TemplateStatusBadge status={status} />
      </div>

      <div className="grid gap-5 md:grid-cols-2">
        <TextInput
          id="template-name"
          label="Nombre"
          requiredMark
          value={form.name}
          error={fieldErrors.name}
          onChange={(event) => updateForm({ name: event.target.value })}
        />
        <Select
          id="template-type"
          label="Tipo normativo"
          requiredMark
          options={TYPE_OPTIONS}
          value={form.type}
          error={fieldErrors.type}
          onChange={(event) =>
            updateForm({ type: event.target.value as TemplateFormViewModel['type'] })
          }
        />
        <div className="md:col-span-2">
          <label htmlFor="template-description" className="mb-1 block text-label-md text-gray-600">
            Descripción
          </label>
          <textarea
            id="template-description"
            rows={3}
            value={form.description}
            className="w-full rounded-lg border border-gray-300 p-3 text-body-md text-gray-800 focus:border-primary-500 focus:outline-none"
            onChange={(event) => updateForm({ description: event.target.value })}
          />
        </div>
      </div>

      <div className="mt-8 flex flex-wrap gap-3">
        <Button type="button" onClick={onSave} isLoading={isSaving}>
          Guardar
        </Button>
        {status === 'DRAFT' && (
          <Button type="button" variant="secondary" onClick={onPublish} isLoading={isSaving}>
            Publicar
          </Button>
        )}
        {status === 'PUBLISHED' && (
          <Button type="button" variant="ghost" onClick={onArchive} isLoading={isSaving}>
            Archivar
          </Button>
        )}
        <Button type="button" variant="ghost" onClick={onDuplicate} isLoading={isSaving}>
          Duplicar
        </Button>
        {status === 'DRAFT' && (
          <Button type="button" variant="danger" onClick={() => setConfirmDeleteTemplate(true)}>
            Eliminar
          </Button>
        )}
        <Button type="button" variant="ghost" onClick={onCancel}>
          Cancelar
        </Button>
      </div>

      <ConfirmDialog
        isOpen={confirmDeleteTemplate}
        title="Eliminar plantilla"
        description="Esta acción no se puede deshacer. ¿Desea eliminar la plantilla?"
        confirmLabel="Eliminar"
        confirmVariant="danger"
        onConfirm={() => {
          setConfirmDeleteTemplate(false);
          onDelete();
        }}
        onClose={() => setConfirmDeleteTemplate(false)}
      />
    </section>
  );
}
