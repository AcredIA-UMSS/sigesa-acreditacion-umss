import { useEffect, useMemo, useState } from 'react';
import { Search, X } from 'lucide-react';
import { loadSession } from '../../../lib/auth/tokenStorage';
import type { ExecutiveSection } from '../types';

type SemaphoreProgram = NonNullable<ExecutiveSection['semaforoProgramas']>[number];
type SemaphoreTone = SemaphoreProgram['estado'];
type ToneFilter = 'TODOS' | SemaphoreTone;

interface SemaphoreObservation {
  observationId?: string;
  indicatorCode?: string;
  indicatorTitle?: string;
  description?: string;
  dueDate?: string;
  status?: string;
}

const FACULTY_LABELS: Record<string, string> = {
  FCT: 'Facultad de Ciencias y Tecnología',
  FAU: 'Facultad de Arquitectura',
  FCMM: 'Facultad de Medicina',
  CCJJ: 'Facultad de Ciencias Jurídicas',
  FACSO: 'Facultad de Ciencias Sociales',
  FCyt: 'Facultad de Ciencias y Tecnología (FCyt)',
  FAFCYT: 'Facultad de Agronomía',
};

const FILTERS: { id: ToneFilter; label: string }[] = [
  { id: 'TODOS', label: 'Todas' },
  { id: 'ROJO', label: 'Crítico' },
  { id: 'AMARILLO', label: 'En alerta' },
  { id: 'VERDE', label: 'Saludable' },
  { id: 'SIN_DATO', label: 'Sin dato' },
];

function facultyLabel(code?: string) {
  if (!code?.trim()) return 'Sin facultad';
  return FACULTY_LABELS[code] ?? code;
}

function toneLabel(estado: SemaphoreTone) {
  switch (estado) {
    case 'VERDE':
      return 'Saludable';
    case 'AMARILLO':
      return 'En alerta';
    case 'ROJO':
      return 'Crítico';
    default:
      return 'Sin dato';
  }
}

function toneClasses(estado: SemaphoreTone) {
  switch (estado) {
    case 'VERDE':
      return { pill: 'bg-success/10 text-success', bar: 'bg-success', dot: 'bg-success' };
    case 'AMARILLO':
      return { pill: 'bg-warning/15 text-gray-900', bar: 'bg-warning', dot: 'bg-warning' };
    case 'ROJO':
      return { pill: 'bg-secondary/10 text-secondary', bar: 'bg-secondary', dot: 'bg-secondary' };
    default:
      return { pill: 'bg-gray-100 text-gray-700', bar: 'bg-gray-400', dot: 'bg-gray-400' };
  }
}

async function fetchProgramObservations(programId: string): Promise<SemaphoreObservation[]> {
  const session = loadSession();
  const headers = new Headers();
  if (session?.accessToken) {
    headers.set('Authorization', `Bearer ${session.accessToken}`);
  }
  const response = await fetch(
    `/api/v1/dashboards/executive/observations?programId=${encodeURIComponent(programId)}`,
    { headers },
  );
  if (!response.ok) {
    throw new Error('No se pudieron cargar las observaciones.');
  }
  return (await response.json()) as SemaphoreObservation[];
}

function isOverdue(dueDate?: string) {
  if (!dueDate) return false;
  const due = new Date(`${dueDate}T00:00:00`);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return due < today;
}

