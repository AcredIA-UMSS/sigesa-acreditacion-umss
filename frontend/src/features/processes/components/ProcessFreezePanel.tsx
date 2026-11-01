import { Lock, Unlock } from 'lucide-react';
import { Button } from '../../../components/ui/Button';

export type ProcessFreezeView = {
  id?: string;
  mode?: string;
  active?: boolean;
  frozenAt?: string;
};

export type ProcessFreezePanelProps = {
  freeze?: ProcessFreezeView;
  canFreeze: boolean;
  canLift: boolean;
  isLoading: boolean;
  isMutating: boolean;
  errorMessage?: string;
  onFreeze: () => void;
  onLift: (targetMode: 'ACTIVE' | 'IMPROVEMENT_MONITORING') => void;
};

export function ProcessFreezePanel({
  freeze,
  canFreeze,
  canLift,
  isLoading,
  isMutating,
  errorMessage,
  onFreeze,
  onLift,
}: ProcessFreezePanelProps) {
  const active = freeze?.active === true;

  return (
    <section className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 className="text-heading-lg font-semibold text-primary-800">
            Congelamiento de expediente
          </h2>
          <p className="mt-1 text-body-md text-gray-600">
            El Técnico DUEA congela el proceso en modo auditoría (solo lectura) y lo levanta
            hacia operación activa o monitoreo del Plan de Mejoras.
          </p>
        </div>
      </div>

      {isLoading ? (
        <p className="text-body-md text-gray-600">Consultando estado de freeze…</p>
      ) : (
        <p className="text-body-md text-gray-700">
          Estado: {active ? 'Freeze AUDIT_READONLY activo' : 'Sin freeze de auditoría activo'}
          {freeze?.mode ? ` · modo ${freeze.mode}` : ''}
        </p>
      )}

      {errorMessage && <p className="mt-3 text-body-md text-danger">{errorMessage}</p>}

      {(canFreeze || canLift) && (
        <div className="mt-4 flex flex-wrap gap-2">
          {canFreeze && !active && (
            <Button variant="primary" isLoading={isMutating} onClick={onFreeze}>
              <Lock size={16} />
              Congelar expediente
            </Button>
          )}
          {canLift && active && (
            <>
              <Button
                variant="secondary"
                isLoading={isMutating}
                onClick={() => onLift('ACTIVE')}
              >
                <Unlock size={16} />
                Levantar a operativo
              </Button>
              <Button
                variant="ghost"
                isLoading={isMutating}
                onClick={() => onLift('IMPROVEMENT_MONITORING')}
              >
                Levantar a monitoreo
              </Button>
            </>
          )}
        </div>
      )}
    </section>
  );
}
