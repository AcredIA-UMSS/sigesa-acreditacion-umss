import { useGet } from '../../../api/endpoints/audit/audit';
import { auditErrorMessage } from '../lib/auditDisplay';

export function useAuditLogDetail(id: string | null) {
  const query = useGet(id ?? '', {
    query: { enabled: Boolean(id) },
  });

  return {
    event: query.data?.data,
    isLoading: Boolean(id) && query.isLoading,
    isError: Boolean(id) && query.isError,
    errorMessage: query.isError ? auditErrorMessage(query.error) : null,
  };
}
