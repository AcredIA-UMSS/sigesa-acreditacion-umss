import { useEffect, useMemo, useState } from 'react';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';
import { useUsersList } from '../../admin/users/hooks/useUsersList';
import type { AuditHistoryView } from '../historyRoutes';
import { useAccountAuditStats } from '../hooks/useAccountAuditStats';
import { useAuditLogDetail } from '../hooks/useAuditLogDetail';
import { useAuditLogList } from '../hooks/useAuditLogList';
import { useUserAuditActivity } from '../hooks/useUserAuditActivity';
import { auditErrorMessage, type ActorDirectory } from '../lib/auditDisplay';
import {
  dateRangeError,
  EMPTY_AUDIT_FILTERS,
  filtersForQuery,
  normalizeFilters,
  type AuditFilterValues,
} from '../lib/auditFilters';
import { exportAuditRows, exportFilteredAuditCsv } from '../lib/exportAuditCsv';
import { AccountAuditStats } from './AccountAuditStats';
import { AuditDetailPanel } from './AuditDetailPanel';
import { AuditFilters } from './AuditFilters';
import { AuditLogTable } from './AuditLogTable';
import { AuditPagination } from './AuditPagination';
import { AuditTimeline } from './AuditTimeline';
import { UserAuditPicker } from './UserAuditPicker';

interface AuditHistoryPanelProps {
  view: AuditHistoryView;
}

interface SelectedUser {
  id?: string;
  email: string;
  fullName: string;
}

type AccountCard = 'accounts' | 'registered' | 'activated' | 'deactivated' | 'reports';

