import { useAuth } from '../../../lib/auth/useAuth';
import { PublicSiteShell } from '../../home/PublicSiteShell';
import { PublicNewsListView } from '../components/PublicNewsListView';
import { usePublicNewsList } from '../hooks/usePublicNewsList';

export function PublicNewsListPage() {
  const news = usePublicNewsList();
  const { session } = useAuth();

  return (
    <PublicSiteShell>
      <PublicNewsListView
        items={news.items}
        page={news.page}
        totalPages={news.totalPages}
        onPageChange={news.setPage}
        isLoading={news.isLoading}
        error={news.error}
        canManage={session?.role === 'JD'}
      />
    </PublicSiteShell>
  );
}
