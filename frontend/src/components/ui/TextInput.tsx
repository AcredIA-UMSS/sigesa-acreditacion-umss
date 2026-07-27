import type { InputHTMLAttributes, ReactNode } from 'react';

interface TextInputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  prefixIcon?: ReactNode;
  suffixIcon?: ReactNode;
}

export function TextInput({
  label,
  error,
  prefixIcon,
  suffixIcon,
  id,
  className = '',
  ...props
}: TextInputProps) {
  const inputId = id ?? label.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className="space-y-1">
      <label htmlFor={inputId} className="block text-label-md text-gray-700">
        {label}
      </label>
      <div className="relative">
        {prefixIcon && (
          <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">
            {prefixIcon}
          </span>
        )}
        <input
          id={inputId}
          className={`w-full rounded-lg border bg-body py-3 text-body-md text-gray-900 outline-none transition-colors placeholder:text-gray-400 focus:border-primary-500 ${
            prefixIcon ? 'pl-10' : 'pl-3'
          } ${suffixIcon ? 'pr-10' : 'pr-3'} ${
            error ? 'border-danger' : 'border-gray-300'
          } ${className}`}
          {...props}
        />
        {suffixIcon && (
          <span className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500">{suffixIcon}</span>
        )}
      </div>
      {error && <p className="text-label-md text-danger">{error}</p>}
    </div>
  );
}
