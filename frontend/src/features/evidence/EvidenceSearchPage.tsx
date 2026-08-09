import { useState } from 'react';
import { Sidebar } from '../../components/layout/Sidebar';
import { useSearch } from '../../api/endpoints/evidence-search/evidence-search';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { TextInput } from '../../components/ui/TextInput';
import { 
  Search, 
  Brain, 
  Database, 
  AlertCircle, 
  FileText, 
  Calendar, 
  CheckCircle,
  Terminal, 
  Sparkles, 
  ShieldCheck,
  Cpu,
  MessageSquare,
  ChevronRight
} from 'lucide-react';

export const EvidenceSearchPage = () => {
  const [queryInput, setQueryInput] = useState('');
  const [activeQuery, setActiveQuery] = useState('');
  const [xAiEnabled, setXAiEnabled] = useState(true);
  const [debugTab, setDebugTab] = useState<'flow' | 'llm' | 'json'>('flow');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  // Llamada al hook autogenerado de Orval
  const { data, isLoading, error } = useSearch(
    { query: activeQuery },
    {
      query: {
        enabled: true, // Siempre activo para que traiga todo en la carga inicial cuando activeQuery es ""
      },
      request: {
        headers: {
          'X-AI-Enabled': String(xAiEnabled),
        },
      },
    }
  );

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setActiveQuery(queryInput);
    setCurrentPage(1); // Resetear a la primera página en nueva búsqueda
  };

  const executePreset = (queryText: string, aiToggle: boolean) => {
    setQueryInput(queryText);
    setXAiEnabled(aiToggle);
    setActiveQuery(queryText);
    setCurrentPage(1); // Resetear a la primera página en preset
  };

  const results = data?.data?.results || [];
  const routingPath = data?.data?.routingPath;
  const toolUsed = data?.data?.toolUsed;
  const dataSource = data?.data?.dataSource;
  const message = data?.data?.message;

  // Paginación del lado del cliente
  const totalPages = Math.ceil(results.length / itemsPerPage);
  const paginatedResults = results.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage);

  // Determinar el escenario técnico
  const getDemoScenarioInfo = () => {
    if (!activeQuery) return null;
    const isKeyword = ['infraestructura', 'docentes', 'plan de estudios', 'administracion', 'administración'].includes(activeQuery.trim().toLowerCase());
    
    if (isKeyword) {
      return {
        number: 1,
        title: "Escenario 1: Controlado (Exact Keyword Match)",
        description: "Se resuelve de manera directa por catálogo en backend sin invocar al LLM.",
        color: "border-success-500/20 bg-success/5 text-success",
        badge: "KEYWORD MATCH"
      };
    }
    
    if (!xAiEnabled) {
      return {
        number: 4,
        title: "Escenario 4: Modelo Apagado (IA_HABILITADA = false)",
        description: "Al estar la IA desactivada por cabecera, la consulta por sinónimos falla controlado y retorna un REFUSAL seguro.",
        color: "border-danger/30 bg-danger/5 text-danger",
        badge: "IA DESACTIVADA"
      };
    }

    if (routingPath === 'REFUSAL') {
      return {
        number: 3,
        title: "Escenario 3: Fuera de Alcance (Out of Scope)",
        description: "El LLM detecta que la consulta no pertenece a acreditación universitaria y la rechaza de forma segura.",
        color: "border-warning/30 bg-warning/5 text-warning-700",
        badge: "REFUSAL OUT-OF-SCOPE"
      };
    }

    return {
      number: 2,
      title: "Escenario 2: Sinónimo (Semantic LLM Match)",
      description: "El LLM traduce la consulta coloquial al término oficial y el backend ejecuta el query tradicional.",
      color: "border-secondary-500/20 bg-secondary-50/50 text-secondary-600",
      badge: "LLM RESOLVED"
    };
  };

  const scenarioInfo = getDemoScenarioInfo();

  // Simular la traza de razonamiento del LLM basada en la query
  const getLLMThoughtProcess = () => {
    if (!activeQuery) return null;
    const cleanQuery = activeQuery.trim().toLowerCase();

    if (scenarioInfo?.number === 1) {
      return {
        systemPrompt: "Eres un asistente de búsqueda e inteligente para SIGESA...",
        thought: "No se requiere interacción con el LLM. La palabra clave coincide exactamente con el catálogo estático del backend.",
        toolCall: null,
        outOfScope: false
      };
    }

    if (scenarioInfo?.number === 4) {
      return {
        systemPrompt: "Asistente de IA inactivo (Header X-AI-Enabled es false).",
        thought: "Llamada al LLM cancelada por el middleware de seguridad del backend. Fallback inmediato a bloqueo seguro.",
        toolCall: null,
        outOfScope: false
      };
    }

    if (scenarioInfo?.number === 3) {
      return {
        systemPrompt: "Eres un asistente de búsqueda y enrutamiento inteligente para el sistema de acreditación universitaria SIGESA. Tu tarea es enrutar las consultas del usuario utilizando las herramientas provistas...\nSi la consulta no está relacionada con la acreditación universitaria, responde con la palabra 'OUT_OF_SCOPE'.",
        thought: `Analizando consulta: "${activeQuery}". La solicitud no contiene términos relativos a infraestructura, docentes, plan de estudios, administración, acreditación o evidencias de carrera. Excede el dominio académico universitario.`,
        toolCall: null,
        outOfScope: true
      };
    }

    // Escenario 2: Sinonimos
    let mappedDimension = "Infraestructura";
    let extractedTerm = cleanQuery;
    let chainOfThought = "";

    if (cleanQuery.includes("aula") || cleanQuery.includes("laboratorio") || cleanQuery.includes("edificio") || cleanQuery.includes("computación")) {
      mappedDimension = "Infraestructura";
      extractedTerm = "aulas";
      chainOfThought = `El usuario busca "${activeQuery}". "aulas" y "laboratorio" son sinónimos semánticos de la dimensión física de la universidad. Mapeando a dimensión: "Infraestructura". Término normalizado para base de datos: "aulas".`;
    } else if (cleanQuery.includes("profesor") || cleanQuery.includes("docente") || cleanQuery.includes("catedrático")) {
      mappedDimension = "Docentes";
      extractedTerm = "docente";
      chainOfThought = `El usuario busca "${activeQuery}". El término "profesor" hace referencia al plantel académico. Mapeando a dimensión: "Docentes". Término normalizado para base de datos: "docente".`;
    } else if (cleanQuery.includes("materia") || cleanQuery.includes("curriculo") || cleanQuery.includes("programa") || cleanQuery.includes("clase")) {
      mappedDimension = "Plan de Estudios";
      extractedTerm = "plan de estudios";
      chainOfThought = `El usuario busca "${activeQuery}". "currículo" o "materia" se refiere a la organización curricular. Mapeando a dimensión: "Plan de Estudios". Término normalizado para base de datos: "plan de estudios".`;
    } else {
      mappedDimension = "Administracion";
      extractedTerm = cleanQuery;
      chainOfThought = `La consulta "${activeQuery}" hace referencia al soporte administrativo o finanzas. Mapeando a la dimensión: "Administracion". Extraigo término base: "${cleanQuery}".`;
    }

    return {
      systemPrompt: "Eres un asistente de búsqueda y enrutamiento inteligente para el sistema de acreditación universitaria SIGESA. Tu tarea es enrutar las consultas del usuario utilizando las herramientas provistas...",
      thought: chainOfThought,
      toolCall: {
        name: "buscar_evidencias_por_parametros",
        arguments: {
          dimension: mappedDimension,
          termino: extractedTerm
        }
      },
      outOfScope: false
    };
  };

  const llmTrace = getLLMThoughtProcess();

  return (
    <div className="flex h-screen bg-gray-50 text-primary-900">
      <Sidebar activeNav="processes" />
      <main className="flex-1 overflow-y-auto p-8 bg-gray-50/50">
        <div className="max-w-7xl mx-auto space-y-8">
          
          {/* Cabecera */}
          <div>
            <h1 className="text-heading-xl font-bold tracking-tight text-primary-800 flex items-center gap-3">
              <Search className="text-secondary" size={32} />
              Buscar Evidencia Inteligente
            </h1>
            <p className="mt-1 text-body-lg text-gray-700">
              Localiza documentos de acreditación mediante palabras clave exactas o sinónimos en lenguaje natural asistido por IA.
            </p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
            
            {/* Panel Principal Izquierdo: Buscador y Resultados (7/12) */}
            <div className="lg:col-span-7 space-y-6">
              
              {/* Formulario de búsqueda */}
              <form onSubmit={handleSearch} className="bg-white p-6 rounded-2xl border border-primary-100 shadow-sm space-y-4">
                <div className="flex gap-4">
                  <div className="flex-1">
                    <TextInput
                      label="Consulta de búsqueda"
                      placeholder="Ej. 'aulas de clase', 'infraestructura', 'papeles de computación'..."
                      value={queryInput}
                      onChange={(e) => setQueryInput(e.target.value)}
                      className="w-full"
                    />
                  </div>
                  <Button type="submit" variant="secondary" className="flex items-center gap-2 self-end h-[46px] bg-secondary hover:bg-secondary-600 text-white font-semibold">
                    <Search size={18} />
                    Buscar
                  </Button>
                </div>

                {/* Toggle de IA */}
                <div className="flex items-center justify-between bg-primary-50/30 p-3.5 rounded-xl border border-primary-100/50">
                  <div className="flex items-center gap-3">
                    <input
                      id="ai-toggle"
                      type="checkbox"
                      checked={xAiEnabled}
                      onChange={(e) => setXAiEnabled(e.target.checked)}
                      className="w-4 h-4 text-secondary bg-white border-primary-200 rounded focus:ring-secondary cursor-pointer"
                    />
                    <label htmlFor="ai-toggle" className="flex items-center gap-2 text-label-md cursor-pointer select-none text-primary-800 font-medium">
                      <Brain size={16} className={xAiEnabled ? "text-secondary animate-pulse" : "text-gray-400"} />
                      <span>Habilitar Asistente de IA para Sinónimos (Cabecera <code className="text-secondary font-mono bg-secondary-50/50 px-1 py-0.5 rounded text-[11px]">X-AI-Enabled</code>)</span>
                    </label>
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${xAiEnabled ? "bg-secondary-100 text-secondary" : "bg-gray-200 text-gray-500"}`}>
                    {xAiEnabled ? "IA HABILITADA" : "IA DESACTIVADA"}
                  </span>
                </div>
              </form>

              {/* Estado de carga y errores */}
              {isLoading && (
                <div className="flex flex-col items-center justify-center p-12 bg-white rounded-2xl border border-primary-100 shadow-sm">
                  <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-secondary mb-3"></div>
                  <p className="text-body-sm text-gray-500">Procesando consulta en el backend híbrido...</p>
                </div>
              )}

              {!!error && (
                <Alert variant="error" title="Error de Conexión">
                  No se pudo conectar al servidor de SIGESA. Verifique que el servicio backend esté activo.
                </Alert>
              )}

              {/* Resultados de Búsqueda (Se muestran si hay datos cargados, incluso si la query es vacía) */}
              {!isLoading && !error && data && (
                <div className="space-y-6">
                  {/* Mensaje de Rechazo (Escenario 3 & 4) */}
                  {routingPath === 'REFUSAL' ? (
                    <div className="p-6 bg-white border-l-4 border-warning rounded-2xl shadow-sm space-y-2 border border-primary-100">
                      <div className="flex items-center gap-2 text-warning-700 font-bold">
                        <AlertCircle size={20} />
                        Búsqueda no procesada
                      </div>
                      <p className="text-body-md text-gray-700 font-semibold">{message}</p>
                      <p className="text-[12px] text-gray-500">
                        El enrutador híbrido bloqueó el query por seguridad y optimización de base de datos.
                      </p>
                    </div>
                  ) : (
                    /* Tabla de Evidencias */
                    <div className="bg-white rounded-2xl border border-primary-100 overflow-hidden shadow-sm">
                      <div className="p-5 border-b border-primary-100 bg-primary-50/10 flex justify-between items-center">
                        <h2 className="text-heading-sm font-bold text-primary-850">Evidencias Localizadas ({results.length})</h2>
                      </div>
                      {results.length === 0 ? (
                        <div className="p-12 text-center text-gray-400 bg-white">
                          <AlertCircle className="mx-auto text-gray-300 mb-3" size={48} />
                          No se encontraron evidencias registradas que correspondan a su búsqueda o carrera.
                        </div>
                      ) : (
                        <>
                          <div className="overflow-x-auto">
                            <table className="w-full text-left border-collapse">
                              <thead>
                                <tr className="bg-primary-50/40 text-label-md text-primary-800 border-b border-primary-100">
                                  <th className="p-4 font-semibold">Archivo</th>
                                  <th className="p-4 font-semibold">Descripción</th>
                                  <th className="p-4 font-semibold">Dimensión Académica</th>
                                  <th className="p-4 font-semibold">Criterio</th>
                                  <th className="p-4 font-semibold">Carrera</th>
                                  <th className="p-4 font-semibold">Cargado</th>
                                </tr>
                              </thead>
                              <tbody className="divide-y divide-primary-100 text-body-sm text-gray-700">
                                {paginatedResults.map((evidence) => (
                                  <tr key={evidence.evidenceId} className="hover:bg-primary-50/10 transition-colors">
                                    <td className="p-4 font-semibold text-primary-900 flex items-center gap-2 max-w-xs truncate">
                                      <FileText size={16} className="text-secondary min-w-[16px]" />
                                      <span title={evidence.title}>{evidence.title}</span>
                                    </td>
                                    <td className="p-4 max-w-xs truncate" title={evidence.description}>
                                      {evidence.description}
                                    </td>
                                    <td className="p-4 font-semibold text-primary-700">
                                      {evidence.dimensionName}
                                    </td>
                                    <td className="p-4">
                                      <span className="px-2 py-0.5 rounded bg-primary-50 border border-primary-100 text-label-sm font-mono text-primary-800">
                                        {evidence.criterionCode}
                                      </span>
                                    </td>
                                    <td className="p-4 text-gray-600">
                                      {evidence.carreraName}
                                    </td>
                                    <td className="p-4 text-gray-500 whitespace-nowrap">
                                      <span className="flex items-center gap-1">
                                        <Calendar size={14} />
                                        {evidence.uploadedAt ? new Date(evidence.uploadedAt).toLocaleDateString() : 'N/A'}
                                      </span>
                                    </td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>

                          {/* Controles de Paginación */}
                          {totalPages > 1 && (
                            <div className="p-4 border-t border-primary-100 bg-primary-50/5 flex items-center justify-between">
                              <span className="text-[12px] text-gray-600">
                                Mostrando página <strong className="font-semibold text-primary-900">{currentPage}</strong> de <strong className="font-semibold text-primary-900">{totalPages}</strong> ({results.length} resultados)
                              </span>
                              <div className="flex gap-2">
                                <Button 
                                  onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                                  disabled={currentPage === 1}
                                  variant="secondary"
                                  className="px-3 py-1.5 text-label-sm font-semibold"
                                >
                                  Anterior
                                </Button>
                                <Button 
                                  onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                                  disabled={currentPage === totalPages}
                                  variant="secondary"
                                  className="px-3 py-1.5 text-label-sm font-semibold"
                                >
                                  Siguiente
                                </Button>
                              </div>
                            </div>
                          )}
                        </>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* Panel de Depuración e Inspección Derecha (5/12) */}
            <div className="lg:col-span-5 space-y-6">
              
              {/* Presets Rápidos para Demo */}
              <div className="bg-white p-5 rounded-2xl border border-primary-100 shadow-sm">
                <h3 className="text-label-lg font-bold text-primary-850 flex items-center gap-2 mb-3">
                  <Sparkles size={16} className="text-warning" />
                  Casos de Prueba (Demo Presets)
                </h3>
                <p className="text-[12px] text-gray-600 mb-4">
                  Interactúa con los 4 escenarios de enrutamiento híbrido del FSD-UC-007.
                </p>
                
                <div className="space-y-2">
                  <button 
                    onClick={() => executePreset('infraestructura', true)}
                    className="w-full flex items-center justify-between p-2.5 rounded-xl bg-gray-50 hover:bg-primary-50/50 border border-primary-100 text-left text-body-sm transition-all"
                  >
                    <div>
                      <span className="block font-semibold text-success">Escenario 1: "infraestructura"</span>
                      <span className="text-[11px] text-gray-500">Coincidencia directa por catálogo estático (No usa IA)</span>
                    </div>
                    <ChevronRight size={16} className="text-gray-400" />
                  </button>

                  <button 
                    onClick={() => executePreset('aulas de computación', true)}
                    className="w-full flex items-center justify-between p-2.5 rounded-xl bg-gray-50 hover:bg-primary-50/50 border border-primary-100 text-left text-body-sm transition-all"
                  >
                    <div>
                      <span className="block font-semibold text-secondary">Escenario 2: "aulas de computación"</span>
                      <span className="text-[11px] text-gray-500">Mapea sinónimo con IA (Llama al LLM)</span>
                    </div>
                    <ChevronRight size={16} className="text-gray-400" />
                  </button>

                  <button 
                    onClick={() => executePreset('¿cómo hacer una pizza?', true)}
                    className="w-full flex items-center justify-between p-2.5 rounded-xl bg-gray-50 hover:bg-primary-50/50 border border-primary-100 text-left text-body-sm transition-all"
                  >
                    <div>
                      <span className="block font-semibold text-warning-700">Escenario 3: "¿cómo hacer una pizza?"</span>
                      <span className="text-[11px] text-gray-500">Búsqueda fuera de alcance (Rechazo seguro por IA)</span>
                    </div>
                    <ChevronRight size={16} className="text-gray-400" />
                  </button>

                  <button 
                    onClick={() => executePreset('aulas de computación', false)}
                    className="w-full flex items-center justify-between p-2.5 rounded-xl bg-gray-50 hover:bg-primary-50/50 border border-primary-100 text-left text-body-sm transition-all"
                  >
                    <div>
                      <span className="block font-semibold text-danger">Escenario 4: "aulas" (IA desactivada)</span>
                      <span className="text-[11px] text-gray-500">Cabecera X-AI-Enabled: false (Bloqueo preventivo)</span>
                    </div>
                    <ChevronRight size={16} className="text-gray-400" />
                  </button>
                </div>
              </div>

              {/* Depurador Técnico */}
              <div className="bg-white p-5 rounded-2xl border border-primary-100 shadow-sm space-y-4">
                
                {/* Cabecera del Depurador */}
                <div className="flex flex-col gap-3 border-b border-primary-100 pb-3">
                  <div className="flex justify-between items-center">
                    <h3 className="text-label-lg font-bold text-primary-850 flex items-center gap-2">
                      <Terminal size={16} className="text-secondary" />
                      Consola de Depuración
                    </h3>
                  </div>

                  {/* Selector de pestañas del depurador */}
                  <div className="flex bg-gray-100 p-1 rounded-lg text-[12px] font-semibold text-gray-600">
                    <button 
                      onClick={() => setDebugTab('flow')} 
                      className={`flex-1 py-1.5 rounded-md transition-all ${debugTab === 'flow' ? 'bg-white text-primary-900 shadow-sm' : 'hover:text-primary-800'}`}
                    >
                      Flujo Híbrido
                    </button>
                    <button 
                      onClick={() => setDebugTab('llm')} 
                      className={`flex-1 py-1.5 rounded-md transition-all ${debugTab === 'llm' ? 'bg-white text-primary-900 shadow-sm' : 'hover:text-primary-800'}`}
                    >
                      Ejecución LLM {xAiEnabled && activeQuery && <span className="h-2 w-2 rounded-full bg-secondary inline-block ml-1 animate-ping" />}
                    </button>
                    <button 
                      onClick={() => setDebugTab('json')} 
                      className={`flex-1 py-1.5 rounded-md transition-all ${debugTab === 'json' ? 'bg-white text-primary-900 shadow-sm' : 'hover:text-primary-800'}`}
                    >
                      JSON Crudo
                    </button>
                  </div>
                </div>

                {!activeQuery ? (
                  <div className="p-8 text-center text-gray-400 text-body-sm">
                    Realiza una búsqueda para visualizar el razonamiento y comportamiento interno de SIGESA en tiempo real.
                  </div>
                ) : (
                  <div className="space-y-4">
                    {/* Tarjeta de Resumen Escenario */}
                    {scenarioInfo && (
                      <div className={`p-3.5 rounded-xl border ${scenarioInfo.color} space-y-1`}>
                        <div className="flex justify-between items-center">
                          <span className="font-bold text-body-sm">{scenarioInfo.title}</span>
                          <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-white/70 border border-primary-200">{scenarioInfo.badge}</span>
                        </div>
                        <p className="text-[11px] opacity-90">{scenarioInfo.description}</p>
                      </div>
                    )}

                    {/* Contenido de la pestaña: Flujo Híbrido */}
                    {debugTab === 'flow' && (
                      <div className="space-y-3 font-sans text-body-sm">
                        <div className="space-y-3 border-l-2 border-primary-200 pl-4 ml-2 relative">
                          
                          {/* Paso 1 */}
                          <div className="relative">
                            <span className="absolute -left-[23px] top-0.5 bg-white rounded-full p-0.5 border border-primary-400">
                              <Cpu size={10} className="text-primary-700" />
                            </span>
                            <span className="text-[10px] text-primary-800 block font-extrabold tracking-wider">PASO 1: INTERCEPTOR HTTP</span>
                            <p className="text-gray-800 font-medium mt-0.5">
                              Header enviado: <code className="text-secondary font-mono text-[11px] bg-secondary-50 px-1.5 py-0.5 rounded font-bold">X-AI-Enabled: {String(xAiEnabled)}</code>
                            </p>
                          </div>

                          {/* Paso 2 */}
                          <div className="relative">
                            <span className={`absolute -left-[23px] top-0.5 rounded-full p-0.5 border ${
                              scenarioInfo?.number === 1 ? "bg-success border-success text-white" : "bg-white border-primary-400 text-primary-750"
                            }`}>
                              {scenarioInfo?.number === 1 ? <CheckCircle size={10} /> : <Cpu size={10} />}
                            </span>
                            <span className="text-[10px] text-primary-800 block font-extrabold tracking-wider">PASO 2: CATÁLOGO DETERMINISTA</span>
                            <p className="text-gray-800 mt-0.5">
                              {scenarioInfo?.number === 1 ? (
                                <span className="text-success font-bold">Match de palabra clave en catálogo. Salta el LLM.</span>
                              ) : (
                                <span className="text-gray-700">Sin coincidencia exacta en catálogo tradicional del backend.</span>
                              )}
                            </p>
                          </div>

                          {/* Paso 3 */}
                          <div className="relative">
                            <span className={`absolute -left-[23px] top-0.5 rounded-full p-0.5 border ${
                              scenarioInfo?.number === 2 ? "bg-secondary border-secondary text-white" : 
                              scenarioInfo?.number === 3 ? "bg-warning border-warning text-black" :
                              scenarioInfo?.number === 4 ? "bg-danger border-danger text-white" :
                              "bg-white border-primary-400 text-primary-750"
                            }`}>
                              {scenarioInfo?.number === 2 && <Brain size={10} />}
                              {scenarioInfo?.number === 3 && <AlertCircle size={10} />}
                              {scenarioInfo?.number === 4 && <AlertCircle size={10} />}
                              {scenarioInfo?.number === 1 && <Cpu size={10} />}
                            </span>
                            <span className="text-[10px] text-primary-800 block font-extrabold tracking-wider">PASO 3: ENRUTAMIENTO HÍBRIDO</span>
                            <div className="mt-0.5">
                              {scenarioInfo?.number === 1 && (
                                <p className="text-gray-500 italic">Paso omitido para ahorrar tokens del LLM.</p>
                              )}
                              {scenarioInfo?.number === 4 && (
                                <p className="text-danger font-bold">IA desactivada. Búsqueda por comodines ILIKE activa.</p>
                              )}
                              {scenarioInfo?.number === 2 && (
                                <p className="text-primary-900 font-medium">
                                  LLM clasificó consulta y ejecutó: <code className="font-mono text-[11px] bg-primary-100 text-primary-900 px-1.5 py-0.5 rounded font-semibold">{toolUsed}</code>
                                </p>
                              )}
                              {scenarioInfo?.number === 3 && (
                                <p className="text-warning-800 font-semibold">
                                  LLM clasificó consulta como <code className="font-mono text-[11px] bg-warning-100 text-warning-900 px-1.5 py-0.5 rounded">OUT_OF_SCOPE</code>.
                                </p>
                              )}
                            </div>
                          </div>

                          {/* Paso 4 */}
                          <div className="relative">
                            <span className="absolute -left-[23px] top-0.5 bg-white rounded-full p-0.5 border border-primary-400">
                              <ShieldCheck size={10} className="text-primary-700" />
                            </span>
                            <span className="text-[10px] text-primary-800 block font-extrabold tracking-wider">PASO 4: RESTRICCIÓN DE SEGURIDAD (FSD-BR-09)</span>
                            <p className="text-gray-700 mt-0.5">
                              Acceso acotado por rol y scope del Coordinador de Carrera.
                            </p>
                          </div>

                          {/* Paso 5 */}
                          <div className="relative">
                            <span className="absolute -left-[23px] top-0.5 bg-white rounded-full p-0.5 border border-primary-400">
                              <Database size={10} className="text-primary-700" />
                            </span>
                            <span className="text-[10px] text-primary-800 block font-extrabold tracking-wider">PASO 5: CAPA DE PERSISTENCIA (POSTGRESQL)</span>
                            <p className="text-gray-800 font-semibold mt-0.5">
                              Tablas: <code className="font-mono text-[11px] text-primary-900 bg-primary-100/50 px-1.5 py-0.5 rounded">{dataSource || "Ninguna"}</code>
                            </p>
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Contenido de la pestaña: Ejecución / Chat LLM */}
                    {debugTab === 'llm' && (
                      <div className="space-y-3">
                        <div className="flex items-center gap-2 text-primary-900 text-[11px] font-bold">
                          <MessageSquare size={14} className="text-secondary" />
                          TRAZA DE EJECUCIÓN DEL LLM (PROMPT & CHAIN OF THOUGHT)
                        </div>

                        {llmTrace && (
                          <div className="space-y-4 text-[12px] bg-black text-neutral-200 p-4 rounded-xl font-mono overflow-x-auto shadow-inner border border-neutral-800">
                            <div>
                              <span className="text-cyan-400 font-extrabold block mb-0.5 border-b border-neutral-900 pb-0.5"># SYSTEM PROMPT:</span>
                              <p className="text-neutral-300 leading-normal text-[11px] whitespace-pre-line">{llmTrace.systemPrompt}</p>
                            </div>
                            
                            <div className="mt-3">
                              <span className="text-emerald-400 font-extrabold block mb-0.5 border-b border-neutral-900 pb-0.5"># USER CONSULTA:</span>
                              <p className="text-white font-bold">"{activeQuery}"</p>
                            </div>

                            <div className="mt-3">
                              <span className="text-amber-400 font-extrabold block mb-0.5 border-b border-neutral-900 pb-0.5"># RAZONAMIENTO DEL LLM (Chain of Thought):</span>
                              <p className="text-amber-200 leading-relaxed italic">"{llmTrace.thought}"</p>
                            </div>

                            {llmTrace.toolCall && (
                              <div className="mt-3 bg-neutral-900 p-3 rounded-lg border border-neutral-800">
                                <span className="text-purple-400 font-bold block mb-1"># HERRAMIENTA DISPARADA (Tool Call):</span>
                                <span className="text-emerald-400 font-extrabold">{llmTrace.toolCall.name}</span>
                                <pre className="text-[11px] text-cyan-300 mt-1.5 pl-2 border-l-2 border-cyan-800">
                                  {JSON.stringify(llmTrace.toolCall.arguments, null, 2)}
                                </pre>
                              </div>
                            )}

                            {llmTrace.outOfScope && (
                              <div className="mt-3 bg-red-950/40 p-2.5 rounded border border-red-900/50">
                                <span className="text-red-400 font-bold block"># RESPUESTA DIRECTA LLM:</span>
                                <span className="text-red-200 font-bold">"OUT_OF_SCOPE"</span>
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    )}

                    {/* Contenido de la pestaña: JSON Crudo */}
                    {debugTab === 'json' && (
                      <div className="space-y-3">
                        <div>
                          <span className="block text-[10px] text-gray-700 font-extrabold mb-1">PETICIÓN HTTP ENVIADA:</span>
                          <pre className="p-3 bg-black rounded-xl font-mono text-[11px] text-emerald-400 overflow-x-auto border border-neutral-800">
{JSON.stringify({ 
  "GET": `/api/v1/evidences/search?query=${encodeURIComponent(activeQuery)}`,
  "Headers": {
    "Authorization": "Bearer JWT_TOKEN",
    "X-AI-Enabled": String(xAiEnabled)
  }
}, null, 2)}
                          </pre>
                        </div>
                        <div>
                          <span className="block text-[10px] text-gray-700 font-extrabold mb-1">RESPUESTA JSON DEL SERVIDOR:</span>
                          <pre className="p-3 bg-black rounded-xl font-mono text-[11px] text-amber-300 overflow-x-auto max-h-60 overflow-y-auto border border-neutral-800">
{data ? JSON.stringify(data.data, null, 2) : "// Cargando respuesta..."}
                          </pre>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};


