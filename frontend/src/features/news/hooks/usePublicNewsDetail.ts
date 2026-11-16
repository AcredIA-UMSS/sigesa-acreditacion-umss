import { useGetPublicNewsBySlug } from '../../../api/endpoints/public-news/public-news';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';

const ANON = { auth: false as const };

export function usePublicNewsDetail(slug: string) {
  const query = useGetPublicNewsBySlug(slug, {
    query: { enabled: slug.length > 0 },
    request: ANON,
  });

  return {
    article: query.data?.status === 200 ? (query.data.data ?? null) : null,
    isLoading: query.isLoading,
    error: query.error ? getApiErrorMessage(query.error) : null,
  };
}