export function AuditHistoryPanel({ view }: AuditHistoryPanelProps) {
  const initialFilters = normalizeFilters(view.id, EMPTY_AUDIT_FILTERS);
  const [draft, setDraft] = useState<AuditFilterValues>(initialFilters);
  const [applied, setApplied] = useState<AuditFilterValues>(initialFilters);
  const [page, setPage] = useState(0);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [selectedUser, setSelectedUser] = useState<SelectedUser | null>(null);
  const [presentation, setPresentation] = useState<'table' | 'timeline'>('table');
  const [exporting, setExporting] = useState(false);
  const [exportNotice, setExportNotice] = useState<{ tone: 'info' | 'error'; text: string } | null>(null);

  useEffect(() => {
    const next = normalizeFilters(view.id, EMPTY_AUDIT_FILTERS);
    setDraft(next);
    setApplied(next);
    setPage(0);
    setSelectedId(null);
    setSelectedUser(null);
    setPresentation('table');
    setExportNotice(null);
  }, [view.id]);

  const usersList = useUsersList();
  const directory = useMemo<ActorDirectory>(() => {
    const byId = new Map<string, { fullName: string; email: string }>();
    const byEmail = new Map<string, { fullName: string; email: string }>();
    for (const user of usersList.users) {
      const profile = { fullName: user.fullName, email: user.email };
      byId.set(user.userId, profile);
      byEmail.set(user.email.toLowerCase(), profile);
    }
    return { byId, byEmail };
  }, [usersList.users]);

  const queried = filtersForQuery(view.id, applied);
  const serverList = useAuditLogList(queried, page, view.id !== 'user');
  const userList = useUserAuditActivity(
    applied,
    selectedUser ? { actorId: selectedUser.id, email: selectedUser.email } : null,
    page,
    view.id === 'user',
  );
  const stats = useAccountAuditStats(applied, view.id === 'accounts');
  const detail = useAuditLogDetail(selectedId);
  const list = view.id === 'user' ? userList : serverList;
  const showResults = view.id !== 'user' || selectedUser != null;
  const rangeError = dateRangeError(draft);

  const applyFilters = (nextDraft: AuditFilterValues) => {
    const next = normalizeFilters(view.id, nextDraft);
    if (dateRangeError(next)) {
      setDraft(next);
      return;
    }
    setDraft(next);
    setApplied(next);
    setPage(0);
    setSelectedId(null);
    setExportNotice(null);
  };

  const exportReport = async () => {
    setExporting(true);
    setExportNotice(null);
    try {
      const result = view.id === 'user'
        ? exportAuditRows(view, userList.allRows, directory, userList.truncated)
        : await exportFilteredAuditCsv(view, queried, directory);
      if (result.rowCount === 0) {
        setExportNotice({ tone: 'info', text: 'No hay registros para exportar.' });
      } else if (result.truncated) {
        setExportNotice({
          tone: 'info',
          text: 'Se exportaron los registros más recientes de esta consulta.',
        });
      }
    } catch (error) {
      setExportNotice({ tone: 'error', text: auditErrorMessage(error) });
    } finally {
      setExporting(false);
    }
  };

  const accountActive = (): AccountCard => {
    if (applied.action === 'USER_REGISTERED') return 'registered';
    if (applied.action === 'USER_ACTIVATED') return 'activated';
    if (applied.action === 'USER_DEACTIVATED') return 'deactivated';
    if (applied.action === 'REPORT_REQUESTED') return 'reports';
    return 'accounts';
  };

  return (
    <div className="space-y-6" data-testid="audit-history-panel">
      <div className="flex flex-wrap items-center justify-end gap-3">
        <Button
          type="button"
          variant="secondary"
          isLoading={exporting}
          disabled={!showResults || list.isLoading}
          onClick={exportReport}
          data-testid="audit-export"
        >
          Exportar CSV
        </Button>
      </div>

      {view.id === 'accounts' && (
        <AccountAuditStats
          stats={stats.stats}
          isLoading={stats.isLoading}
          active={accountActive()}
          onSelect={(action) => applyFilters({ ...applied, action })}
        />
      )}
      {view.id === 'accounts' && stats.isError && stats.errorMessage && (
        <Alert variant="error">{stats.errorMessage}</Alert>
      )}

      {view.id === 'user' && (
        <UserAuditPicker
          users={usersList.users}
          isLoading={usersList.isLoading}
          errorMessage={usersList.isError ? getApiErrorMessage(usersList.error) : undefined}
          selected={selectedUser}
          onSelect={(user) => {
            setSelectedUser(user);
            setPage(0);
            setSelectedId(null);
          }}
          onConsultEmail={(email) => {
            const known = usersList.users.find((user) => user.email.toLowerCase() === email.toLowerCase());
            setSelectedUser(known
              ? { id: known.userId, email: known.email, fullName: known.fullName }
              : { email, fullName: '—' });
            setPage(0);
            setSelectedId(null);
          }}
        />
      )}

      <AuditFilters
        viewId={view.id}
        values={draft}
        rangeError={rangeError}
        onChange={setDraft}
        onSearch={() => applyFilters(draft)}
        onClear={() => applyFilters(EMPTY_AUDIT_FILTERS)}
      />

      {exportNotice && <Alert variant={exportNotice.tone === 'error' ? 'error' : 'info'}>{exportNotice.text}</Alert>}
      {showResults && list.isError && list.errorMessage && <Alert variant="error" data-testid="audit-error">{list.errorMessage}</Alert>}
      {view.id === 'user' && userList.truncated && (
        <Alert variant="info">
          Se reúnen hasta 100 registros de cada consulta. Acote las fechas si necesita un tramo más preciso.
        </Alert>
      )}
      {view.id === 'user' && !selectedUser && (
        <p className="text-body-md text-gray-600" data-testid="audit-user-prompt">
          Seleccione un usuario para ver su actividad.
        </p>
      )}

      {showResults && !list.isError && (
        <>
          {view.id === 'user' && (
            <div className="flex gap-2" role="tablist" aria-label="Presentación del historial">
              <Button type="button" variant={presentation === 'table' ? 'primary' : 'ghost'} onClick={() => setPresentation('table')}>
                Tabla
              </Button>
              <Button type="button" variant={presentation === 'timeline' ? 'primary' : 'ghost'} onClick={() => setPresentation('timeline')}>
                Línea de tiempo
              </Button>
            </div>
          )}
          {(view.id !== 'user' || presentation === 'table') && (
            <AuditLogTable
              viewId={view.id}
              rows={list.rows}
              directory={directory}
              isLoading={list.isLoading}
              selectedId={selectedId}
              onSelect={setSelectedId}
            />
          )}
          {view.id === 'user' && presentation === 'timeline' && !list.isLoading && (
            <AuditTimeline
              rows={list.rows}
              directory={directory}
              selectedId={selectedId}
              onSelect={setSelectedId}
            />
          )}
          {view.id === 'user' && presentation === 'timeline' && list.isLoading && (
            <p className="text-body-md text-gray-500" data-testid="audit-loading">Cargando historial…</p>
          )}
          <AuditPagination
            page={page}
            totalPages={list.totalPages}
            totalElements={list.totalElements}
            disabled={list.isFetching}
            onPageChange={(nextPage) => {
              setPage(nextPage);
              setSelectedId(null);
            }}
          />
        </>
      )}

      {selectedId && (
        <AuditDetailPanel
          event={detail.event}
          directory={directory}
          isLoading={detail.isLoading}
          errorMessage={detail.errorMessage}
          onClose={() => setSelectedId(null)}
        />
      )}
    </div>
  );
}
