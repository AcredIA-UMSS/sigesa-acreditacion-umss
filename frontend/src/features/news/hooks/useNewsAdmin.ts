import { useState } from 'react';
import {
  useCreateNews,
  useDeleteNews,
  useFeatureNews,
  useListAdminNews,
  usePublishNews,
  useUnpublishNews,
  useUpdateNews,
} from '../../../api/endpoints/admin-news/admin-news';
import type { AdminNewsResponse, NewsWriteRequest } from '../../../api/model';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';

export type NewsFormState = {
  title: string;
  category: string;
  summary: string;
  body: string;
};

const EMPTY_FORM: NewsFormState = {
  title: '',
  category: 'EVENTO',
  summary: '',
  body: '',
};

export function useNewsAdmin() {
  const [page, setPage] = useState(0);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form, setForm] = useState<NewsFormState>(EMPTY_FORM);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const listQuery = useListAdminNews({ page, size: 20 });
  const createMutation = useCreateNews();
  const updateMutation = useUpdateNews();
  const deleteMutation = useDeleteNews();
  const publishMutation = usePublishNews();
  const unpublishMutation = useUnpublishNews();
  const featureMutation = useFeatureNews();

  const refresh = () => {
    void listQuery.refetch();
  };

  const openCreate = () => {
    setEditingId(null);
    setForm(EMPTY_FORM);
    setActionError(null);
    setIsFormOpen(true);
  };

  const openEdit = (article: AdminNewsResponse) => {
    if (article.id == null) {
      return;
    }
    setEditingId(article.id);
    setForm({
      title: article.title ?? '',
      category: article.category ?? 'EVENTO',
      summary: article.summary ?? '',
      body: article.body ?? '',
    });
    setActionError(null);
    setIsFormOpen(true);
  };

  const closeForm = () => {
    setIsFormOpen(false);
    setEditingId(null);
  };

  const saveForm = async () => {
    setActionError(null);
    const payload: NewsWriteRequest = {
      title: form.title,
      category: form.category,
      summary: form.summary,
      body: form.body,
    };
    try {
      if (editingId == null) {
        await createMutation.mutateAsync({ data: payload });
      } else {
        await updateMutation.mutateAsync({ id: editingId, data: payload });
      }
      closeForm();
      refresh();
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    }
  };

  const runAction = async (action: () => Promise<unknown>) => {
    setActionError(null);
    try {
      await action();
      refresh();
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    }
  };

  const isMutating =
    createMutation.isPending
    || updateMutation.isPending
    || deleteMutation.isPending
    || publishMutation.isPending
    || unpublishMutation.isPending
    || featureMutation.isPending;

  return {
    items: listQuery.data?.data?.content ?? [],
    page,
    setPage,
    totalPages: listQuery.data?.data?.totalPages ?? 0,
    isLoading: listQuery.isLoading,
    listError: listQuery.error ? getApiErrorMessage(listQuery.error) : null,
    actionError,
    form,
    setForm,
    isFormOpen,
    editingId,
    isMutating,
    openCreate,
    openEdit,
    closeForm,
    saveForm,
    publish: (id: string) => runAction(() => publishMutation.mutateAsync({ id })),
    unpublish: (id: string) => runAction(() => unpublishMutation.mutateAsync({ id })),
    feature: (id: string) => runAction(() => featureMutation.mutateAsync({ id })),
    remove: (id: string) => runAction(() => deleteMutation.mutateAsync({ id })),
  };
}
