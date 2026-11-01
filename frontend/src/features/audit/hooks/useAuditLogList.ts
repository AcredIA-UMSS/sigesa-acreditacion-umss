import { useList2 } from '../../../api/endpoints/audit/audit';
import type { AuditLogResponse } from '../../../api/model/auditLogResponse';
import { auditErrorMessage } from '../lib/auditDisplay';
import {
  AUDIT_PAGE_SIZE,
  dateRangeError,
  toListParams,
  type AuditFilterValues,
} from '../lib/auditFilters';

export function useAuditLogList(filters: AuditFilterValues, page: number, enabled: boolean) {
  const rangeError = dateRangeError(filters);
  const params = toListParams(filters, page, AUDIT_PAGE_SIZE);
  const query = useList2(params, {
    query: {
      enabled: enabled && rangeError == null,
      queryKey: [
        'audit-logs',
        filters.actorEmail,
        filters.action,
        filters.resourceType,
        filters.result,
        filters.from,
        filters.to,
        page,
        AUDIT_PAGE_SIZE,
      ],
    },
  });
  const body = query.data?.data;

  return {
    rows: (body?.content ?? []) as AuditLogResponse[],
    totalElements: body?.totalElements ?? 0,
    totalPages: body?.totalPages ?? 0,
    rangeError,
    isLoading: enabled && query.isLoading,
    isFetching: enabled && query.isFetching,
    isError: enabled && query.isError,
    errorMessage: query.isError ? auditErrorMessage(query.error) : null,
  };
}
