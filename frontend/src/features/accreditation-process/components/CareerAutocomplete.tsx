import React, { useEffect, useId, useRef, useState } from 'react';
import { Search } from 'lucide-react';
import { TextInput } from '../../../components/ui/TextInput';
import { useProgramSearch } from '../hooks/useProgramSearch';
import type { CareerOption } from './CreateProcessForm';

interface CareerAutocompleteProps {
  value: CareerOption | null;
  onChange: (career: CareerOption | null) => void;
  disabled?: boolean;
  error?: string;
}

export const CareerAutocomplete: React.FC<CareerAutocompleteProps> = ({
  value,
  onChange,
  disabled = false,
  error,
}) => {
  const listboxId = useId();
  const containerRef = useRef<HTMLDivElement>(null);
  const [inputValue, setInputValue] = useState(value?.name ?? '');
  const [isOpen, setIsOpen] = useState(false);

  const { programs, isLoading, isError } = useProgramSearch(inputValue, isOpen || inputValue.length > 0);

  useEffect(() => {
    if (value) {
      setInputValue(value.name);
    }
  }, [value]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const options: CareerOption[] = programs
    .filter((program) => program.id && program.name)
    .map((program) => ({
      id: program.id as string,
      name: program.name as string,
      code: program.code as string | undefined,
    }));

  const handleSelect = (career: CareerOption) => {
    onChange(career);
    setInputValue(career.name);
    setIsOpen(false);
  };

  const handleInputChange = (nextValue: string) => {
    setInputValue(nextValue);
    onChange(null);
    setIsOpen(true);
  };

  return (
    <div ref={containerRef} className="relative">
      <TextInput
        label="Carrera"
        placeholder="Buscar por nombre o código (ej. Sistemas, MED)..."
        value={inputValue}
        onChange={(event) => handleInputChange(event.target.value)}
        onFocus={() => setIsOpen(true)}
        disabled={disabled}
        error={error ?? (isError ? 'No se pudieron cargar las carreras.' : undefined)}
        autoComplete="off"
        role="combobox"
        aria-expanded={isOpen}
        aria-controls={listboxId}
        aria-autocomplete="list"
        prefixIcon={<Search className="size-4" aria-hidden="true" />}
      />

      {isOpen && !disabled && (
        <ul
          id={listboxId}
          role="listbox"
          className="absolute z-20 mt-1 max-h-56 w-full overflow-y-auto rounded-lg border border-gray-300 bg-body shadow-lg"
        >
          {isLoading && (
            <li className="px-3 py-2 text-body-md text-gray-600">Buscando carreras...</li>
          )}

          {!isLoading && options.length === 0 && (
            <li className="px-3 py-2 text-body-md text-gray-600">
              No hay carreras que coincidan con &quot;{inputValue}&quot;.
            </li>
          )}

          {!isLoading &&
            options.map((career) => (
              <li key={career.id}>
                <button
                  type="button"
                  role="option"
                  aria-selected={value?.id === career.id}
                  className="flex w-full flex-col items-start px-3 py-2 text-left hover:bg-primary-50 focus:bg-primary-50 focus:outline-none"
                  onMouseDown={(event) => event.preventDefault()}
                  onClick={() => handleSelect(career)}
                >
                  <span className="text-body-md text-gray-900">{career.name}</span>
                  {career.code && (
                    <span className="text-label-md text-gray-600">{career.code}</span>
                  )}
                </button>
              </li>
            ))}
        </ul>
      )}
    </div>
  );
};
