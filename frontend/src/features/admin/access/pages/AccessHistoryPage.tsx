import { useCallback, useEffect, useRef, useState } from 'react';
import { Printer } from 'lucide-react';
import { Sidebar } from '../../../../components/layout/Sidebar';
import { Button } from '../../../../components/ui/Button';
import { Select } from '../../../../components/ui/Select';
import { TextInput } from '../../../../components/ui/TextInput';
import { Alert } from '../../../../components/ui/Alert';
import {
  fetchAccessActions,
  fetchAccessSessions,
  fetchAccessSummary,
  type AccessActionItem,
  type AccessSessionItem,
  type AccessSessionSummary,
} from '../api/accessHistoryApi';

const STATUS_OPTIONS = [
  { value: '', label: 'Todos los estados' },
  { value: 'ACTIVE', label: 'Activa' },
  { value: 'CLOSED', label: 'Cerrada' },
  { value: 'EXPIRED', label: 'Expirada' },
  { value: 'FAILED', label: 'Fallida' },
  { value: 'REVOKED', label: 'Revocada' },
];

const ROLE_OPTIONS = [
  { value: '', label: 'Todos los roles' },
  { value: 'JD', label: 'JD' },
  { value: 'TD', label: 'TD' },
  { value: 'CC', label: 'CC' },
  { value: 'EE', label: 'EE' },
  { value: 'CAE_MEMBER', label: 'CAE' },
];

function formatWhen(iso: string | null, part: 'date' | 'time'): string {
  if (!iso) return '—';
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return '—';
  return part === 'date'
    ? new Intl.DateTimeFormat('es-BO', { dateStyle: 'short' }).format(date)
    : new Intl.DateTimeFormat('es-BO', { timeStyle: 'short' }).format(date);
}

function formatDuration(seconds: number | null, status: string): string {
  if (status === 'ACTIVE') return 'En curso';
  if (seconds == null) return '—';
  const minutes = Math.floor(seconds / 60);
  const rest = seconds % 60;
  return `${minutes} min ${rest} s`;
}

function statusLabel(status: string): string {
  return STATUS_OPTIONS.find((option) => option.value === status)?.label ?? status;
}

function actionLabels(actions: string[] | undefined): string[] {
  return actions ?? [];
}

