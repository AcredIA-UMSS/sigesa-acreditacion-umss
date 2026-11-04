import { useParams } from 'react-router-dom';
import { PublicNewsDetailView } from '../components/PublicNewsDetailView';
import { usePublicNewsDetail } from '../hooks/usePublicNewsDetail';

export function PublicNewsDetailPage() {
  const { slug } = useParams();
  const detail = usePublicNewsDetail(slug ?? '');

  return (
    <PublicNewsDetailView
      article={detail.article}
      isLoading={detail.isLoading}
      error={detail.error}
    />
  );
}
