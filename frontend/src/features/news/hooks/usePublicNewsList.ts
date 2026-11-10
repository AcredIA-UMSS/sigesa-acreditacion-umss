import { useState } from 'react';
import { useListPublicNews } from '../../../api/endpoints/public-news/public-news';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';

const ANON = { auth: false as const };

export function usePublicNewsList() {
  const [page, setPage] = useState(0);
  const query = useListPublicNews({ page, size: 10 }, { request: ANON });

  return {
    items: query.data?.data?.content ?? [],
    page,
    setPage,
    totalPages: query.data?.data?.totalPages ?? 0,
    isLoading: query.isLoading,
    error: query.error ? getApiErrorMessage(query.error) : null,
  };
}
