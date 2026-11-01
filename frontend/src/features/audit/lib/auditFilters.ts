import type { List2Params } from '../../../api/model/list2Params';
import type { AuditHistoryView } from '../historyRoutes';

export const AUDIT_PAGE_SIZE = 20;
export const AUDIT_FETCH_CAP = 100;

export interface AuditFilterValues {
  actorEmail: string;
  action: string;
  resourceType: string;
  result: string;
  from: string;
  to: string;
}

export const EMPTY_AUDIT_FILTERS: AuditFilterValues = {
  actorEmail: '',
  action: '',
  resourceType: '',
  result: '',
  from: '',
  to: '',
};

const BOLIVIA_OFFSET = '-04:00';

const ACCESS_ACTIONS = 'LOGIN,LOGOUT,SESSION_EXPIRED';
const ACCOUNT_ACTIONS = 'USER_REGISTERED,USER_ACTIVATED,USER_DEACTIVATED,REPORT_REQUESTED';

export function filtersForQuery(
  viewId: AuditHistoryView['id'],
  filters: AuditFilterValues,
): AuditFilterValues {
  if (viewId === 'access') {
    return { ...filters, action: ACCESS_ACTIONS, resourceType: '' };
  }
  if (viewId === 'accounts' && filters.action.length === 0) {
    return { ...filters, action: ACCOUNT_ACTIONS };
  }
  return filters;
}

export function normalizeFilters(
  viewId: AuditHistoryView['id'],
  filters: AuditFilterValues,
): AuditFilterValues {
  if (viewId === 'access') {
    return { ...filters, action: '', resourceType: '' };
  }
  if (viewId === 'evidence') {
    return { ...filters, resourceType: 'EVIDENCE' };
  }
  if (viewId === 'accounts') {
    return { ...filters, resourceType: '' };
  }
  return filters;
}

export function dateRangeError(filters: AuditFilterValues): string | null {
  if (filters.from && filters.to && filters.from > filters.to) {
    return 'La fecha desde no puede ser posterior a la fecha hasta.';
  }
  return null;
}

export function toListParams(
  filters: AuditFilterValues,
  page: number,
  size: number,
  actorId?: string,
): List2Params {
  return {
    actorId,
    actorEmail: filters.actorEmail.trim() || undefined,
    action: filters.action || undefined,
    resourceType: filters.resourceType || undefined,
    result: filters.result || undefined,
    from: filters.from ? `${filters.from}T00:00:00${BOLIVIA_OFFSET}` : undefined,
    to: filters.to ? `${filters.to}T23:59:59.999${BOLIVIA_OFFSET}` : undefined,
    pageable: { page, size },
  };
}
