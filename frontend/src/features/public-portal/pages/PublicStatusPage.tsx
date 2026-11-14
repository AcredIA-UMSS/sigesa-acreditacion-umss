import { PublicSiteShell } from '../../home/PublicSiteShell';
import { PublicStatusView } from '../components/PublicStatusView';
import { usePublicPortal } from '../hooks/usePublicPortal';

export function PublicStatusPage() {
  const portal = usePublicPortal();

  return (
    <PublicSiteShell>
      <PublicStatusView
        searchQuery={portal.searchQuery}
        onSearchChange={portal.setSearchQuery}
        programs={portal.filteredPrograms}
        selectedSlug={portal.selectedSlug}
        onSelect={portal.selectProgram}
        status={portal.status}
        isListLoading={portal.isListLoading}
        isStatusLoading={portal.isStatusLoading}
        listError={portal.listError}
        statusError={portal.statusError}
        onDownload={() => {
          void portal.downloadCertificate();
        }}
        isDownloading={portal.isDownloading}
        downloadError={portal.downloadError}
      />
    </PublicSiteShell>
  );
}
