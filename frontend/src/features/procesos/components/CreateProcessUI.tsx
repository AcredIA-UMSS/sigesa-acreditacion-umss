// frontend/src/features/procesos/components/CreateProcessUI.tsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Info, Users, Calendar, Plus, X, Bell, Settings, CheckCircle, AlertTriangle, ArrowLeft } from 'lucide-react';
import { Button } from '../../../components/ui/Button';
import { Alert } from '../../../components/ui/Alert';

// Mock Data
const MOCK_FACULTADES = [
  { id: 'fac-tech', name: 'Facultad de Ciencias y Tecnología' },
  { id: 'fac-med', name: 'Facultad de Medicina' },
  { id: 'fac-econ', name: 'Facultad de Ciencias Económicas' },
  { id: 'fac-hum', name: 'Facultad de Humanidades y Ciencias de la Educación' },
];

const MOCK_CARRERAS: Record<string, { id: string; name: string }[]> = {
  'fac-tech': [
    { id: 'car-sistemas', name: 'Ingeniería de Sistemas' },
    { id: 'car-civil', name: 'Ingeniería Civil' },
    { id: 'car-industrial', name: 'Ingeniería Industrial' },
  ],
  'fac-med': [
    { id: 'car-medicina', name: 'Medicina' },
    { id: 'car-enfermeria', name: 'Enfermería' },
  ],
  'fac-econ': [
    { id: 'car-admin', name: 'Administración de Empresas' },
    { id: 'car-conta', name: 'Contaduría Pública' },
  ],
  'fac-hum': [
    { id: 'car-edu', name: 'Ciencias de la Educación' },
  ],
};

const MOCK_TEMPLATES = [
  { id: 'temp-ceub-2026', name: 'Modelo Nacional CEUB 2026 (Acreditación Completa)', type: 'CEUB' },
  { id: 'temp-arcu-2025', name: 'Modelo Regional ARCU-SUR 2025 (Mercosur)', type: 'ARCU_SUR' },
];

const MOCK_PERIODOS = ['Gestión 2026', 'Gestión 2027', '2026-I', '2026-II'];

const MOCK_DISPONIBLE_COORDINADORES = [
  'Dr. Hugo Valenzuela',
  'Ing. Claudia Arce',
  'MSc. Ramiro Mendoza',
  'Dra. Sofía Rocha',
];

const MOCK_DISPONIBLE_EVALUADORES = [
  'Ing. Carlos Mendoza (Par Externo)',
  'Dr. Luis Peralta (Evaluador Nacional)',
  'Mg. Ana María Rojas (Par Internacional)',
];

// Existing active processes to check FSD-BR-08 (Only one active process per career/type/period)
const EXISTING_ACTIVE_PROCESSES = [
  { careerId: 'car-sistemas', type: 'CEUB', period: 'Gestión 2026' }
];

