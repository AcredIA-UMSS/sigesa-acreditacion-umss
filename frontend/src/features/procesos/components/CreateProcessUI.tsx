import { ArrowLeft, CheckCircle, Info, AlertTriangle, Loader2 } from 'lucide-react';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { Select } from '../../../components/ui/Select';

interface SelectOption {
  value: string;
  label: string;
}

interface SelectedTemplateInfo {
  type: string;
  taxonomyVersion: string;
}

interface CreateProcessUIProps {
  careerId: string;
  templateId: string;
  period: string;
  fieldErrors: Partial<Record<'careerId' | 'templateId' | 'period', string>>;
  submitError: string | null;
  successMessage: string | null;
  isPending: boolean;
  isProgramsLoading: boolean;
  isProgramsError: boolean;
  isTemplatesLoading: boolean;
  isTemplatesError: boolean;
  programOptions: SelectOption[];
  templateOptions: SelectOption[];
  periodOptions: SelectOption[];
  selectedTemplate: SelectedTemplateInfo | undefined;
  onCareerIdChange: (value: string) => void;
  onTemplateIdChange: (value: string) => void;
  onPeriodChange: (value: string) => void;
  onSubmit: (event: React.FormEvent) => void;
  onCancel: () => void;
  onBack: () => void;
}

export function CreateProcessUI({
  careerId,
  templateId,
  period,
  fieldErrors,
  submitError,
  successMessage,
  isPending,
  isProgramsLoading,
  isProgramsError,
  isTemplatesLoading,
  isTemplatesError,
  programOptions,
  templateOptions,
  periodOptions,
  selectedTemplate,
  onCareerIdChange,
  onTemplateIdChange,
  onPeriodChange,
  onSubmit,
  onCancel,
  onBack,
}: CreateProcessUIProps) {
  return (
    <div className="flex h-screen flex-1 flex-col overflow-hidden bg-gray-50">
      <header className="flex items-center justify-between border-b border-gray-200 bg-body px-8 py-4">
        <div className="flex items-center gap-3 text-body-md text-gray-500">
          <button
            type="button"
            onClick={onBack}
            className="flex items-center gap-1 text-primary-600 transition-colors hover:text-primary-800"
          >
            <ArrowLeft size={16} /> Dashboard
          </button>
          <span>/</span>
          <span>Gestión de procesos de acreditación</span>
        </div>
      </header>

      <main className="flex-1 overflow-y-auto p-8">
        <div className="mx-auto max-w-3xl">
          <div className="mb-8">
            <div className="mb-4 h-1 w-12 bg-secondary" />
            <h1 className="mb-2 text-heading-xl text-primary-800">
              Inicializar Nuevo Proceso de Acreditación
            </h1>
            <p className="text-body-lg text-gray-600">
              Complete los campos requeridos por el backend para crear un proceso activo bajo la
              regla FSD-BR-08 (un solo proceso activo por tipo, carrera y periodo).
            </p>
          </div>

          {submitError && (
            <div className="mb-6">
              <Alert variant="error" title="Error al crear proceso">
                <div className="flex items-center gap-2">
                  <AlertTriangle size={18} />
                  <span>{submitError}</span>
                </div>
              </Alert>
            </div>
          )}

          {successMessage && (
            <div className="mb-6">
              <Alert variant="success" title="Proceso creado">
                <div className="flex items-center gap-2">
                  <CheckCircle size={18} />
                  <span>{successMessage} Redirigiendo al dashboard…</span>
                </div>
              </Alert>
            </div>
          )}

          {isProgramsError && (
            <div className="mb-6">
              <Alert variant="error" title="Catálogo de programas">
                No se pudo cargar el catálogo de programas. Verifique que el backend esté activo.
              </Alert>
            </div>
          )}

          {isTemplatesError && (
            <div className="mb-6">
              <Alert variant="error" title="Plantillas normativas">
                No se pudo cargar el catálogo de plantillas desde GET /templates.
              </Alert>
            </div>
          )}

          <form onSubmit={onSubmit} className="space-y-6">
            <section className="rounded-2xl border border-gray-100 bg-body p-8 shadow-sm">
              <div className="mb-6 flex items-center gap-4">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-50 text-primary-600">
                  <Info size={24} />
                </div>
                <div>
                  <h2 className="text-heading-md text-primary-800">Parámetros del proceso</h2>
                  <p className="text-body-md text-gray-500">
                    Campos del contrato `CreateProcessRequest`
                  </p>
                </div>
              </div>

              {isProgramsLoading || isTemplatesLoading ? (
                <div className="flex items-center gap-2 py-8 text-gray-500">
                  <Loader2 className="animate-spin" size={20} />
                  <span className="text-body-md">Cargando catálogos…</span>
                </div>
              ) : (
                <div className="space-y-4">
                  <Select
                    label="Programa / Carrera"
                    value={careerId}
                    onChange={(e) => onCareerIdChange(e.target.value)}
                    options={[{ value: '', label: 'Seleccione un programa' }, ...programOptions]}
                    error={fieldErrors.careerId}
                    disabled={isPending}
                  />

                  <Select
                    label="Plantilla normativa"
                    value={templateId}
                    onChange={(e) => onTemplateIdChange(e.target.value)}
                    options={[{ value: '', label: 'Seleccione una plantilla validada' }, ...templateOptions]}
                    error={fieldErrors.templateId}
                    helperText="Plantillas validadas expuestas por GET /api/v1/templates."
                    disabled={isPending}
                  />

                  {selectedTemplate && (
                    <div className="rounded-xl border border-primary-100 bg-primary-50 px-4 py-3 text-body-md text-primary-800">
                      <span className="font-semibold">Tipo:</span> {selectedTemplate.type}
                      <span className="mx-2 text-primary-300">·</span>
                      <span className="font-semibold">Taxonomía:</span>{' '}
                      {selectedTemplate.taxonomyVersion}
                    </div>
                  )}

                  <Select
                    label="Periodo académico"
                    value={period}
                    onChange={(e) => onPeriodChange(e.target.value)}
                    options={[{ value: '', label: 'Seleccione el periodo' }, ...periodOptions]}
                    error={fieldErrors.period}
                    helperText='Formato seed: "2026-1", "2025-2".'
                    disabled={isPending}
                  />
                </div>
              )}
            </section>

            <section className="rounded-2xl border border-gray-200 bg-gray-100 p-6">
              <h3 className="mb-3 text-heading-sm text-primary-800">Notas</h3>
              <ul className="list-disc space-y-2 pl-5 text-body-md text-gray-600">
                <li>
                  La regla FSD-BR-08 se valida en servidor (HTTP 409 si ya existe un proceso
                  activo).
                </li>
                <li>
                  Plantillas no validadas (p. ej. DRAFT-0.1) devuelven HTTP 422
                  `TEMPLATE_NOT_VALID`.
                </li>
                <li>
                  Responsables, cronograma y facultades no forman parte del DTO actual y no se
                  envían al backend.
                </li>
              </ul>
            </section>

            <div className="flex justify-end gap-4 rounded-2xl border border-gray-100 bg-body p-6 shadow-sm">
              <Button variant="ghost" onClick={onCancel} disabled={isPending}>
                Cancelar
              </Button>
              <Button type="submit" variant="primary" isLoading={isPending} disabled={isProgramsLoading || isTemplatesLoading}>
                Inicializar Proceso
              </Button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
