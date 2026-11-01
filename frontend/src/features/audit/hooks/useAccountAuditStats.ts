import { useList2 } from '../../../api/endpoints/audit/audit';
import { auditErrorMessage } from '../lib/auditDisplay';
import { dateRangeError, toListParams, type AuditFilterValues } from '../lib/auditFilters';

export interface AccountAuditStats {
  accounts: number;
  registered: number;
  activated: number;
  deactivated: number;
  reports: number;
}

export function useAccountAuditStats(filters: AuditFilterValues, enabled: boolean) {
  const rangeError = dateRangeError(filters);
  const canQuery = enabled && rangeError == null;
  const shared: AuditFilterValues = {
    ...filters,
    action: '',
    resourceType: '',
  };

  const registered = useCount(shared, { action: 'USER_REGISTERED' }, canQuery, 'registered');
  const activated = useCount(shared, { action: 'USER_ACTIVATED' }, canQuery, 'activated');
  const deactivated = useCount(shared, { action: 'USER_DEACTIVATED' }, canQuery, 'deactivated');
  const reports = useCount(shared, { action: 'REPORT_REQUESTED' }, canQuery, 'reports');
  const queries = [registered, activated, deactivated, reports];
  const failed = queries.find((query) => query.isError);

  return {
    stats: {
      accounts: registered.total + activated.total + deactivated.total + reports.total,
      registered: registered.total,
      activated: activated.total,
      deactivated: deactivated.total,
      reports: reports.total,
    } satisfies AccountAuditStats,
    isLoading: canQuery && queries.some((query) => query.isLoading),
    isError: Boolean(failed),
    errorMessage: failed ? auditErrorMessage(failed.error) : null,
  };
}

function useCount(
  filters: AuditFilterValues,
  patch: Partial<AuditFilterValues>,
  enabled: boolean,
  key: string,
) {
  const next = { ...filters, ...patch };
  const params = toListParams(next, 0, 1);
  const query = useList2(params, {
    query: {
      enabled,
      queryKey: ['audit-stats', key, next.actorEmail, next.action, next.resourceType, next.from, next.to, next.result],
    },
  });

  return {
    total: query.data?.data.totalElements ?? 0,
    isLoading: query.isLoading,
    isError: query.isError,
    error: query.error,
  };
}
