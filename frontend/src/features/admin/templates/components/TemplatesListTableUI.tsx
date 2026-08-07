import { Copy, FilePenLine, Plus, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Alert } from '../../../../components/ui/Alert';
import { Button } from '../../../../components/ui/Button';
import { Select } from '../../../../components/ui/Select';
import type { TemplateListFilters, TemplateRowViewModel } from '../lib/templateTypes';
import { TemplateStatusBadge } from './TemplateStatusBadge';

interface TemplatesListTableUIProps {
  templates: TemplateRowViewModel[];
  filters: TemplateListFilters;
  isLoading: boolean;
  isError: boolean;
  errorMessage?: string;
  isActionBusy: boolean;
  onFiltersChange: (filters: TemplateListFilters) => void;
  onPublish: (templateId: string) => void;
  onArchive: (templateId: string) => void;
  onDuplicate: (templateId: string) => void;
  onDelete: (templateId: string) => void;
}

const TYPE_OPTIONS = [
  { value: '', label: 'Todos los tipos' },
  { value: 'CEUB', label: 'CEUB' },
  { value: 'ARCU-SUR', label: 'ARCU-SUR' },
];

const STATUS_OPTIONS = [
  { value: '', label: 'Todos los estados' },
  { value: 'DRAFT', label: 'Borrador' },
  { value: 'PUBLISHED', label: 'Publicada' },
  { value: 'ARCHIVED', label: 'Archivada' },
];

export function TemplatesListTableUI({
  templates,
  filters,
  isLoading,
  isError,
  errorMessage,
  isActionBusy,
  onFiltersChange,
  onPublish,
  onArchive,
  onDuplicate,
  onDelete,
}: TemplatesListTableUIProps) {
  return (
    <section className="rounded-2xl border border-gray-100 bg-body p-8 shadow-sm">
      <div className="mb-6 flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 className="text-heading-md text-primary-800">Plantillas normativas</h2>
          <p className="mt-1 max-w-3xl text-body-md text-gray-600">
            Defina taxonomías CEUB/ARCU-SUR con fases, subfases y enlaces HTTPS de referencia.
            Solo las plantillas publicadas pueden usarse al crear procesos.
          </p>
        </div>
        <Link to="/admin/plantillas/nueva" className="shrink-0">
          <Button type="button">
            <Plus size={18} />
            Nueva plantilla
          </Button>
        </Link>
      </div>

      <div className="mb-6 grid gap-4 md:grid-cols-2">
        <Select
          label="Filtrar por tipo"
          options={TYPE_OPTIONS}
          value={filters.type}
          onChange={(event) =>
            onFiltersChange({
              ...filters,
              type: event.target.value as TemplateListFilters['type'],
            })
          }
        />
        <Select
          label="Filtrar por estado"
          options={STATUS_OPTIONS}
          value={filters.status}
          onChange={(event) =>
            onFiltersChange({
              ...filters,
              status: event.target.value as TemplateListFilters['status'],
            })
          }
        />
      </div>

      {isError && (
        <div className="mb-4">
          <Alert variant="error">{errorMessage ?? 'No fue posible cargar las plantillas.'}</Alert>
        </div>
      )}

      <div className="overflow-x-auto rounded-xl border border-gray-200">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              {['Nombre', 'Tipo', 'Estado', 'Fases', 'Subfases', 'Acciones'].map((header) => (
                <th
                  key={header}
                  className="px-4 py-3 text-left text-label-md font-medium uppercase tracking-wide text-gray-600"
                >
                  {header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 bg-body">
            {isLoading && (
              <tr>
                <td colSpan={6} className="px-4 py-10 text-center text-body-md text-gray-500">
                  Cargando plantillas…
                </td>
              </tr>
            )}

            {!isLoading && templates.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-10 text-center text-body-md text-gray-500">
                  No hay plantillas con los filtros seleccionados.
                </td>
              </tr>
            )}

            {!isLoading &&
              templates.map((template) => (
                <tr key={template.id}>
                  <td className="px-4 py-4">
                    <div>
                      <p className="text-body-md font-medium text-primary-800">{template.name}</p>
                      <p className="mt-1 text-label-md text-gray-500">{template.description}</p>
                    </div>
                  </td>
                  <td className="px-4 py-4 text-body-md text-gray-700">{template.type}</td>
                  <td className="px-4 py-4">
                    <TemplateStatusBadge status={template.status} />
                  </td>
                  <td className="px-4 py-4 text-body-md text-gray-700">{template.phaseCount}</td>
                  <td className="px-4 py-4 text-body-md text-gray-700">{template.subphaseCount}</td>
                  <td className="px-4 py-4">
                    <div className="flex flex-wrap gap-2">
                      <Link to={`/admin/plantillas/${template.id}`}>
                        <Button type="button" variant="ghost" className="px-3 py-2">
                          <FilePenLine size={16} />
                          Editar
                        </Button>
                      </Link>
                      {template.status === 'DRAFT' && (
                        <Button
                          type="button"
                          variant="secondary"
                          className="px-3 py-2"
                          disabled={isActionBusy}
                          onClick={() => onPublish(template.id)}
                        >
                          Publicar
                        </Button>
                      )}
                      {template.status === 'PUBLISHED' && (
                        <Button
                          type="button"
                          variant="ghost"
                          className="px-3 py-2"
                          disabled={isActionBusy}
                          onClick={() => onArchive(template.id)}
                        >
                          Archivar
                        </Button>
                      )}
                      <Button
                        type="button"
                        variant="ghost"
                        className="px-3 py-2"
                        disabled={isActionBusy}
                        onClick={() => onDuplicate(template.id)}
                      >
                        <Copy size={16} />
                        Duplicar
                      </Button>
                      {template.status === 'DRAFT' && (
                        <Button
                          type="button"
                          variant="danger"
                          className="px-3 py-2"
                          disabled={isActionBusy}
                          onClick={() => onDelete(template.id)}
                        >
                          <Trash2 size={16} />
                          Eliminar
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
