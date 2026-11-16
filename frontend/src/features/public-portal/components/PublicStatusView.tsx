import { Download, GraduationCap, Search } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { PublicProgramStatusResponse, PublicProgramSummaryResponse } from '../../../api/model';
import { Button } from '../../../components/ui/Button';
import { TextInput } from '../../../components/ui/TextInput';

export type PublicStatusViewProps = {
  searchQuery: string;
  onSearchChange: (value: string) => void;
  programs: PublicProgramSummaryResponse[];
  selectedSlug: string | null;
  onSelect: (slug: string) => void;
  status: PublicProgramStatusResponse | null;
  isListLoading: boolean;
  isStatusLoading: boolean;
  listError: string | null;
  statusError: string | null;
  onDownload: () => void;
  isDownloading: boolean;
  downloadError: string | null;
};

function statusLabel(value: string | undefined): string {
  if (value === 'ACREDITADA') {
    return 'Acreditada';
  }
  if (value === 'EN_PROCESO') {
    return 'En proceso';
  }
  return value ?? 'Sin estado';
}

export function PublicStatusView({
  searchQuery,
  onSearchChange,
  programs,
  selectedSlug,
  onSelect,
  status,
  isListLoading,
  isStatusLoading,
  listError,
  statusError,
  onDownload,
  isDownloading,
  downloadError,
}: PublicStatusViewProps) {
  return (
    <main className="bg-gray-50">
      <div className="mx-auto max-w-6xl space-y-6 px-4 py-10 md:px-6">
        <div>
          <p className="text-body-md text-gray-500">
            <Link to="/" className="text-primary-700 hover:text-primary-500">
              Inicio
            </Link>
            <span className="mx-2 text-gray-400">/</span>
            <span>Estado de Acreditación</span>
          </p>
          <h1 className="mt-3 text-heading-xl text-primary-800">Estado de acreditación</h1>
          <p className="mt-2 text-body-md text-gray-700">
            Consulte carreras con información publicada por la DUEA. Solo se muestra lo autorizado.
          </p>
        </div>

        <TextInput
          id="buscar-carrera"
          label="Buscar por nombre de carrera"
          value={searchQuery}
          onChange={(event) => onSearchChange(event.target.value)}
          placeholder="Ej. Ingeniería de Sistemas"
          prefixIcon={<Search size={16} aria-hidden />}
        />

        {listError ? (
          <p role="alert" className="text-body-md text-danger">
            {listError}
          </p>
        ) : null}

        {isListLoading ? (
          <p className="text-body-md text-gray-600">Cargando carreras publicadas…</p>
        ) : programs.length === 0 ? (
          <p className="text-body-md text-gray-600">No hay información pública para esa búsqueda.</p>
        ) : (
          <ul className="grid gap-3 md:grid-cols-2" aria-label="Carreras publicadas">
            {programs.map((program) => {
              const slug = program.slug ?? '';
              const selected = slug === selectedSlug;
              return (
                <li key={slug}>
                  <button
                    type="button"
                    onClick={() => onSelect(slug)}
                    className={`flex w-full items-center gap-3 rounded-xl border p-4 text-left transition-colors ${
                      selected
                        ? 'border-primary-600 bg-primary-50'
                        : 'border-gray-200 bg-body hover:border-primary-300'
                    }`}
                  >
                    <GraduationCap className="text-primary-700" size={20} aria-hidden />
                    <span className="text-body-lg text-gray-900">{program.name}</span>
                  </button>
                </li>
              );
            })}
          </ul>
        )}

        {selectedSlug != null ? (
          <section className="rounded-2xl border border-gray-200 bg-body p-6" aria-live="polite">
            {isStatusLoading ? (
              <p className="text-body-md text-gray-600">Consultando estado publicado…</p>
            ) : statusError ? (
              <p role="alert" className="text-body-md text-danger">
                {statusError}
              </p>
            ) : status == null ? null : (
              <>
                <h2 className="text-heading-md text-primary-800">{status.name}</h2>
                <dl className="mt-4 grid gap-3 text-body-md text-gray-800 sm:grid-cols-2">
                  <div>
                    <dt className="text-label-md text-gray-600">Estado de acreditación</dt>
                    <dd className="mt-1 font-medium">{statusLabel(status.accreditationStatus)}</dd>
                  </div>
                  <div>
                    <dt className="text-label-md text-gray-600">Vigencia</dt>
                    <dd className="mt-1">
                      {status.validFrom ?? '—'} — {status.validUntil ?? '—'}
                    </dd>
                  </div>
                </dl>
                {status.certificateAvailable ? (
                  <div className="mt-6">
                    <Button onClick={onDownload} isLoading={isDownloading}>
                      <Download size={16} aria-hidden />
                      Descargar certificado
                    </Button>
                    {downloadError ? (
                      <p role="alert" className="mt-2 text-body-md text-danger">
                        {downloadError}
                      </p>
                    ) : null}
                  </div>
                ) : (
                  <p className="mt-4 text-body-md text-gray-600">
                    No hay certificado publicado para esta carrera.
                  </p>
                )}
              </>
            )}
          </section>
        ) : null}
      </div>
    </main>
  );
}
