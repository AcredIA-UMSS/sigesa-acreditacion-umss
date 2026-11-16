import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  getGetPeerVisitQueryKey,
  useGetPeerVisit,
  useUpsertPeerVisit,
} from '../../../api/endpoints/peer-visit/peer-visit';
import type { PeerVisitItemRequestDto } from '../../../api/model';
import { Sidebar } from '../../../components/layout/Sidebar';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';
import { useAuth } from '../../../lib/auth/useAuth';
import { useQueryClient } from '@tanstack/react-query';
import { PeerVisitAgendaView, type PeerVisitDraft } from '../components/PeerVisitAgendaView';

const EMPTY_DRAFT: PeerVisitDraft = {
  startsOn: '',
  endsOn: '',
  venue: '',
  room: '',
  status: 'SCHEDULED',
  notes: '',
  items: [],
};

export function PeerVisitAgendaPage() {
  const { processId } = useParams<{ processId: string }>();
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const [draft, setDraft] = useState<PeerVisitDraft>(EMPTY_DRAFT);
  const [formError, setFormError] = useState<string | null>(null);
  const query = useGetPeerVisit(processId ?? '', {
    query: { enabled: Boolean(processId) },
  });
  const mutation = useUpsertPeerVisit({
    mutation: {
      onSuccess: async () => {
        if (processId) {
          await queryClient.invalidateQueries({ queryKey: getGetPeerVisitQueryKey(processId) });
        }
      },
    },
  });

  const view = query.data?.data;
  const canEdit = session?.role === 'TD' || session?.role === 'JD';

  useEffect(() => {
    const visit = view?.visit;
    if (!visit) {
      setDraft(EMPTY_DRAFT);
      return;
    }
    setDraft({
      startsOn: visit.startsOn ?? '',
      endsOn: visit.endsOn ?? '',
      venue: visit.venue ?? '',
      room: visit.room ?? '',
      status: visit.status ?? 'SCHEDULED',
      notes: visit.notes ?? '',
      items: (visit.items ?? []).map((item) => ({
        id: item.id,
        type: item.type,
        title: item.title,
        scheduledAt: item.scheduledAt,
        location: item.location,
        notes: item.notes,
        done: item.done,
      })),
    });
  }, [view]);

  if (!processId) {
    return null;
  }

  const errorMessage = formError
    ?? (query.isError ? getApiErrorMessage(query.error, 'No se pudo cargar la agenda.') : null)
    ?? (mutation.isError ? getApiErrorMessage(mutation.error, 'No se pudo guardar la agenda.') : null);

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="processes" />
      <main data-app-scroll className="flex-1 overflow-y-auto px-8 py-8">
        <PeerVisitAgendaView
          processId={processId}
          view={view}
          draft={draft}
          canEdit={canEdit}
          isLoading={query.isLoading}
          isSaving={mutation.isPending}
          errorMessage={errorMessage}
          onChange={(patch) => setDraft((current) => ({ ...current, ...patch }))}
          onItemChange={(index, patch) => {
            setDraft((current) => {
              const items = [...current.items];
              items[index] = patch;
              return { ...current, items };
            });
          }}
          onAddItem={() => {
            const item: PeerVisitItemRequestDto = { type: 'DOCUMENT', title: '', done: false };
            setDraft((current) => ({ ...current, items: [...current.items, item] }));
          }}
          onRemoveItem={(index) => {
            setDraft((current) => ({
              ...current,
              items: current.items.filter((_, itemIndex) => itemIndex !== index),
            }));
          }}
          onSave={() => {
            setFormError(null);
            mutation.mutate({
              processId,
              data: {
                startsOn: draft.startsOn || undefined,
                endsOn: draft.endsOn || undefined,
                venue: draft.venue || undefined,
                room: draft.room || undefined,
                status: draft.status,
                notes: draft.notes || undefined,
                items: draft.items.filter((item) => (item.title ?? '').trim().length > 0),
              },
            });
          }}
        />
      </main>
    </div>
  );
}
