import { ArrowRight, ClipboardCheck, FileSearch, Medal } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';

interface ProcessStep {
  n: string;
  title: string;
  body: string;
  Icon: LucideIcon;
  badge: string;
  iconWrap: string;
  iconColor: string;
}

const STEPS: ProcessStep[] = [
  {
    n: '1',
    title: 'Autoevaluación',
    body: 'Análisis interno exhaustivo de fortalezas y debilidades del programa académico.',
    Icon: FileSearch,
    badge: 'bg-primary-700',
    iconWrap: 'bg-primary-50',
    iconColor: 'text-primary-700',
  },
  {
    n: '2',
    title: 'Evaluación de Pares',
    body: 'Visita y validación externa por expertos nacionales e internacionales certificados.',
    Icon: ClipboardCheck,
    badge: 'bg-secondary-600',
    iconWrap: 'bg-secondary-50',
    iconColor: 'text-secondary-600',
  },
  {
    n: '3',
    title: 'Certificación',
    body: 'Otorgamiento oficial del sello de calidad y reconocimiento de excelencia académica.',
    Icon: Medal,
    badge: 'home-gold-fill',
    iconWrap: 'home-gold-soft',
    iconColor: 'home-gold-text',
  },
];

export function HomeProcessSection() {
  return (
    <section className="bg-body px-4 py-16 md:px-6">
      <div className="mx-auto max-w-6xl">
        <h2 className="text-center text-heading-lg text-primary-800">Proceso de Acreditación</h2>
        <p className="mx-auto mt-3 max-w-2xl text-center text-body-md text-gray-600">
          Un camino riguroso hacia la excelencia académica dividido en fases estratégicas bien
          definidas.
        </p>
        <ol className="mt-10 flex flex-col items-stretch gap-4 md:flex-row md:items-center">
          {STEPS.map((step, index) => (
            <li key={step.n} className="flex flex-1 items-center gap-4">
              <article className="w-full rounded-2xl border border-gray-200 bg-body p-6 text-center shadow-sm">
                <span
                  className={`mx-auto mb-3 flex h-7 w-7 items-center justify-center rounded-full text-label-md font-medium text-body ${step.badge}`}
                >
                  {step.n}
                </span>
                <div className={`mx-auto mb-3 inline-flex rounded-xl p-3 ${step.iconWrap}`}>
                  <step.Icon className={step.iconColor} size={26} aria-hidden />
                </div>
                <h3 className="text-heading-sm text-primary-800">{step.title}</h3>
                <p className="mt-2 text-body-md text-gray-600">{step.body}</p>
              </article>
              {index < STEPS.length - 1 ? (
                <ArrowRight className="hidden shrink-0 text-gray-400 md:block" aria-hidden />
              ) : null}
            </li>
          ))}
        </ol>
      </div>
    </section>
  );
}
