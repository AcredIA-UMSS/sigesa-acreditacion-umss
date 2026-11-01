import { useMemo } from 'react';
import { useList2 } from '../../../api/endpoints/audit/audit';
import type { AuditLogResponse } from '../../../api/model/auditLogResponse';
import { auditErrorMessage } from '../lib/auditDisplay';
import {
  AUDIT_FETCH_CAP,
  AUDIT_PAGE_SIZE,
  dateRangeError,
  toListParams,
  type AuditFilterValues,
} from '../lib/auditFilters';

interface UserAuditTarget {
  actorId?: string;
  email: string;
}

export function useUserAuditActivity(
  filters: AuditFilterValues,
  target: UserAuditTarget | null,
  page: number,
  enabled: boolean,
) {
  const rangeError = dateRangeError(filters);
  const email = target?.email.trim() ?? '';
  const actorId = target?.actorId;
  const canQuery = enabled && target != null && rangeError == null;
  const shared: AuditFilterValues = { ...filters, actorEmail: '' };

  const byActor = useList2(toListParams(shared, 0, AUDIT_FETCH_CAP, actorId), {
    query: {
      enabled: canQuery && Boolean(actorId),
      queryKey: ['audit-user-id', actorId, shared.action, shared.resourceType, shared.result, shared.from, shared.to],
    },
  });
  const byEmail = useList2(toListParams({ ...shared, actorEmail: email }, 0, AUDIT_FETCH_CAP), {
    query: {
      enabled: canQuery && email.length > 0,
      queryKey: ['audit-user-email', email, shared.action, shared.resourceType, shared.result, shared.from, shared.to],
    },
  });

  const merged = useMemo(() => {
    const rows = new Map<string, AuditLogResponse>();
    for (const row of byActor.data?.data.content ?? []) {
      if (row.id) {
        rows.set(row.id, row);
      }
    }
    for (const row of byEmail.data?.data.content ?? []) {
      if (row.id) {
        rows.set(row.id, row);
      }
    }
    return [...rows.values()].sort((left, right) => (right.occurredAt ?? '').localeCompare(left.occurredAt ?? ''));
  }, [byActor.data, byEmail.data]);

  const actorTotal = byActor.data?.data.totalElements ?? 0;
  const emailTotal = byEmail.data?.data.totalElements ?? 0;
  const truncated = actorTotal > AUDIT_FETCH_CAP || emailTotal > AUDIT_FETCH_CAP;
  const start = page * AUDIT_PAGE_SIZE;
  const isLoading = canQuery && ((Boolean(actorId) && byActor.isLoading) || (email.length > 0 && byEmail.isLoading));
  const failed = byActor.isError ? byActor.error : byEmail.isError ? byEmail.error : null;

  return {
    rows: merged.slice(start, start + AUDIT_PAGE_SIZE),
    allRows: merged,
    totalElements: merged.length,
    totalPages: Math.ceil(merged.length / AUDIT_PAGE_SIZE),
    truncated,
    rangeError,
    isLoading,
    isFetching: canQuery && (byActor.isFetching || byEmail.isFetching),
    isError: canQuery && failed != null,
    errorMessage: failed ? auditErrorMessage(failed) : null,
  };
}
