const CATEGORY_LABELS: Record<string, string> = {
  EVENTO: 'Evento',
  CAPACITACION: 'Capacitación',
  LOGRO: 'Logro',
};

const STATUS_LABELS: Record<string, string> = {
  BORRADOR: 'Borrador',
  PUBLICADA: 'Publicada',
};

export const NEWS_CATEGORIES = ['EVENTO', 'CAPACITACION', 'LOGRO'] as const;

export function newsCategoryLabel(category: string | undefined): string {
  if (category == null || category.length === 0) {
    return 'Noticia';
  }
  return CATEGORY_LABELS[category] ?? category;
}

export function newsStatusLabel(status: string | undefined): string {
  if (status == null || status.length === 0) {
    return '—';
  }
  return STATUS_LABELS[status] ?? status;
}

export function formatNewsDate(value: string | undefined): string {
  if (value == null || value.length === 0) {
    return '';
  }
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleDateString('es-BO', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}
