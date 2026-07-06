// frontend/src/features/procesos/components/CreateProcessUI.tsx
import { Info, Users, Calendar, Plus, X, Bell, Settings } from 'lucide-react';

interface CreateProcessUIProps {
  // Aquí vendrían los props tipados (ej: form values, handlers), por ahora lo dejamos estático para la maquetación.
}

export const CreateProcessUI = (_props: CreateProcessUIProps) => {
  return (
    <div className="flex-1 bg-gray-50 flex flex-col h-screen overflow-hidden">
      {/* Topbar */}
      <header className="flex justify-between items-center px-8 py-4 bg-body border-b border-gray-200">
        <div className="text-body-md text-gray-500">
          <span className="text-primary-600">Inicio</span> / Gestión de procesos de acreditación
        </div>
        <div className="flex items-center gap-4 text-gray-600">
          <button className="hover:text-primary-600 relative">
            <Bell size={24} />
            <span className="absolute top-0 right-0 w-2 h-2 bg-secondary rounded-full"></span>
          </button>
          <button className="hover:text-primary-600"><Settings size={24} /></button>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 overflow-y-auto p-8">
        <div className="max-w-6xl mx-auto">
          {/* Header */}
          <div className="mb-8">
            <div className="w-12 h-1 bg-secondary mb-4"></div>
            <h1 className="text-heading-xl text-primary-800 mb-2">Inicializar Nuevo Proceso de Acreditación</h1>
            <p className="text-body-lg text-gray-600">
              Configure los parámetros, asigne responsabilidades clave y defina el cronograma estratégico para el ciclo institucional de aseguramiento de la calidad.
            </p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Columna Izquierda (2/3) */}
            <div className="lg:col-span-2 space-y-6">
              
              {/* Tarjeta 1: Información Básica */}
              <section className="bg-body p-8 rounded-2xl shadow-sm border border-gray-100">
                <div className="flex items-center gap-4 mb-6">
                  <div className="w-12 h-12 bg-gray-50 rounded-full flex items-center justify-center text-primary-600">
                    <Info size={24} />
                  </div>
                  <div>
                    <h2 className="text-heading-md text-primary-800">Información Básica</h2>
                    <p className="text-body-md text-gray-500">Defina el alcance fundamental del proceso</p>
                  </div>
                </div>

                <div className="space-y-4">
                  <div>
                    <label className="block text-label-md text-gray-600 mb-1">FACULTAD</label>
                    <select className="w-full border border-gray-300 rounded-lg p-3 text-body-md text-gray-800 focus:outline-none focus:border-primary-500">
                      <option>Seleccione una opción</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-label-md text-gray-600 mb-1">NOMBRE DE LA CARRERA / PROGRAMA</label>
                    <input type="text" className="w-full border border-gray-300 rounded-lg p-3 text-body-md text-gray-800 focus:outline-none focus:border-primary-500" placeholder="Ej. Ingeniería Civil" />
                  </div>
                </div>
              </section>

              {/* Tarjeta 2: Responsables */}
              <section className="bg-body p-8 rounded-2xl shadow-sm border border-gray-100">
                <div className="flex items-center gap-4 mb-6">
                  <div className="w-12 h-12 bg-primary-50 rounded-full flex items-center justify-center text-primary-600">
                    <Users size={24} />
                  </div>
                  <div>
                    <h2 className="text-heading-md text-primary-800">Responsables Estratégicos</h2>
                    <p className="text-body-md text-gray-500">Asigne coordinadores institucionales y técnicos</p>
                  </div>
                </div>

                <div className="bg-gray-50 p-4 rounded-xl flex items-center gap-4 mb-4 border border-gray-200">
                  <div className="w-10 h-10 border border-gray-300 rounded-full flex items-center justify-center text-gray-400 bg-body">
                    <Users size={20} />
                  </div>
                  <div>
                    <p className="text-label-md text-gray-500">COORDINADOR(ES) DE CARRERA</p>
                    <p className="text-body-md text-gray-800">Sin asignar</p>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-gray-50 p-4 rounded-xl border border-gray-200">
                    <p className="text-label-md text-gray-500 mb-2">ENLACE TÉCNICO</p>
                    <div className="flex items-center gap-2 bg-body p-2 rounded border border-primary-200">
                      <div className="w-8 h-8 bg-secondary-400 text-body rounded-full flex items-center justify-center text-label-md">MA</div>
                      <span className="text-body-md text-gray-800 flex-1">María Antezana</span>
                      <X size={16} className="text-gray-400 cursor-pointer" />
                    </div>
                    <button className="mt-3 text-primary-600 text-label-md flex items-center gap-1 font-semibold">
                      <Plus size={16} /> Asignar
                    </button>
                  </div>
                  <div className="border-2 border-dashed border-gray-300 rounded-xl flex flex-col items-center justify-center p-4 text-gray-500 cursor-pointer hover:bg-gray-50 transition-colors">
                    <Users size={24} className="mb-2" />
                    <span className="text-label-md">ASIGNAR EVALUADOR</span>
                  </div>
                </div>
              </section>
            </div>

            {/* Columna Derecha (1/3) */}
            <div className="space-y-6">
              
              {/* Tarjeta 3: Guía Rápida */}
              <section className="bg-gray-100 p-6 rounded-2xl border border-gray-200">
                <div className="flex items-center gap-2 mb-4 text-primary-800">
                  <Info size={20} />
                  <h3 className="text-heading-sm">Guía Rápida</h3>
                </div>
                <ul className="space-y-4">
                  <li className="flex gap-3">
                    <span className="text-heading-md text-gray-400 font-bold">01</span>
                    <p className="text-body-md text-gray-600">Defina el nombre y la carrera para categorizar correctamente los indicadores.</p>
                  </li>
                  <li className="flex gap-3">
                    <span className="text-heading-md text-gray-400 font-bold">02</span>
                    <p className="text-body-md text-gray-600">Seleccione el modelo de acreditación (Regional o Nacional) según corresponda.</p>
                  </li>
                  <li className="flex gap-3">
                    <span className="text-heading-md text-gray-400 font-bold">03</span>
                    <p className="text-body-md text-gray-600">Una vez creado, podrá invitar a pares evaluadores y comenzar con el proceso de acreditación.</p>
                  </li>
                </ul>
              </section>

              {/* Tarjeta 4: Cronograma */}
              <section className="bg-body p-6 rounded-2xl shadow-sm border border-gray-100">
                <div className="flex items-center gap-4 mb-6">
                  <div className="w-10 h-10 bg-primary-50 rounded-lg flex items-center justify-center text-primary-600">
                    <Calendar size={20} />
                  </div>
                  <div>
                    <h2 className="text-heading-md text-primary-800">Cronograma Maestro</h2>
                    <p className="text-body-md text-gray-500">Establezca fechas clave e hitos</p>
                  </div>
                </div>

                <div className="relative pl-4 border-l-2 border-gray-200 space-y-6 mb-6">
                  <TimelineInput label="INICIO DEL PROCESO" dotColor="bg-primary-600" />
                  <TimelineInput label="LÍMITE DE AUTOEVALUACIÓN" dotColor="bg-warning" />
                  <TimelineInput label="VISITA EXTERNA" dotColor="bg-info" />
                  <TimelineInput label="RESOLUCIÓN ESPERADA" dotColor="bg-secondary" />
                </div>

                <div className="bg-primary-50 p-4 rounded-xl border border-primary-100 flex items-end justify-between">
                  <div>
                    <p className="text-label-md text-primary-700 font-bold mb-1">ESFUERZO ESTIMADO</p>
                    <p className="text-display-lg text-primary-900 leading-none">182 <span className="text-body-lg text-primary-600">Días</span></p>
                  </div>
                  <div className="text-right">
                    <p className="text-label-md text-primary-600">TIPO DE CICLO</p>
                    <p className="text-body-md font-bold text-primary-900">Ciclo Completo Estándar</p>
                  </div>
                </div>
              </section>

            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

const TimelineInput = ({ label, dotColor }: { label: string, dotColor: string }) => (
  <div className="relative">
    <div className={`absolute -left-6 top-2 w-3 h-3 rounded-full ${dotColor} border-2 border-body`}></div>
    <label className="block text-label-md text-gray-500 mb-1">{label}</label>
    <div className="bg-gray-50 border border-gray-200 rounded p-2 text-body-md text-gray-500">
      dd/mm/yyyy
    </div>
  </div>
);