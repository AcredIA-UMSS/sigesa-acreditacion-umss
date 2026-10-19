import { Link } from 'react-router-dom';
import { FilePlus2, FileText, Upload } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';

interface QuickActionItem {
  to: string;
  title: string;
  description: string;
  icon: LucideIcon;
}

const QUICK_ACTIONS: QuickActionItem[] = [
  {
    to: '/procesos/nuevo',
    title: 'Crear proceso',
    description: 'Inicia un nuevo proceso de acreditación.',
    icon: FilePlus2,
  },
  {
    to: '/evidencias/cargar',
    title: 'Subir evidencias',
    description: 'Carga documentos de respaldo para indicadores.',
    icon: Upload,
  },
  {
    to: '/reportes/ejecutivo',
    title: 'Generar reporte',
    description: 'Abre el reporte ejecutivo institucional.',
    icon: FileText,
  },
];

export function QuickActions() {
  return (
    <section aria-labelledby="quick-actions-heading" className="quick-actions">
      <h2 id="quick-actions-heading" className="text-heading-md font-semibold text-primary-800">
        Quick Actions
      </h2>
      <p className="mt-1 text-body-md text-gray-700">Accesos directos a las tareas más frecuentes.</p>

      <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {QUICK_ACTIONS.map((action) => {
          const Icon = action.icon;
          return (
            <Link
              key={action.to}
              to={action.to}
              className="flex items-start gap-3 rounded-2xl border border-primary-200/40 bg-body p-5 shadow-sm transition-shadow hover:shadow-md"
            >
              <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-primary-50 text-primary-600">
                <Icon size={22} aria-hidden />
              </span>
              <span className="min-w-0">
                <span className="block text-heading-sm font-semibold text-primary-800">{action.title}</span>
                <span className="mt-1 block text-body-md text-gray-600">{action.description}</span>
              </span>
            </Link>
          );
        })}
      </div>
    </section>
  );
}