export function AccessHistoryPage() {
  const [summary, setSummary] = useState<AccessSessionSummary | null>(null);
  const [rows, setRows] = useState<AccessSessionItem[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('');
  const [status, setStatus] = useState('');
  const [ipAddress, setIpAddress] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [applied, setApplied] = useState({ email: '', role: '', status: '', ipAddress: '', from: '', to: '' });
  const [selected, setSelected] = useState<AccessSessionItem | null>(null);
  const [actions, setActions] = useState<AccessActionItem[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [printing, setPrinting] = useState(false);
  const [printRows, setPrintRows] = useState<AccessSessionItem[] | null>(null);
  const [printNote, setPrintNote] = useState<string | null>(null);
  const shouldPrint = useRef(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [totals, list] = await Promise.all([
        fetchAccessSummary(),
        fetchAccessSessions({
          email: applied.email || undefined,
          role: applied.role || undefined,
          status: applied.status || undefined,
          ipAddress: applied.ipAddress || undefined,
          from: applied.from ? `${applied.from}T00:00:00` : undefined,
          to: applied.to ? `${applied.to}T23:59:59` : undefined,
          page,
          size: 20,
        }),
      ]);
      setSummary(totals);
      setRows(list.content ?? []);
      setTotalPages(list.totalPages ?? 0);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo cargar el historial de accesos.');
    } finally {
      setLoading(false);
    }
  }, [applied, page]);

  useEffect(() => {
    void load();
  }, [load]);

  useEffect(() => {
    if (!shouldPrint.current || printRows === null) {
      return;
    }
    shouldPrint.current = false;
    const finish = () => setPrinting(false);
    window.addEventListener('afterprint', finish, { once: true });
    window.print();
  }, [printRows]);

  async function printReport() {
    setPrinting(true);
    setError(null);
    try {
      const list = await fetchAccessSessions({
        email: applied.email || undefined,
        role: applied.role || undefined,
        status: applied.status || undefined,
        ipAddress: applied.ipAddress || undefined,
        from: applied.from ? `${applied.from}T00:00:00` : undefined,
        to: applied.to ? `${applied.to}T23:59:59` : undefined,
        page: 0,
        size: 200,
      });
      const content = list.content ?? [];
      setPrintNote(
        list.totalElements > content.length
          ? `Se imprimen ${content.length} de ${list.totalElements} accesos del filtro.`
          : null,
      );
      shouldPrint.current = true;
      setPrintRows(content);
    } catch (err) {
      setPrinting(false);
      setError(err instanceof Error ? err.message : 'No se pudo preparar la impresión.');
    }
  }

  async function openSession(item: AccessSessionItem) {
    setSelected(item);
    setActions([]);
    try {
      setActions(await fetchAccessActions(item.id));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo cargar la actividad de la sesión.');
    }
  }

  return (
    <div className="flex h-screen bg-gray-50" data-testid="access-history-page">
      <div className="print:hidden">
        <Sidebar activeNav="reports" />
      </div>
      <main className="flex-1 overflow-y-auto px-4 py-6 md:px-8">
        <div className="print:hidden">
        <header className="mb-6">
          <h1 className="text-heading-xl font-semibold text-primary-800">Historial de accesos</h1>
          <p className="mt-1 text-body-md text-gray-600">
            Consulta de sesiones y de las acciones registradas en la bitácora. Los registros no se editan ni se eliminan desde esta pantalla.
          </p>
        </header>

        <div className="mb-6 grid grid-cols-2 gap-3 lg:grid-cols-4">
          <SummaryCard label="Total de ingresos" value={summary?.total} />
          <SummaryCard label="Usuarios con sesión activa" value={summary?.activeUsers} />
          <SummaryCard label="Ingresos de hoy" value={summary?.today} />
          <SummaryCard label="Intentos fallidos" value={summary?.failed} />
        </div>

        <form
          className="mb-6 grid gap-4 rounded-2xl border border-gray-200 bg-body p-4 md:grid-cols-3"
          onSubmit={(event) => {
            event.preventDefault();
            setPage(0);
            setApplied({ email, role, status, ipAddress, from, to });
          }}
        >
          <TextInput id="access-email" label="Usuario" value={email} onChange={(event) => setEmail(event.target.value)} />
          <Select id="access-role" label="Rol" options={ROLE_OPTIONS} value={role} onChange={(event) => setRole(event.target.value)} />
          <Select id="access-status" label="Estado de sesión" options={STATUS_OPTIONS} value={status} onChange={(event) => setStatus(event.target.value)} />
          <TextInput id="access-from" label="Fecha desde" type="date" value={from} onChange={(event) => setFrom(event.target.value)} />
          <TextInput id="access-to" label="Fecha hasta" type="date" value={to} onChange={(event) => setTo(event.target.value)} />
          <TextInput id="access-ip" label="Dirección IP" value={ipAddress} onChange={(event) => setIpAddress(event.target.value)} />
          <div className="md:col-span-3">
            <Button type="submit">Aplicar filtros</Button>
          </div>
        </form>

        {error && <div className="mb-4"><Alert variant="error">{error}</Alert></div>}

        <div className="overflow-x-auto rounded-2xl border border-gray-200 bg-body">
          <table className="w-full text-left text-body-md">
            <thead className="text-label-md uppercase text-gray-600">
              <tr>
                <th className="px-4 py-3">Usuario</th>
                <th className="px-4 py-3">Rol</th>
                <th className="px-4 py-3">Fecha de ingreso</th>
                <th className="px-4 py-3">Hora de ingreso</th>
                <th className="px-4 py-3">Hora de salida</th>
                <th className="px-4 py-3">Duración</th>
                <th className="px-4 py-3">Dirección IP</th>
                <th className="px-4 py-3">Estado</th>
                <th className="px-4 py-3">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {loading && (
                <tr><td className="px-4 py-6 text-gray-600" colSpan={9}>Cargando accesos…</td></tr>
              )}
              {!loading && rows.length === 0 && (
                <tr><td className="px-4 py-6 text-gray-600" colSpan={9}>No hay accesos con esos filtros.</td></tr>
              )}
              {rows.map((item) => (
                <tr
                  key={item.id}
                  tabIndex={0}
                  className="cursor-pointer border-t border-gray-200 hover:bg-primary-50"
                  onClick={() => void openSession(item)}
                  onKeyDown={(event) => {
                    if (event.key === 'Enter') void openSession(item);
                  }}
                >
                  <td className="px-4 py-3">
                    <p className="font-medium text-gray-900">{item.fullName || item.email}</p>
                    <p className="text-label-md text-gray-600">{item.email}</p>
                  </td>
                  <td className="px-4 py-3">{item.role ?? '—'}</td>
                  <td className="px-4 py-3">{formatWhen(item.startedAt, 'date')}</td>
                  <td className="px-4 py-3">{formatWhen(item.startedAt, 'time')}</td>
                  <td className="px-4 py-3">{formatWhen(item.endedAt, 'time')}</td>
                  <td className="px-4 py-3">{formatDuration(item.durationSeconds, item.status)}</td>
                  <td className="px-4 py-3">{item.ipAddress ?? '—'}</td>
                  <td className="px-4 py-3">{statusLabel(item.status)}</td>
                  <td className="px-4 py-3"><ActionList actions={item.actions} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="mt-4 flex flex-wrap items-center gap-3">
          <Button type="button" variant="ghost" disabled={page <= 0} onClick={() => setPage((current) => current - 1)}>Anterior</Button>
          <p className="text-body-md text-gray-600">Página {page + 1} de {Math.max(totalPages, 1)}</p>
          <Button type="button" variant="ghost" disabled={page + 1 >= totalPages} onClick={() => setPage((current) => current + 1)}>Siguiente</Button>
          <Button type="button" onClick={() => void printReport()} disabled={printing || loading} isLoading={printing}>
            <Printer size={16} />
            Imprimir reporte
          </Button>
        </div>

        {selected && (
          <section className="mt-6 rounded-2xl border border-gray-200 bg-body p-4">
            <h2 className="text-heading-sm font-semibold text-primary-800">Actividad de la sesión</h2>
            <p className="mt-1 text-body-md text-gray-600">
              {selected.fullName || selected.email} · {statusLabel(selected.status)}
              {selected.userAgent ? ` · ${selected.userAgent}` : ''}
            </p>
            <ul className="mt-4 space-y-3">
              {actions.length === 0 && <li className="text-body-md text-gray-600">Esta sesión no tiene acciones asociadas.</li>}
              {actions.map((action) => (
                <li key={action.id} className="rounded-lg bg-gray-50 p-3">
                  <p className="text-label-md text-gray-600">{formatWhen(action.createdAt, 'date')} {formatWhen(action.createdAt, 'time')} · {action.module} · {action.result}</p>
                  <p className="text-body-md text-gray-900">{action.action}</p>
                  {action.description && <p className="text-body-md text-gray-700">{action.description}</p>}
                </li>
              ))}
            </ul>
          </section>
        )}
        </div>

        <section className="hidden print:block" aria-hidden="true">
          <h1 className="text-heading-lg font-semibold text-primary-800">Historial de accesos</h1>
          <p className="mt-1 text-body-md text-gray-700">
            UMSS DUEA · {new Intl.DateTimeFormat('es-BO', { dateStyle: 'long', timeStyle: 'short' }).format(new Date())}
          </p>
          {printNote && <p className="mt-2 text-body-md text-gray-700">{printNote}</p>}
          <table className="mt-4 w-full border-collapse text-left text-body-md">
            <thead>
              <tr className="border-b border-gray-300">
                <th className="py-2 pr-3">Usuario</th>
                <th className="py-2 pr-3">Rol</th>
                <th className="py-2 pr-3">Ingreso</th>
                <th className="py-2 pr-3">Salida</th>
                <th className="py-2 pr-3">IP</th>
                <th className="py-2 pr-3">Estado</th>
                <th className="py-2">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {(printRows ?? rows).length === 0 && (
                <tr><td className="py-3" colSpan={7}>No hay accesos con esos filtros.</td></tr>
              )}
              {(printRows ?? rows).map((item) => (
                <tr key={item.id} className="border-b border-gray-200 align-top">
                  <td className="py-2 pr-3">{item.fullName || item.email}<br />{item.email}</td>
                  <td className="py-2 pr-3">{item.role ?? '—'}</td>
                  <td className="py-2 pr-3">{formatWhen(item.startedAt, 'date')} {formatWhen(item.startedAt, 'time')}</td>
                  <td className="py-2 pr-3">{formatWhen(item.endedAt, 'time')}</td>
                  <td className="py-2 pr-3">{item.ipAddress ?? '—'}</td>
                  <td className="py-2 pr-3">{statusLabel(item.status)}</td>
                  <td className="py-2">{actionLabels(item.actions).join(', ') || 'Sin cambios'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </main>
    </div>
  );
}

function ActionList({ actions }: { actions: string[] | undefined }) {
  const labels = actionLabels(actions);
  if (labels.length === 0) {
    return <span className="text-body-md text-gray-600">Sin cambios</span>;
  }
  return (
    <ul className="flex flex-wrap gap-1">
      {labels.map((label) => (
        <li key={label} className="rounded-md bg-primary-50 px-2 py-1 text-label-md text-primary-800">
          {label}
        </li>
      ))}
    </ul>
  );
}

function SummaryCard({ label, value }: { label: string; value: number | undefined }) {
  return (
    <article className="rounded-xl border border-gray-200 bg-body px-4 py-3">
      <p className="text-label-md text-gray-600">{label}</p>
      <p className="mt-1 text-heading-md font-semibold text-primary-800">{value ?? '—'}</p>
    </article>
  );
}
