import image1 from '../../assets/image1.jpg';
import image2 from '../../assets/image2.jpg';
import image3 from '../../assets/image3.jpg';

export const CAMPUS_PHOTOS = [
  { src: image1, alt: 'Facultad de Humanidades y Ciencias de la Educación, UMSS' },
  { src: image2, alt: 'Campus UMSS, Facultad de Ciencias y Tecnología' },
  { src: image3, alt: 'Facultad de Medicina Dr. Aurelio Meleán, UMSS' },
] as const;

export function campusPhotoAt(index: number): (typeof CAMPUS_PHOTOS)[number] {
  const safe = Number.isFinite(index) ? Math.abs(Math.trunc(index)) : 0;
  return CAMPUS_PHOTOS[safe % CAMPUS_PHOTOS.length];
}
