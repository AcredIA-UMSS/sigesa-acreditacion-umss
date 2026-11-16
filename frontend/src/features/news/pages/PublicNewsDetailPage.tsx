import { useParams } from 'react-router-dom';
import { PublicSiteShell } from '../../home/PublicSiteShell';
import { PublicNewsDetailView } from '../components/PublicNewsDetailView';
import { usePublicNewsDetail } from '../hooks/usePublicNewsDetail';

export function PublicNewsDetailPage() {
  const { slug } = useParams();
  const detail = usePublicNewsDetail(slug ?? '');

  return (
    <PublicSiteShell>
      <PublicNewsDetailView
        article={detail.article}
        isLoading={detail.isLoading}
        error={detail.error}
      />
    </PublicSiteShell>
  );
}
