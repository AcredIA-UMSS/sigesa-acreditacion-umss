import type { ButtonHTMLAttributes, ReactNode } from 'react';

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  isLoading?: boolean;
  children: ReactNode;
}

const variantClasses: Record<ButtonVariant, string> = {
  primary: 'bg-primary-600 text-body hover:bg-primary-500 disabled:bg-primary-300',
  secondary: 'bg-secondary text-body hover:bg-secondary-600 disabled:bg-secondary-300',
  ghost: 'bg-transparent text-primary-700 hover:bg-primary-50 disabled:text-gray-400',
  danger: 'bg-danger text-body hover:bg-danger/90 disabled:bg-danger/50',
};

export function Button({
  variant = 'primary',
  isLoading = false,
  disabled,
  className = '',
  children,
  ...props
}: ButtonProps) {
  return (
    <button
      type="button"
      disabled={disabled ?? isLoading}
      className={`inline-flex items-center justify-center gap-2 rounded-lg px-4 py-3 text-label-md font-medium transition-colors disabled:cursor-not-allowed ${variantClasses[variant]} ${className}`}
      {...props}
    >
      {isLoading ? 'Procesando…' : children}
    </button>
  );
}
