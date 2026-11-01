import type { AccountAuditStats } from '../hooks/useAccountAuditStats';

interface AccountAuditStatsProps {
  stats: AccountAuditStats;
  isLoading: boolean;
  active: 'accounts' | 'registered' | 'activated' | 'deactivated' | 'reports';
  onSelect: (action: string) => void;
}

const CARDS: { id: AccountAuditStatsProps['active']; label: string; action: string }[] = [
  { id: 'accounts', label: 'Cuentas', action: '' },
  { id: 'registered', label: 'Altas', action: 'USER_REGISTERED' },
  { id: 'activated', label: 'Activaciones', action: 'USER_ACTIVATED' },
  { id: 'deactivated', label: 'Desactivaciones', action: 'USER_DEACTIVATED' },
  { id: 'reports', label: 'Reportes', action: 'REPORT_REQUESTED' },
];

export function AccountAuditStats({ stats, isLoading, active, onSelect }: AccountAuditStatsProps) {
  return (
    <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-5" data-testid="audit-stats">
      {CARDS.map((card) => {
        const selected = card.id === active;
        return (
          <button
            key={card.id}
            type="button"
            onClick={() => onSelect(card.action)}
            className={`rounded-xl border px-4 py-3 text-left transition-colors ${
              selected
                ? 'border-secondary bg-primary-50'
                : 'border-gray-200 bg-body hover:border-primary-300'
            }`}
          >
            <p className="text-label-md text-gray-600">{card.label}</p>
            <p className="mt-1 text-heading-md text-primary-800">{isLoading ? '…' : stats[card.id]}</p>
          </button>
        );
      })}
    </div>
  );
}
