import { PublicSiteShell } from '../../home/PublicSiteShell';
import { PublicNewsListView } from '../components/PublicNewsListView';
import { usePublicNewsList } from '../hooks/usePublicNewsList';

export function PublicNewsListPage() {
  const news = usePublicNewsList();

  return (
    <PublicSiteShell>
      <PublicNewsListView
        items={news.items}
        page={news.page}
        totalPages={news.totalPages}
        onPageChange={news.setPage}
        isLoading={news.isLoading}
        error={news.error}
      />
    </PublicSiteShell>
  );
}
