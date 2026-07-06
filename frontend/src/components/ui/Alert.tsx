import type { ReactNode } from 'react';

type AlertVariant = 'success' | 'error' | 'info' | 'warning';

interface AlertProps {
  variant?: AlertVariant;
  title?: string;
  children: ReactNode;
}

const variantClasses: Record<AlertVariant, string> = {
  success: 'border-success/30 bg-success/10 text-success',
  error: 'border-danger/30 bg-danger/10 text-danger',
  info: 'border-info/30 bg-info/10 text-info',
  warning: 'border-warning/40 bg-warning/10 text-gray-800',
};

export function Alert({ variant = 'info', title, children }: AlertProps) {
  return (
    <div className={`rounded-lg border px-4 py-3 text-body-md ${variantClasses[variant]}`} role="alert">
      {title && <p className="mb-1 text-label-md font-medium">{title}</p>}
      <div>{children}</div>
    </div>
  );
}
