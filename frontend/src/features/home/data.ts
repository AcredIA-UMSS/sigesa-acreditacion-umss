export interface HomeNewsItem {
  id: string;
  category: 'EVENTO' | 'CAPACITACIÓN' | 'LOGRO';
  title: string;
  summary: string;
  featured: boolean;
}

export const HOME_NEWS: HomeNewsItem[] = [
  {
    id: 'medicina-mercosur',
    category: 'EVENTO',
    title: 'Facultad de Medicina renueva Acreditación MERCOSUR',
    summary:
      'Tras un riguroso proceso de evaluación, la carrera de medicina mantiene sus estándares de excelencia regional.',
    featured: true,
  },
  {
    id: 'taller-autoevaluacion',
    category: 'CAPACITACIÓN',
    title: 'Taller de Autoevaluación para nuevas carreras',
    summary: 'Iniciamos la formación para los comités encargados de los nuevos procesos 2026.',
    featured: false,
  },
  {
    id: 'investigacion-indexada',
    category: 'LOGRO',
    title: 'Reconocimiento a la Investigación Indexada',
    summary: 'La DUEA destaca el incremento de publicaciones en revistas de alto impacto.',
    featured: false,
  },
];
