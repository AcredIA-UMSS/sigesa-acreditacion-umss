import { useMemo, useState } from 'react';
import { Button } from '../../../components/ui/Button';
import { TextInput } from '../../../components/ui/TextInput';
import type { UserRowViewModel } from '../../admin/users/hooks/useUsersList';

const INSTITUTIONAL_EMAIL = /^[^@\s]+@umss\.edu\.bo$/i;

interface UserAuditPickerProps {
  users: UserRowViewModel[];
  isLoading: boolean;
  errorMessage?: string;
  selected: { id?: string; email: string; fullName: string } | null;
  onSelect: (user: { id: string; email: string; fullName: string }) => void;
  onConsultEmail: (email: string) => void;
}

export function UserAuditPicker({
  users,
  isLoading,
  errorMessage,
  selected,
  onSelect,
  onConsultEmail,
}: UserAuditPickerProps) {
  const [query, setQuery] = useState('');
  const [emailError, setEmailError] = useState<string | null>(null);
  const matches = useMemo(() => {
    const term = query.trim().toLowerCase();
    if (!term) {
      return users.slice(0, 8);
    }
    return users
      .filter((user) => user.fullName.toLowerCase().includes(term) || user.email.toLowerCase().includes(term))
      .slice(0, 8);
  }, [query, users]);

  const consultTypedEmail = () => {
    const email = query.trim();
    if (!INSTITUTIONAL_EMAIL.test(email)) {
      setEmailError('Ingrese un correo institucional @umss.edu.bo.');
      return;
    }
    setEmailError(null);
    onConsultEmail(email);
  };

  return (
    <section className="rounded-2xl border border-gray-100 bg-body p-6 shadow-sm" data-testid="audit-user-picker">
      <h2 className="text-heading-sm text-primary-800">Usuario</h2>
      <p className="mt-1 text-body-md text-gray-600">
        Busque por nombre o correo y seleccione la cuenta cuya actividad quiere consultar.
      </p>
      {selected && (
        <p className="mt-3 text-body-md text-primary-800" data-testid="audit-selected-user">
          {selected.fullName !== '—' ? `${selected.fullName} · ${selected.email}` : selected.email}
        </p>
      )}
      <div className="mt-4 grid gap-3 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-end">
        <TextInput
          label="Buscar usuario"
          value={query}
          placeholder="Nombre o correo@umss.edu.bo"
          error={emailError ?? undefined}
          onChange={(event) => {
            setQuery(event.target.value);
            setEmailError(null);
          }}
        />
        <Button type="button" variant="secondary" onClick={consultTypedEmail}>
          Consultar correo
        </Button>
      </div>
      {isLoading && <p className="mt-3 text-body-md text-gray-500">Cargando usuarios…</p>}
      {errorMessage && <p className="mt-3 text-body-md text-danger">{errorMessage}</p>}
      {!isLoading && (
        <ul className="mt-3 divide-y divide-gray-100 rounded-lg border border-gray-200">
          {matches.length === 0 && (
            <li className="px-4 py-3 text-body-md text-gray-500">Ningún usuario coincide con la búsqueda.</li>
          )}
          {matches.map((user) => (
            <li key={user.userId}>
              <button
                type="button"
                className="flex w-full flex-col px-4 py-3 text-left hover:bg-primary-50"
                onClick={() => onSelect({ id: user.userId, email: user.email, fullName: user.fullName })}
              >
                <span className="text-body-md text-gray-900">{user.fullName}</span>
                <span className="text-label-md text-gray-500">{user.email}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
