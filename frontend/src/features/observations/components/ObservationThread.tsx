import { Button } from '../../../components/ui/Button';
import { TextInput } from '../../../components/ui/TextInput';
import type { ProcessObservation } from '../hooks/useProcessObservations';
import { formatWhen, roleLabel } from '../model/observationView';
import { ObservationBadge } from './ObservationBadge';

interface ObservationThreadProps {
  observation: ProcessObservation;
  evidenceLabel: string;
  isTechnician: boolean;
  isCoordinator: boolean;
  comment: string;
  isActing: boolean;
  onComment: (value: string) => void;
  onReview: () => void;
  onReply: () => void;
  onAttend: () => void;
  onClose: () => void;
}

export function ObservationThread({
  observation,
  evidenceLabel,
  isTechnician,
  isCoordinator,
  comment,
  isActing,
  onComment,
  onReview,
  onReply,
  onAttend,
  onClose,
}: ObservationThreadProps) {
  const needsComment =
    (isCoordinator && (observation.status === 'EN_REVISION' || observation.status === 'RESPONDIDA'))
    || (isTechnician && observation.status === 'ATENDIDA');

  return (
    <article className="rounded-xl border border-gray-200 bg-body p-4">
      <div className="flex flex-wrap items-center gap-2">
        <ObservationBadge kind="status" value={observation.status} />
        <ObservationBadge kind="type" value={observation.kind} />
        <ObservationBadge kind="priority" value={observation.priority} />
        <ObservationBadge kind="compliance" value={observation.compliance} />
      </div>
      <h3 className="mt-3 text-heading-sm font-semibold text-primary-800">{evidenceLabel}</h3>
      <p className="mt-2 text-body-md text-gray-800">{observation.description}</p>
      <p className="mt-3 text-body-md text-gray-700">
        <span className="text-label-md uppercase text-gray-500">Recomendación · </span>
        {observation.recommendation?.trim() ? observation.recommendation : 'Sin recomendación.'}
      </p>
      <p className="mt-2 text-label-md text-gray-500">
        Creada {formatWhen(observation.createdAt)} · Actualizada {formatWhen(observation.updatedAt)}
      </p>

      <h4 className="mt-5 text-label-md uppercase text-gray-600">Historial</h4>
      <ul className="mt-3 space-y-3">
        {observation.messages.length === 0 && (
          <li className="text-body-md text-gray-600">Todavía no hay respuestas.</li>
        )}
        {observation.messages.map((message) => (
          <li key={message.id} className="rounded-lg bg-gray-50 p-3">
            <p className="text-label-md text-gray-600">
              {roleLabel(message.authorRole)} · {formatWhen(message.createdAt)}
            </p>
            <p className="mt-1 text-body-md text-gray-900">{message.body}</p>
          </li>
        ))}
      </ul>

      {needsComment && (
        <div className="mt-4">
          <TextInput
            id="observation-comment"
            label={isTechnician ? 'Cierre' : 'Comentario'}
            requiredMark
            value={comment}
            onChange={(event) => onComment(event.target.value)}
          />
        </div>
      )}

      <div className="mt-4 flex flex-wrap gap-3">
        {isCoordinator && observation.status === 'ENVIADA' && (
          <Button type="button" variant="secondary" isLoading={isActing} onClick={onReview}>
            Marcar en revisión
          </Button>
        )}
        {isCoordinator && observation.status === 'EN_REVISION' && (
          <Button type="button" isLoading={isActing} onClick={onReply}>Responder</Button>
        )}
        {isCoordinator && observation.status === 'RESPONDIDA' && (
          <Button type="button" variant="secondary" isLoading={isActing} onClick={onAttend}>
            Marcar atendida
          </Button>
        )}
        {isTechnician && observation.status === 'ATENDIDA' && (
          <Button type="button" isLoading={isActing} onClick={onClose}>Cerrar</Button>
        )}
      </div>
    </article>
  );
}