export function SemaphoreBoard({ section }: { section: ExecutiveSection }) {
  const programs = section.semaforoProgramas ?? [];
  const [query, setQuery] = useState('');
  const [tone, setTone] = useState<ToneFilter>('TODOS');
  const [selected, setSelected] = useState<SemaphoreProgram | null>(null);
  const [observations, setObservations] = useState<SemaphoreObservation[]>([]);
  const [observationsError, setObservationsError] = useState<string | null>(null);
  const [loadingObservations, setLoadingObservations] = useState(false);

  useEffect(() => {
    if (!selected) return;
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setSelected(null);
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [selected]);

  const visible = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return programs.filter((program) => {
      if (tone !== 'TODOS' && program.estado !== tone) return false;
      if (!needle) return true;
      const faculty = facultyLabel(program.facultad).toLowerCase();
      return program.nombre.toLowerCase().includes(needle) || faculty.includes(needle);
    });
  }, [programs, query, tone]);

  async function openObservations(program: SemaphoreProgram) {
    setSelected(program);
    setObservations([]);
    setObservationsError(null);
    setLoadingObservations(true);
    try {
      setObservations(await fetchProgramObservations(program.programaId));
    } catch (err) {
      setObservationsError(err instanceof Error ? err.message : 'No se pudieron cargar las observaciones.');
    } finally {
      setLoadingObservations(false);
    }
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <Stat label="En acreditación" value={String(section.totalProgramasEnAcreditacion ?? 0)} />
        <Stat label="Avance institucional" value={`${(section.porcentajeAvanceInstitucional ?? 0).toFixed(0)}%`} />
        <Stat label="Observaciones abiertas" value={String(section.criticalObservations ?? 0)} />
        <Stat label="En alerta o críticas" value={String(section.alertPrograms ?? 0)} />
      </div>

      <section className="rounded-2xl border border-gray-200 bg-body shadow-sm">
        <div className="flex flex-col gap-4 border-b border-gray-200 p-4 lg:flex-row lg:items-center lg:justify-between">
          <label className="relative block w-full lg:max-w-md">
            <span className="sr-only">Buscar carrera o facultad</span>
            <Search className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={18} />
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Buscar carrera o facultad"
              className="w-full rounded-lg border border-gray-300 bg-body py-2.5 pl-10 pr-3 text-body-md text-primary-900 outline-none focus:border-primary"
            />
          </label>
          <div className="flex flex-wrap gap-2">
            {FILTERS.map((filter) => (
              <button
                key={filter.id}
                type="button"
                onClick={() => setTone(filter.id)}
                className={`rounded-lg px-3 py-2 text-label-md font-medium ${
                  tone === filter.id
                    ? 'bg-primary text-body'
                    : 'bg-gray-100 text-gray-800 hover:bg-gray-200'
                }`}
              >
                {filter.label}
              </button>
            ))}
          </div>
        </div>

        {visible.length === 0 ? (
          <p className="px-4 py-10 text-center text-body-md text-gray-600">
            Ninguna carrera coincide con la búsqueda.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] table-fixed border-collapse text-left">
              <thead>
                <tr className="border-b border-gray-200 bg-gray-50 text-label-md font-semibold uppercase text-gray-600">
                  <th className="w-[26%] px-4 py-3">Facultad</th>
                  <th className="w-[24%] px-4 py-3">Carrera</th>
                  <th className="w-[18%] px-4 py-3">Avance</th>
                  <th className="w-[16%] px-4 py-3">Observaciones</th>
                  <th className="w-[16%] px-4 py-3">Estado</th>
                </tr>
              </thead>
              <tbody>
                {visible.map((program) => {
                  const toneStyle = toneClasses(program.estado);
                  const progress = program.avance;
                  const selectedRow = selected?.programaId === program.programaId;
                  return (
                    <tr
                      key={program.programaId}
                      className={`border-b border-gray-100 ${selectedRow ? 'bg-primary-50' : 'hover:bg-gray-50'}`}
                    >
                      <td className="px-4 py-3 align-middle text-body-md text-gray-700">
                        {facultyLabel(program.facultad)}
                      </td>
                      <td className="px-4 py-3 align-middle text-body-md font-semibold text-primary-900">
                        {program.nombre}
                      </td>
                      <td className="px-4 py-3 align-middle">
                        {progress == null ? (
                          <span className="text-body-md text-gray-500">—</span>
                        ) : (
                          <div className="flex items-center gap-3">
                            <div className="h-2 w-full max-w-24 overflow-hidden rounded-full bg-gray-200">
                              <div
                                className={`h-full rounded-full ${toneStyle.bar}`}
                                style={{ width: `${Math.max(0, Math.min(100, progress))}%` }}
                              />
                            </div>
                            <span className="w-10 shrink-0 text-body-md text-gray-800">{progress.toFixed(0)}%</span>
                          </div>
                        )}
                      </td>
                      <td className="px-4 py-3 align-middle">
                        <button
                          type="button"
                          onClick={() => openObservations(program)}
                          className="text-body-md font-semibold text-primary underline-offset-2 hover:underline"
                        >
                          Ver
                        </button>
                      </td>
                      <td className="px-4 py-3 align-middle">
                        <span className={`inline-flex items-center gap-2 rounded-full px-2.5 py-1 text-label-md font-semibold ${toneStyle.pill}`}>
                          <span className={`h-2.5 w-2.5 rounded-full ${toneStyle.dot}`} />
                          {toneLabel(program.estado)}
                        </span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {selected && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-primary-900/40 p-4"
          onClick={() => setSelected(null)}
        >
          <div
            role="dialog"
            aria-modal="true"
            aria-labelledby="semaphore-observations-title"
            className="max-h-[70vh] w-full max-w-lg overflow-y-auto rounded-2xl border border-gray-200 bg-body p-5 shadow-md"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="mb-4 flex items-start justify-between gap-4">
              <div>
                <h2 id="semaphore-observations-title" className="text-heading-sm font-semibold text-primary-900">
                  {selected.nombre}
                </h2>
                <p className="mt-1 text-body-md text-gray-600">{facultyLabel(selected.facultad)}</p>
              </div>
              <button
                type="button"
                onClick={() => setSelected(null)}
                className="inline-flex items-center gap-1 rounded-lg px-2 py-1 text-label-md text-gray-700 hover:bg-gray-100"
              >
                <X size={16} />
                Cerrar
              </button>
            </div>

            {loadingObservations && <p className="text-body-md text-gray-600">Cargando observaciones…</p>}
            {observationsError && <p className="text-body-md text-secondary">{observationsError}</p>}
            {!loadingObservations && !observationsError && observations.length === 0 && (
              <p className="text-body-md text-gray-600">Esta carrera no tiene observaciones registradas.</p>
            )}
            {!loadingObservations && observations.length > 0 && (
              <ul className="divide-y divide-gray-100">
                {observations.map((item) => {
                  const overdue = isOverdue(item.dueDate);
                  return (
                    <li key={item.observationId} className="py-3">
                      <div className="flex flex-wrap items-baseline justify-between gap-2">
                        <p className="text-body-md font-semibold text-primary-900">
                          {item.indicatorCode} · {item.indicatorTitle}
                        </p>
                        <p className={`text-label-md font-semibold ${overdue ? 'text-secondary' : 'text-gray-600'}`}>
                          {overdue ? 'Vencida' : 'En plazo'} · {item.dueDate}
                        </p>
                      </div>
                      <p className="mt-1 text-body-md text-gray-700">{item.description}</p>
                    </li>
                  );
                })}
              </ul>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl border border-gray-200 bg-body px-4 py-4 shadow-sm">
      <p className="text-label-md font-medium uppercase text-gray-600">{label}</p>
      <p className="mt-1 text-heading-lg font-semibold text-primary-900">{value}</p>
    </div>
  );
}
