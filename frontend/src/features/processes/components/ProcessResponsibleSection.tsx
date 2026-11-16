import { UserCheck, UserMinus, UserPlus, X } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { EligibleResponsibleDto } from '../../../api/model';
import { Button } from '../../../components/ui/Button';
import type { ProcessResponsibleDto } from '../hooks/useProcessResponsible';

interface AssignResponsibleModalUIProps {
  isOpen: boolean;
  careerName?: string;
  candidates: EligibleResponsibleDto[];
  selectedUserId: string;
  isLoadingCandidates: boolean;
  isSubmitting: boolean;
  submitError: string | null;
  loadError: string | null;
  onClose: () => void;
  onSelectUser: (userId: string) => void;
  onSubmit: () => void;
}

export function AssignResponsibleModalUI({
  isOpen,
  careerName,
  candidates,
  selectedUserId,
  isLoadingCandidates,
  isSubmitting,
  submitError,
  loadError,
  onClose,
  onSelectUser,
  onSubmit,
}: AssignResponsibleModalUIProps) {
  if (!isOpen) {
    return null;
  }

  const selectable = candidates.filter((candidate) => Boolean(candidate.userId));
  const careerLabel = careerName?.trim() ? careerName.trim() : 'esta carrera';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button
        type="button"
        className="absolute inset-0 bg-primary-900/50 backdrop-blur-[2px]"
        aria-label="Cerrar modal"
        onClick={onClose}
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="assign-responsible-title"
        className="relative z-10 w-full max-w-lg rounded-2xl border border-gray-200 bg-body shadow-2xl"
      >
        <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
          <h2 id="assign-responsible-title" className="text-heading-md font-semibold text-primary-800">
            Asignar responsable
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-primary-700"
            aria-label="Cerrar"
          >
            <X size={20} />
          </button>
        </div>

        <div className="space-y-4 px-6 py-5">
          <p className="text-body-md text-gray-600">
            Solo se listan Coordinadores de Carrera [CC] activos asignados a{' '}
            <span className="font-medium text-primary-800">{careerLabel}</span>.
          </p>

          {isLoadingCandidates && (
            <p className="text-body-md text-gray-600">Cargando coordinadores de la carrera…</p>
          )}

          {!isLoadingCandidates && loadError && (
            <p className="rounded-lg border border-danger/30 bg-danger/10 px-4 py-3 text-body-md text-danger">
              {loadError}
            </p>
          )}

          {!isLoadingCandidates && !loadError && selectable.length === 0 && (
            <div className="rounded-lg border border-warning/40 bg-warning/10 px-4 py-3 text-body-md text-gray-800">
              <p>
                No hay coordinadores [CC] activos asignados a {careerLabel}. Asigne un coordinador a
                esa carrera en Usuarios y vuelva a intentar.
              </p>
              <Link
                to="/admin/users"
                className="mt-2 inline-block text-label-md font-medium text-primary-700 underline"
              >
                Ir a administración de usuarios
              </Link>
            </div>
          )}

          {!isLoadingCandidates && !loadError && selectable.length > 0 && (
            <fieldset className="space-y-2">
              <legend className="text-label-md text-gray-700">
                Coordinador responsable
                <span className="ml-0.5 text-secondary">*</span>
              </legend>
              <ul className="max-h-64 space-y-2 overflow-y-auto">
                {selectable.map((candidate) => {
                  const userId = candidate.userId ?? '';
                  const selected = selectedUserId === userId;
                  return (
                    <li key={userId}>
                      <label
                        className={`flex cursor-pointer items-start gap-3 rounded-xl border px-4 py-3 transition-colors ${
                          selected
                            ? 'border-primary-500 bg-primary-50'
                            : 'border-gray-200 bg-body hover:border-primary-300'
                        }`}
                      >
                        <input
                          type="radio"
                          name="responsible-candidate"
                          className="mt-1"
                          value={userId}
                          checked={selected}
                          onChange={() => onSelectUser(userId)}
                          disabled={isSubmitting}
                        />
                        <span>
                          <span className="block text-body-md font-medium text-primary-900">
                            {candidate.fullName ?? 'Sin nombre'}
                          </span>
                          <span className="block text-body-md text-gray-600">
                            {candidate.email ?? '—'}
                          </span>
                        </span>
                      </label>
                    </li>
                  );
                })}
              </ul>
            </fieldset>
          )}

          {submitError && (
            <p className="rounded-lg border border-danger/30 bg-danger/10 px-4 py-3 text-body-md text-danger">
              {submitError}
            </p>
          )}
        </div>

        <div className="flex justify-end gap-3 border-t border-gray-100 px-6 py-4">
          <Button variant="ghost" onClick={onClose} disabled={isSubmitting}>
            Cancelar
          </Button>
          <Button onClick={onSubmit} isLoading={isSubmitting} disabled={!selectedUserId || selectable.length === 0}>
            <UserPlus size={16} />
            Confirmar asignación
          </Button>
        </div>
      </div>
    </div>
  );
}

interface ProcessResponsibleSectionUIProps {
  responsible?: ProcessResponsibleDto | null;
  canManage: boolean;
  isRemoving: boolean;
  onOpenAssign: () => void;
  onRemove: () => void;
}

export function ProcessResponsibleSectionUI({
  responsible,
  canManage,
  isRemoving,
  onOpenAssign,
  onRemove,
}: ProcessResponsibleSectionUIProps) {
  return (
    <section className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 className="text-heading-lg font-semibold text-primary-800">Responsable del proceso</h2>
          <p className="mt-1 text-body-md text-gray-600">
            Coordinador [CC] designado para liderar operativamente esta acreditación.
          </p>
        </div>
        {canManage && (
          <div className="flex flex-wrap gap-2">
            <Button variant="secondary" onClick={onOpenAssign}>
              <UserPlus size={16} />
              {responsible ? 'Cambiar responsable' : 'Asignar responsable'}
            </Button>
            {responsible && (
              <Button variant="ghost" onClick={onRemove} isLoading={isRemoving}>
                <UserMinus size={16} />
                Quitar
              </Button>
            )}
          </div>
        )}
      </div>

      <div className="mt-6 rounded-xl border border-gray-100 bg-gray-50 px-5 py-4">
        {responsible ? (
          <div className="flex items-start gap-3">
            <div className="rounded-full bg-primary-100 p-2 text-primary-700">
              <UserCheck size={20} />
            </div>
            <div>
              <p className="text-body-md font-semibold text-primary-900">
                {responsible.fullName ?? 'Coordinador'}
              </p>
              <p className="text-body-md text-gray-600">{responsible.email ?? '—'}</p>
            </div>
          </div>
        ) : (
          <p className="text-body-md text-gray-600">Sin responsable asignado.</p>
        )}
      </div>
    </section>
  );
}
