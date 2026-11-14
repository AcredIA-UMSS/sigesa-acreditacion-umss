import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { PublicStatusView, type PublicStatusViewProps } from './PublicStatusView';

function renderView(overrides: Partial<PublicStatusViewProps> = {}) {
  const props: PublicStatusViewProps = {
    searchQuery: '',
    onSearchChange: vi.fn(),
    programs: [
      { slug: 'inf-sis', name: 'Ingeniería de Sistemas' },
      { slug: 'med', name: 'Medicina' },
    ],
    selectedSlug: null,
    onSelect: vi.fn(),
    status: null,
    isListLoading: false,
    isStatusLoading: false,
    listError: null,
    statusError: null,
    onDownload: vi.fn(),
    isDownloading: false,
    downloadError: null,
    ...overrides,
  };
  return {
    props,
    ...render(
      <MemoryRouter>
        <PublicStatusView {...props} />
      </MemoryRouter>,
    ),
  };
}

describe('PublicStatusView', () => {
  it('lists only published programs and never shows drafts', () => {
    renderView();

    expect(screen.getByRole('heading', { name: 'Estado de acreditación' })).toBeInTheDocument();
    expect(screen.getByText('Ingeniería de Sistemas')).toBeInTheDocument();
    expect(screen.getByText('Medicina')).toBeInTheDocument();
    expect(screen.queryByText(/borrador/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/observaci/i)).not.toBeInTheDocument();
  });

  it('shows published status and certificate download when available', async () => {
    const user = userEvent.setup();
    const onDownload = vi.fn();
    renderView({
      selectedSlug: 'inf-sis',
      onDownload,
      status: {
        slug: 'inf-sis',
        name: 'Ingeniería de Sistemas',
        accreditationStatus: 'ACREDITADA',
        validFrom: '2025-01-01',
        validUntil: '2029-12-31',
        certificateAvailable: true,
      },
    });

    expect(screen.getByText('Acreditada')).toBeInTheDocument();
    expect(screen.getByText('2025-01-01 — 2029-12-31')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /Descargar certificado/ }));
    expect(onDownload).toHaveBeenCalledTimes(1);
  });

  it('hides download when certificate is not published', () => {
    renderView({
      selectedSlug: 'med',
      status: {
        slug: 'med',
        name: 'Medicina',
        accreditationStatus: 'EN_PROCESO',
        validFrom: '2026-03-01',
        validUntil: '2027-02-28',
        certificateAvailable: false,
      },
    });

    expect(screen.getByText('En proceso')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Descargar certificado/ })).not.toBeInTheDocument();
    expect(screen.getByText('No hay certificado publicado para esta carrera.')).toBeInTheDocument();
  });

  it('shows generic 404 copy without leaking unpublished details', () => {
    renderView({
      selectedSlug: 'ing-civ',
      statusError: 'El recurso solicitado no existe.',
    });

    expect(screen.getByRole('alert')).toHaveTextContent('El recurso solicitado no existe.');
    expect(screen.queryByText('BORRADOR')).not.toBeInTheDocument();
  });
});
