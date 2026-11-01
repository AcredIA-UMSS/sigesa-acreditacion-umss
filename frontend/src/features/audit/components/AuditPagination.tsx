import { Button } from '../../../components/ui/Button';

interface AuditPaginationProps {
  page: number;
  totalPages: number;
  totalElements: number;
  disabled: boolean;
  onPageChange: (page: number) => void;
}

export function AuditPagination({
  page,
  totalPages,
  totalElements,
  disabled,
  onPageChange,
}: AuditPaginationProps) {
  return (
    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between" data-testid="audit-pagination">
      <p className="text-body-md text-gray-600">{totalElements} registros</p>
      <div className="flex items-center gap-3">
        <Button
          type="button"
          variant="ghost"
          disabled={disabled || page <= 0}
          onClick={() => onPageChange(page - 1)}
        >
          Anterior
        </Button>
        <span className="text-body-md text-gray-700">
          Página {totalPages === 0 ? 0 : page + 1} de {totalPages}
        </span>
        <Button
          type="button"
          variant="ghost"
          disabled={disabled || page + 1 >= totalPages}
          onClick={() => onPageChange(page + 1)}
        >
          Siguiente
        </Button>
      </div>
    </div>
  );
}