export const CreateProcessUI = () => {
  const navigate = useNavigate();

  // Form State
  const [selectedFacultad, setSelectedFacultad] = useState('');
  const [selectedCarreraId, setSelectedCarreraId] = useState('');
  const [selectedTemplateId, setSelectedTemplateId] = useState('');
  const [selectedPeriodo, setSelectedPeriodo] = useState('');
  
  // Responsables State
  const [coordinadores, setCoordinadores] = useState<string[]>([]);
  const [nuevoCoordinador, setNuevoCoordinador] = useState('');
  const [enlaceTecnico, setEnlaceTecnico] = useState('María Antezana');
  const [evaluador, setEvaluador] = useState<string | null>(null);
  
  // Show UI for assigning
  const [showAddCoordinador, setShowAddCoordinador] = useState(false);
  const [showAddEvaluador, setShowAddEvaluador] = useState(false);

  // Chronogram Dates (defaults)
  const todayStr = new Date().toISOString().split('T')[0];
  const [fechaInicio, setFechaInicio] = useState(todayStr);
  const [fechaAutoevaluacion, setFechaAutoevaluacion] = useState('');
  const [fechaVisita, setFechaVisita] = useState('');
  const [fechaResolucion, setFechaResolucion] = useState('');
  
  // Effort and validation states
  const [esfuerzoEstimado, setEsfuerzoEstimado] = useState<number | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  // Reset Carrera if Facultad changes
  useEffect(() => {
    setSelectedCarreraId('');
  }, [selectedFacultad]);

  // Calculate effort and set default sequence of dates if fechaInicio is set
  useEffect(() => {
    if (fechaInicio) {
      const inicio = new Date(fechaInicio);
      
      // Auto-set sequence of dates if not set yet
      if (!fechaAutoevaluacion) {
        const auto = new Date(inicio);
        auto.setDate(auto.getDate() + 90);
        setFechaAutoevaluacion(auto.toISOString().split('T')[0]);
      }
      if (!fechaVisita) {
        const visita = new Date(inicio);
        visita.setDate(visita.getDate() + 150);
        setFechaVisita(visita.toISOString().split('T')[0]);
      }
      if (!fechaResolucion) {
        const resol = new Date(inicio);
        resol.setDate(resol.getDate() + 182);
        setFechaResolucion(resol.toISOString().split('T')[0]);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fechaInicio]);

  // Calculate estimated effort
  useEffect(() => {
    if (fechaInicio && fechaResolucion) {
      const start = new Date(fechaInicio);
      const end = new Date(fechaResolucion);
      const diffTime = end.getTime() - start.getTime();
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      setEsfuerzoEstimado(diffDays > 0 ? diffDays : 0);
    } else {
      setEsfuerzoEstimado(null);
    }
  }, [fechaInicio, fechaResolucion]);

  // Form Validation and FSD-BR-08 checks
  const handleCreateProcess = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');

    if (!selectedFacultad || !selectedCarreraId || !selectedTemplateId || !selectedPeriodo) {
      setErrorMsg('Por favor complete todos los campos de información básica obligatorios.');
      return;
    }

    // Get template details
    const selectedTemplate = MOCK_TEMPLATES.find(t => t.id === selectedTemplateId);
    if (!selectedTemplate) return;

    // FSD-BR-08 rule validation: one active process per career/type/period
    const isDuplicate = EXISTING_ACTIVE_PROCESSES.some(
      proc => proc.careerId === selectedCarreraId && 
              proc.type === selectedTemplate.type && 
              proc.period === selectedPeriodo
    );

    if (isDuplicate) {
      const carreraName = MOCK_CARRERAS[selectedFacultad]?.find(c => c.id === selectedCarreraId)?.name;
      setErrorMsg(`[Regla FSD-BR-08] Ya existe un proceso de acreditación activo para "${carreraName}" bajo el modelo "${selectedTemplate.type}" para el periodo "${selectedPeriodo}".`);
      return;
    }

    // Dates validation
    if (new Date(fechaInicio) >= new Date(fechaAutoevaluacion)) {
      setErrorMsg('La fecha de inicio debe ser anterior a la fecha límite de autoevaluación.');
      return;
    }
    if (new Date(fechaAutoevaluacion) >= new Date(fechaVisita)) {
      setErrorMsg('La fecha límite de autoevaluación debe ser anterior a la visita externa.');
      return;
    }
    if (new Date(fechaVisita) >= new Date(fechaResolucion)) {
      setErrorMsg('La visita externa debe ser anterior a la resolución esperada.');
      return;
    }

    // Simulate submission
    setIsSubmitting(true);
    setTimeout(() => {
      setIsSubmitting(false);
      setSuccessMsg('¡Proceso de acreditación inicializado exitosamente!');
      
      // Simulate adding to existing processes
      EXISTING_ACTIVE_PROCESSES.push({
        careerId: selectedCarreraId,
        type: selectedTemplate.type,
        period: selectedPeriodo
      });

      // Redirect after 2 seconds
      setTimeout(() => {
        navigate('/dashboard');
      }, 2000);
    }, 1500);
  };

  const handleAddCoordinador = () => {
    if (nuevoCoordinador && !coordinadores.includes(nuevoCoordinador)) {
      setCoordinadores([...coordinadores, nuevoCoordinador]);
      setNuevoCoordinador('');
      setShowAddCoordinador(false);
    }
  };

  const handleRemoveCoordinador = (name: string) => {
    setCoordinadores(coordinadores.filter(c => c !== name));
  };

  return (
    <div className="flex-1 bg-gray-50 flex flex-col h-screen overflow-hidden">
      {/* Topbar */}
      <header className="flex justify-between items-center px-8 py-4 bg-body border-b border-gray-200">
        <div className="flex items-center gap-3 text-body-md text-gray-500">
          <button 
            onClick={() => navigate('/dashboard')} 
            className="flex items-center gap-1 text-primary-600 hover:text-primary-800 transition-colors"
          >
            <ArrowLeft size={16} /> Dashboard
          </button>
          <span>/</span>
          <span>Gestión de procesos de acreditación</span>
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

          {/* Form */}
          <form onSubmit={handleCreateProcess}>
            {/* Messages */}
            {errorMsg && (
              <div className="mb-6">
                <Alert variant="error" title="Error de Validación">
                  <div className="flex items-center gap-2">
                    <AlertTriangle size={18} />
                    <span>{errorMsg}</span>
                  </div>
                </Alert>
              </div>
            )}

            {successMsg && (
              <div className="mb-6">
                <Alert variant="success" title="Acción Exitosa">
                  <div className="flex items-center gap-2">
                    <CheckCircle size={18} />
                    <span>{successMsg} Redirigiendo al dashboard...</span>
                  </div>
                </Alert>
              </div>
            )}

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
                      <label className="block text-label-md text-gray-600 mb-1 font-bold">FACULTAD</label>
                      <select 
                        value={selectedFacultad}
                        onChange={(e) => setSelectedFacultad(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg p-3 text-body-md text-gray-800 focus:outline-none focus:border-primary-500 bg-body"
                      >
                        <option value="">Seleccione una facultad</option>
                        {MOCK_FACULTADES.map(fac => (
                          <option key={fac.id} value={fac.id}>{fac.name}</option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block text-label-md text-gray-600 mb-1 font-bold">CARRERA / PROGRAMA</label>
                      <select 
                        value={selectedCarreraId}
                        onChange={(e) => setSelectedCarreraId(e.target.value)}
                        disabled={!selectedFacultad}
                        className="w-full border border-gray-300 rounded-lg p-3 text-body-md text-gray-800 focus:outline-none focus:border-primary-500 bg-body disabled:bg-gray-100"
                      >
                        <option value="">Seleccione una carrera</option>
                        {selectedFacultad && MOCK_CARRERAS[selectedFacultad]?.map(car => (
                          <option key={car.id} value={car.id}>{car.name}</option>
                        ))}
                      </select>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-label-md text-gray-600 mb-1 font-bold">MODELO DE ACREDITACIÓN</label>
                        <select 
                          value={selectedTemplateId}
                          onChange={(e) => setSelectedTemplateId(e.target.value)}
                          className="w-full border border-gray-300 rounded-lg p-3 text-body-md text-gray-800 focus:outline-none focus:border-primary-500 bg-body"
                        >
                          <option value="">Seleccione el modelo/plantilla</option>
                          {MOCK_TEMPLATES.map(temp => (
                            <option key={temp.id} value={temp.id}>{temp.name}</option>
                          ))}
                        </select>
                      </div>

                      <div>
                        <label className="block text-label-md text-gray-600 mb-1 font-bold">PERIODO ACADÉMICO / GESTIÓN</label>
                        <select 
                          value={selectedPeriodo}
                          onChange={(e) => setSelectedPeriodo(e.target.value)}
                          className="w-full border border-gray-300 rounded-lg p-3 text-body-md text-gray-800 focus:outline-none focus:border-primary-500 bg-body"
                        >
                          <option value="">Seleccione la gestión</option>
                          {MOCK_PERIODOS.map(p => (
                            <option key={p} value={p}>{p}</option>
                          ))}
                        </select>
                      </div>
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

                  {/* Coordinadores */}
                  <div className="bg-gray-50 p-4 rounded-xl mb-4 border border-gray-200">
                    <div className="flex justify-between items-center mb-2">
                      <p className="text-label-md text-gray-500 font-bold uppercase">Coordinadores de Carrera</p>
                      <button 
                        type="button" 
                        onClick={() => setShowAddCoordinador(!showAddCoordinador)}
                        className="text-primary-600 text-label-md hover:underline flex items-center gap-1 font-bold"
                      >
                        <Plus size={14} /> Agregar
                      </button>
                    </div>

                    {showAddCoordinador && (
                      <div className="flex gap-2 mb-3">
                        <select
                          value={nuevoCoordinador}
                          onChange={(e) => setNuevoCoordinador(e.target.value)}
                          className="flex-1 border border-gray-300 rounded p-2 text-body-md bg-body"
                        >
                          <option value="">Seleccionar Coordinador...</option>
                          {MOCK_DISPONIBLE_COORDINADORES.map(coord => (
                            <option key={coord} value={coord}>{coord}</option>
                          ))}
                        </select>
                        <button
                          type="button"
                          onClick={handleAddCoordinador}
                          className="bg-primary-600 text-body px-3 rounded text-label-md font-semibold hover:bg-primary-500"
                        >
                          Asignar
                        </button>
                      </div>
                    )}

                    {coordinadores.length === 0 ? (
                      <p className="text-body-md text-gray-500 italic">Sin asignar coordinadores aún</p>
                    ) : (
                      <div className="flex flex-wrap gap-2">
                        {coordinadores.map(c => (
                          <span key={c} className="flex items-center gap-2 bg-primary-50 text-primary-800 border border-primary-100 rounded px-3 py-1 text-body-md">
                            {c}
                            <X size={14} className="cursor-pointer hover:text-secondary" onClick={() => handleRemoveCoordinador(c)} />
                          </span>
                        ))}
                      </div>
                    )}
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {/* Enlace Técnico */}
                    <div className="bg-gray-50 p-4 rounded-xl border border-gray-200">
                      <p className="text-label-md text-gray-500 mb-2 font-bold">ENLACE TÉCNICO</p>
                      <div className="flex items-center gap-2 bg-body p-2 rounded border border-primary-200">
                        <div className="w-8 h-8 bg-secondary-400 text-body rounded-full flex items-center justify-center text-label-md">MA</div>
                        <span className="text-body-md text-gray-800 flex-1">{enlaceTecnico}</span>
                        <X size={16} className="text-gray-400 cursor-pointer hover:text-secondary" onClick={() => setEnlaceTecnico('Sin asignar')} />
                      </div>
                      {enlaceTecnico === 'Sin asignar' && (
                        <button 
                          type="button"
                          onClick={() => setEnlaceTecnico('María Antezana')}
                          className="mt-3 text-primary-600 text-label-md flex items-center gap-1 font-semibold hover:underline"
                        >
                          <Plus size={16} /> Re-asignar
                        </button>
                      )}
                    </div>

                    {/* Evaluador Externo */}
                    <div className="bg-gray-50 p-4 rounded-xl border border-gray-200 flex flex-col justify-between">
                      <div>
                        <p className="text-label-md text-gray-500 mb-2 font-bold">EVALUADOR ASIGNADO</p>
                        {evaluador ? (
                          <div className="flex items-center gap-2 bg-body p-2 rounded border border-primary-200">
                            <div className="w-8 h-8 bg-primary-400 text-body rounded-full flex items-center justify-center text-label-md">EV</div>
                            <span className="text-body-md text-gray-800 flex-1 truncate">{evaluador}</span>
                            <X size={16} className="text-gray-400 cursor-pointer hover:text-secondary" onClick={() => setEvaluador(null)} />
                          </div>
                        ) : (
                          <div 
                            onClick={() => setShowAddEvaluador(true)}
                            className="border-2 border-dashed border-gray-300 rounded-xl flex flex-col items-center justify-center p-3 text-gray-500 cursor-pointer hover:bg-gray-100 transition-colors"
                          >
                            <Users size={20} className="mb-1" />
                            <span className="text-label-md font-bold">ASIGNAR EVALUADOR</span>
                          </div>
                        )}
                      </div>

                      {showAddEvaluador && !evaluador && (
                        <div className="mt-2 flex gap-2">
                          <select
                            onChange={(e) => {
                              if (e.target.value) {
                                setEvaluador(e.target.value);
                                setShowAddEvaluador(false);
                              }
                            }}
                            className="w-full border border-gray-300 rounded p-1 text-label-md bg-body"
                          >
                            <option value="">Seleccione...</option>
                            {MOCK_DISPONIBLE_EVALUADORES.map(ev => (
                              <option key={ev} value={ev}>{ev}</option>
                            ))}
                          </select>
                          <button type="button" onClick={() => setShowAddEvaluador(false)} className="text-gray-400 hover:text-gray-600"><X size={16} /></button>
                        </div>
                      )}
                    </div>
                  </div>
                </section>

                {/* Botones de acción del Formulario */}
                <div className="flex justify-end gap-4 bg-body p-6 rounded-2xl border border-gray-100 shadow-sm">
                  <Button 
                    variant="ghost" 
                    onClick={() => navigate('/dashboard')}
                    disabled={isSubmitting}
                  >
                    Cancelar
                  </Button>
                  <Button 
                    type="submit" 
                    variant="primary" 
                    isLoading={isSubmitting}
                  >
                    Inicializar Proceso
                  </Button>
                </div>
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
                      <p className="text-body-md text-gray-600">Defina la facultad, carrera y gestión para catalogar correctamente el proceso.</p>
                    </li>
                    <li className="flex gap-3">
                      <span className="text-heading-md text-gray-400 font-bold">02</span>
                      <p className="text-body-md text-gray-600">Seleccione la plantilla normativa aplicable (CEUB o ARCU-SUR).</p>
                    </li>
                    <li className="flex gap-3">
                      <span className="text-heading-md text-gray-400 font-bold">03</span>
                      <p className="text-body-md text-gray-600">Asegúrese de respetar la regla FSD-BR-08: no se permiten procesos duplicados simultáneamente activos.</p>
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
                    <div className="relative">
                      <div className="absolute -left-6 top-2 w-3 h-3 rounded-full bg-primary-600 border-2 border-body"></div>
                      <label className="block text-label-md text-gray-500 mb-1 font-bold">INICIO DEL PROCESO</label>
                      <input 
                        type="date"
                        value={fechaInicio}
                        onChange={(e) => setFechaInicio(e.target.value)}
                        className="w-full bg-gray-50 border border-gray-200 rounded p-2 text-body-md text-gray-800 focus:outline-none focus:border-primary-500"
                      />
                    </div>

                    <div className="relative">
                      <div className="absolute -left-6 top-2 w-3 h-3 rounded-full bg-warning border-2 border-body"></div>
                      <label className="block text-label-md text-gray-500 mb-1 font-bold">LÍMITE DE AUTOEVALUACIÓN</label>
                      <input 
                        type="date"
                        value={fechaAutoevaluacion}
                        onChange={(e) => setFechaAutoevaluacion(e.target.value)}
                        className="w-full bg-gray-50 border border-gray-200 rounded p-2 text-body-md text-gray-800 focus:outline-none focus:border-primary-500"
                      />
                    </div>

                    <div className="relative">
                      <div className="absolute -left-6 top-2 w-3 h-3 rounded-full bg-info border-2 border-body"></div>
                      <label className="block text-label-md text-gray-500 mb-1 font-bold">VISITA EXTERNA</label>
                      <input 
                        type="date"
                        value={fechaVisita}
                        onChange={(e) => setFechaVisita(e.target.value)}
                        className="w-full bg-gray-50 border border-gray-200 rounded p-2 text-body-md text-gray-800 focus:outline-none focus:border-primary-500"
                      />
                    </div>

                    <div className="relative">
                      <div className="absolute -left-6 top-2 w-3 h-3 rounded-full bg-secondary border-2 border-body"></div>
                      <label className="block text-label-md text-gray-500 mb-1 font-bold">RESOLUCIÓN ESPERADA</label>
                      <input 
                        type="date"
                        value={fechaResolucion}
                        onChange={(e) => setFechaResolucion(e.target.value)}
                        className="w-full bg-gray-50 border border-gray-200 rounded p-2 text-body-md text-gray-800 focus:outline-none focus:border-primary-500"
                      />
                    </div>
                  </div>

                  <div className="bg-primary-50 p-4 rounded-xl border border-primary-100 flex items-end justify-between">
                    <div>
                      <p className="text-label-md text-primary-700 font-bold mb-1">ESFUERZO ESTIMADO</p>
                      <p className="text-display-lg text-primary-900 leading-none">
                        {esfuerzoEstimado !== null ? esfuerzoEstimado : '--'} <span className="text-body-lg text-primary-600">Días</span>
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-label-md text-primary-600">TIPO DE CICLO</p>
                      <p className="text-body-md font-bold text-primary-900">Ciclo Completo Estándar</p>
                    </div>
                  </div>
                </section>

              </div>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
};