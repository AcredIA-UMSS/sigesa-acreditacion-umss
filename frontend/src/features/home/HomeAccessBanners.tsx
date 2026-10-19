import { Building2, BadgeCheck } from 'lucide-react';
import { Link } from 'react-router-dom';

export function HomeAccessBanners() {
  return (
    <section className="bg-gray-50 px-4 py-12 md:px-6">
      <div className="mx-auto grid max-w-6xl gap-6 md:grid-cols-2">
        {/* TODO: no hay catálogo público de carreras; el banner entra al login del sistema. */}
        <Link
          id="carreras"
          to="/login"
          className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-primary-700 to-primary-900 p-8 text-body shadow-sm focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600"
        >
          <h2 className="text-heading-md text-body">Buscar Carrera Acreditada</h2>
          <p className="mt-2 text-body-md text-primary-100">Consultar listado completo</p>
          <Building2 className="absolute -bottom-4 -right-2 h-28 w-28 text-body/15" aria-hidden />
        </Link>
        {/* TODO: no hay página pública de modalidades; el banner entra al login. */}
        <Link
          id="modalidades"
          to="/login"
          className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-secondary-600 to-secondary-800 p-8 text-body shadow-sm focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-secondary-600"
        >
          <h2 className="text-heading-md text-body">Modalidades de Acreditación</h2>
          <p className="mt-2 text-body-md text-secondary-100">Nacional e Internacional</p>
          <BadgeCheck className="absolute -bottom-4 -right-2 h-28 w-28 text-body/15" aria-hidden />
        </Link>
      </div>
    </section>
  );
}
