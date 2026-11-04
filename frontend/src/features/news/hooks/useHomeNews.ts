import { useMemo } from 'react';
import {
  useGetPublicFeaturedNews,
  useListPublicNews,
} from '../../../api/endpoints/public-news/public-news';
import type { PublicNewsSummaryResponse } from '../../../api/model';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';

const ANON = { auth: false as const };

export function useHomeNews() {
  const featuredQuery = useGetPublicFeaturedNews({ request: ANON });
  const listQuery = useListPublicNews({ page: 0, size: 6 }, { request: ANON });

  const featured: PublicNewsSummaryResponse | null =
    featuredQuery.data?.status === 200 ? (featuredQuery.data.data ?? null) : null;

  const excerpt = useMemo(() => {
    const items = listQuery.data?.data?.content ?? [];
    const principalSlug = featured?.slug;
    return items.filter((item) => item.slug != null && item.slug !== principalSlug).slice(0, 3);
  }, [featured?.slug, listQuery.data?.data?.content]);

  return {
    featured,
    excerpt,
    isLoading: featuredQuery.isLoading || listQuery.isLoading,
    error: featuredQuery.error
      ? getApiErrorMessage(featuredQuery.error)
      : listQuery.error
        ? getApiErrorMessage(listQuery.error)
        : null,
  };
}
