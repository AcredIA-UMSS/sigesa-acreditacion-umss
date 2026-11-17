import { Globe, ShieldCheck, TrendingUp } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';

interface Pillar {
  title: string;
  body: string;
  Icon: LucideIcon;
  iconWrap: string;
  iconColor: string;
}

const PILLARS: Pillar[] = [
  {
    title: 'Garantía de Calidad',
    body: 'Supervisamos el cumplimiento de estándares internacionales en todas nuestras facultades y carreras académicas.',
    Icon: ShieldCheck,
    iconWrap: 'bg-primary-50',
    iconColor: 'text-primary-700',
  },
  {
    title: 'Mejora Continua',
    body: 'Implementamos procesos de autoevaluación constantes para optimizar el rendimiento docente e institucional.',
    Icon: TrendingUp,
    iconWrap: 'bg-secondary-50',
    iconColor: 'text-secondary-600',
  },
  {
    title: 'Visión Global',
    body: 'Buscamos el reconocimiento internacional que permita a nuestros graduados destacar en cualquier entorno mundial.',
    Icon: Globe,
    iconWrap: 'home-gold-soft',
    iconColor: 'home-gold-text',
  },
];

export function HomeDueaSection() {
  return (
    <section id="que-es-la-duea" className="bg-body px-4 py-16 md:px-6">
      <div className="mx-auto max-w-6xl">
        <h2 className="text-center text-heading-lg text-primary-800">¿Qué es la DUEA?</h2>
        <p className="mx-auto mt-4 max-w-3xl text-center text-body-md text-gray-700">
          La Dirección Universitaria de Evaluación y Acreditación (DUEA) es la unidad de la
          Universidad Mayor de San Simón responsable de gestionar los procesos de evaluación y
          acreditación académica, promoviendo la mejora continua y el cumplimiento de estándares de
          calidad.
        </p>
        <ul className="mt-10 grid gap-6 md:grid-cols-3">
          {PILLARS.map((pillar) => (
            <li
              key={pillar.title}
              className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm"
            >
              <div className={`mb-4 inline-flex rounded-xl p-3 ${pillar.iconWrap}`}>
                <pillar.Icon className={pillar.iconColor} size={28} aria-hidden />
              </div>
              <h3 className="text-heading-sm text-primary-800">{pillar.title}</h3>
              <p className="mt-2 text-body-md text-gray-600">{pillar.body}</p>
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
