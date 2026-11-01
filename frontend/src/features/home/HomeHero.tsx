import { Building2, Users } from 'lucide-react';

interface HomeHeroProps {
  onDemo: () => void;
}

export function HomeHero({ onDemo }: HomeHeroProps) {
  const scrollToDuea = () => {
    document.getElementById('que-es-la-duea')?.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <section
      id="inicio"
      className="bg-gradient-to-br from-secondary-50 via-primary-50 to-body"
    >
      <div className="mx-auto grid max-w-6xl items-center gap-10 px-4 py-12 md:px-6 md:py-16 lg:grid-cols-2">
        <div>
          <p className="inline-flex rounded-full bg-secondary-50 px-3 py-1 text-label-md font-medium uppercase tracking-wide text-secondary-700">
            DUEA - Universidad Mayor de San Simón
          </p>
          <h1 className="mt-4 text-display-lg leading-tight text-primary-800">
            Excelencia que
            <span className="mt-1 block text-secondary-600">Impacta</span>
          </h1>
          <p className="mt-4 max-w-xl text-body-lg text-gray-700">
            La UMSS impulsa la excelencia académica a través de procesos de evaluación y
            acreditación que garantizan el cumplimiento de estándares de calidad a nivel nacional e
            internacional.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <button
              type="button"
              onClick={onDemo}
              className="rounded-lg bg-primary-700 px-5 py-3 text-label-md font-medium text-body hover:bg-primary-600 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600"
            >
              Ver demo
            </button>
            <button
              type="button"
              onClick={scrollToDuea}
              className="rounded-lg border border-primary-700 bg-body px-5 py-3 text-label-md font-medium text-primary-800 hover:bg-primary-50 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600"
            >
              Saber más
            </button>
          </div>
        </div>

        <div className="relative mx-auto h-[22rem] w-full max-w-md lg:h-[26rem]">
          <div className="home-hero-photo home-hero-photo--campus absolute right-0 top-0 h-48 w-56 rounded-2xl shadow-lg md:h-56 md:w-64">
            <Building2 className="absolute bottom-4 right-4 h-16 w-16 text-body/40" aria-hidden />
          </div>
          <div className="home-hero-photo home-hero-photo--group absolute bottom-6 left-0 h-40 w-52 rounded-2xl border-4 border-body shadow-lg md:h-48 md:w-60">
            <Users className="absolute bottom-3 right-3 h-14 w-14 text-body/50" aria-hidden />
          </div>
          <div className="absolute bottom-16 right-6 rounded-xl bg-body px-4 py-3 shadow-lg">
            <p className="text-heading-lg font-semibold text-secondary-600">+25</p>
            <p className="text-label-md font-medium uppercase tracking-wide text-primary-700">
              Carreras acreditadas
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
