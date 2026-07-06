import type { SelectHTMLAttributes } from 'react';

interface SelectOption {
  value: string;
  label: string;
}

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  options: SelectOption[];
  error?: string;
  helperText?: string;
}

export function Select({
  label,
  options,
  error,
  helperText,
  id,
  className = '',
  ...props
}: SelectProps) {
  const selectId = id ?? label.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className="space-y-1">
      <label htmlFor={selectId} className="block text-label-md text-gray-700">
        {label}
      </label>
      <select
        id={selectId}
        className={`w-full rounded-lg border bg-body px-3 py-3 text-body-md text-gray-900 outline-none transition-colors focus:border-primary-500 disabled:cursor-not-allowed disabled:bg-gray-100 disabled:text-gray-500 ${
          error ? 'border-danger' : 'border-gray-300'
        } ${className}`}
        {...props}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {helperText && !error && <p className="text-label-md text-gray-500">{helperText}</p>}
      {error && <p className="text-label-md text-danger">{error}</p>}
    </div>
  );
}
