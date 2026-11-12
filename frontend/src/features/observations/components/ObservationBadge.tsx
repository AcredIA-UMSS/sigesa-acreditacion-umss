import {
  COMPLIANCE_OPTIONS,
  KIND_OPTIONS,
  PRIORITY_OPTIONS,
  STATUS_OPTIONS,
  complianceClass,
  labelOf,
  priorityClass,
  statusClass,
} from '../model/observationView';

interface ObservationBadgeProps {
  value: string;
  kind: 'status' | 'priority' | 'compliance' | 'type';
}

export function ObservationBadge({ value, kind }: ObservationBadgeProps) {
  const className =
    kind === 'status'
      ? statusClass(value)
      : kind === 'priority'
        ? priorityClass(value)
        : kind === 'compliance'
          ? complianceClass(value)
          : 'border-gray-300 bg-gray-50 text-gray-700';
  const label =
    kind === 'status'
      ? labelOf(STATUS_OPTIONS, value)
      : kind === 'priority'
        ? labelOf(PRIORITY_OPTIONS, value)
        : kind === 'compliance'
          ? labelOf(COMPLIANCE_OPTIONS, value)
          : labelOf(KIND_OPTIONS, value);

  return (
    <span className={`inline-flex rounded-full border px-2 py-1 text-label-md font-medium ${className}`}>
      {label}
    </span>
  );
}
