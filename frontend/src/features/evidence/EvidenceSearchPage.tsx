import { useState } from 'react';
import { Sidebar } from '../../components/layout/Sidebar';
import { useSearch } from '../../api/endpoints/evidence-search/evidence-search';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { TextInput } from '../../components/ui/TextInput';
import { 
  Search, 
  Brain, 
  AlertCircle, 
  FileText, 
  Terminal, 
  Sparkles, 
  ChevronRight,
  FolderOpen
} from 'lucide-react';

export const EvidenceSearchPage = () => {
  const [queryInput, setQueryInput] = useState('');
  const [activeQuery, setActiveQuery] = useState('');
  const [xAiEnabled, setXAiEnabled] = useState(true);

  const { data, isLoading, error } = useSearch(
    { query: activeQuery },
    {
      query: {
        enabled: true,
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
  };

  const executePreset = (queryText: string, aiToggle: boolean) => {
    setQueryInput(queryText);
    setXAiEnabled(aiToggle);
    setActiveQuery(queryText);
  };

  const getDebugJson = (): string => {
    const errorObj = error as { message?: string; status?: number; response?: { status?: number; data?: unknown }; data?: unknown } | null;
    const debugInfo = {
      timestamp: new Date().toISOString(),
      request: {
        url: activeQuery ? `/api/v1/evidences/search?query=${encodeURIComponent(activeQuery)}` : null,
        headers: {
          'X-AI-Enabled': String(xAiEnabled)
        }
      },
      response: data ? {
        status: data.status,
        headers: Array.from((data.headers as Headers || new Headers()).entries()).reduce<Record<string, string>>((acc, [k, v]) => {
          acc[k] = v;
          return acc;
        }, {}),
        payload: data.data
      } : null,
      error: errorObj ? {
        message: errorObj.message || 'Error de conexión',
        status: errorObj.status || errorObj.response?.status,
        details: errorObj.response?.data || errorObj.data || errorObj
      } : null
    };
    return JSON.stringify(debugInfo, null, 2);
  };

  const subsets = data?.data?.subsets || [];
  const routingPath = data?.data?.routingPath;
  const message = data?.data?.message;

  // Determinar si hay al menos un resultado en todos los subconjuntos
  const hasAnyResult = subsets.some(subset => subset.results && subset.results.length > 0);

  const getDemoScenarioInfo = () => {
    if (!activeQuery) return null;
    
    if (routingPath === 'KEYWORD') {
      return {
        number: 1,
        title: "Escenario 1/4: Búsqueda Tradicional",
        description: "Se resuelve de manera directa en base de datos sin IA, o la IA fue desactivada/falló.",
        color: "border-success-500/20 bg-success/5 text-success",
        badge: "KEYWORD MATCH"
      };
    }

    if (routingPath === 'REFUSAL') {
      return {
        number: 3,
        title: "Escenario 3: Fuera de Alcance",
        description: "El LLM detecta que la consulta no pertenece a acreditación y la rechaza.",
        color: "border-warning/30 bg-warning/5 text-warning-700",
        badge: "REFUSAL OUT-OF-SCOPE"
      };
    }

    return {
      number: 2,
      title: "Escenario 2: MCP Multi-Token",
      description: "El LLM ejecutó el servidor MCP embebido generando múltiples subconjuntos.",
      color: "border-secondary-500/20 bg-secondary-50/50 text-secondary-600",
      badge: "LLM MULTIPATH"
    };
  };

  const scenarioInfo = getDemoScenarioInfo();

  return (
    <div className="flex h-screen bg-gray-50 text-primary-900">
      <Sidebar activeNav="processes" />
      <main className="flex-1 overflow-y-auto p-8 bg-gray-50/50">
        <div className="max-w-7xl mx-auto space-y-8">
          
          <div>
            <h1 className="text-heading-xl font-bold tracking-tight text-primary-800 flex items-center gap-3">
              <Search className="text-secondary" size={32} />
              Buscar Evidencia Inteligente (MCP)
            </h1>
            <p className="mt-1 text-body-lg text-gray-700">
              Localiza documentos de acreditación mediante consultas naturales, soportado por un servidor MCP local para resultados multi-subconjunto.
            </p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
            
            <div className="lg:col-span-7 space-y-6">
              
              <form onSubmit={handleSearch} className="bg-white p-6 rounded-2xl border border-primary-100 shadow-sm space-y-4">
                <div className="flex gap-4">
                  <div className="flex-1">
                    <TextInput
                      label="Consulta de búsqueda"
                      placeholder="Ej. 'aulas de clase', 'infraestructura'..."
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
                      <span>Habilitar Asistente MCP IA (Cabecera <code className="text-secondary font-mono bg-secondary-50/50 px-1 py-0.5 rounded text-[11px]">X-AI-Enabled</code>)</span>
                    </label>
                  </div>
                </div>
              </form>

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

              {!isLoading && !error && data && (
                <div className="space-y-6">
                  {routingPath === 'REFUSAL' ? (
                    <div className="p-6 bg-white border-l-4 border-warning rounded-2xl shadow-sm space-y-2 border border-primary-100">
                      <div className="flex items-center gap-2 text-warning-700 font-bold">
                        <AlertCircle size={20} />
                        Búsqueda no procesada
                      </div>
                      <p className="text-body-md text-gray-700 font-semibold">{message}</p>
                    </div>
                  ) : (
                    <div className="bg-white rounded-2xl border border-primary-100 overflow-hidden shadow-sm">
                      <div className="p-5 border-b border-primary-100 bg-primary-50/10 flex justify-between items-center">
                        <h2 className="text-heading-sm font-bold text-primary-850">Subconjuntos de Evidencias</h2>
                      </div>
                      
                      {!hasAnyResult ? (
                        <div className="p-12 text-center text-gray-400 bg-white">
                          <AlertCircle className="mx-auto text-gray-300 mb-3" size={48} />
                          No se encontraron evidencias registradas que correspondan a su búsqueda.
                        </div>
                      ) : (
                        <div className="p-4 space-y-6">
                          {subsets.map((subset, index) => (
                            <div key={index} className="border border-primary-200 rounded-xl overflow-hidden">
                              <div className="bg-primary-50/50 px-4 py-3 flex items-center gap-2 border-b border-primary-200 font-bold text-primary-800">
                                <FolderOpen size={18} className="text-secondary" />
                                {subset.label} 
                                <span className="ml-auto text-sm text-gray-500 font-normal">
                                  {subset.results?.length || 0} resultados
                                </span>
                              </div>
                              
                              {(!subset.results || subset.results.length === 0) ? (
                                <div className="p-4 text-sm text-gray-400 text-center bg-white">
                                  Vacío
                                </div>
                              ) : (
                                <div className="overflow-x-auto bg-white">
                                  <table className="w-full text-left border-collapse">
                                    <thead>
                                      <tr className="bg-gray-50/50 text-label-sm text-primary-700 border-b border-primary-100">
                                        <th className="p-3 font-semibold">Archivo</th>
                                        <th className="p-3 font-semibold">Descripción</th>
                                        <th className="p-3 font-semibold">Dimensión</th>
                                      </tr>
                                    </thead>
                                    <tbody className="divide-y divide-primary-100 text-body-sm text-gray-700">
                                      {subset.results.map((evidence) => (
                                        <tr key={evidence.evidenceId} className="hover:bg-primary-50/10">
                                          <td className="p-3 font-semibold text-primary-900 flex items-center gap-2 max-w-[200px] truncate">
                                            <FileText size={16} className="text-secondary min-w-[16px]" />
                                            <span title={evidence.title}>{evidence.title}</span>
                                          </td>
                                          <td className="p-3 max-w-[250px] truncate" title={evidence.description}>
                                            {evidence.description}
                                          </td>
                                          <td className="p-3 font-semibold text-primary-700">
                                            {evidence.dimensionName}
                                          </td>
                                        </tr>
                                      ))}
                                    </tbody>
                                  </table>
                                </div>
                              )}
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>

            <div className="lg:col-span-5 space-y-6">
              <div className="bg-white p-5 rounded-2xl border border-primary-100 shadow-sm">
                <h3 className="text-label-lg font-bold text-primary-850 flex items-center gap-2 mb-3">
                  <Sparkles size={16} className="text-warning" />
                  Casos de Prueba (Demo)
                </h3>
                
                <div className="space-y-2">
                  <button type="button" onClick={() => executePreset('infraestructura', true)} className="w-full flex items-center justify-between p-2.5 rounded-xl bg-gray-50 hover:bg-primary-50/50 border border-primary-100 text-left text-body-sm transition-all">
                    <div>
                      <span className="block font-semibold text-success">Escenario 1: "infraestructura"</span>
                      <span className="text-[11px] text-gray-500">Coincidencia directa (No usa IA)</span>
                    </div>
                    <ChevronRight size={16} className="text-gray-400" />
                  </button>
                  <button type="button" onClick={() => executePreset('aulas de computación y laboratorios', true)} className="w-full flex items-center justify-between p-2.5 rounded-xl bg-gray-50 hover:bg-primary-50/50 border border-primary-100 text-left text-body-sm transition-all">
                    <div>
                      <span className="block font-semibold text-secondary">Escenario 2: Búsqueda Multi-Token</span>
                      <span className="text-[11px] text-gray-500">Mapea con MCP a subconjuntos</span>
                    </div>
                    <ChevronRight size={16} className="text-gray-400" />
                  </button>
                  <button type="button" onClick={() => executePreset('¿cómo hacer una pizza?', true)} className="w-full flex items-center justify-between p-2.5 rounded-xl bg-gray-50 hover:bg-primary-50/50 border border-primary-100 text-left text-body-sm transition-all">
                    <div>
                      <span className="block font-semibold text-warning-700">Escenario 3: "¿cómo hacer una pizza?"</span>
                      <span className="text-[11px] text-gray-500">Fuera de alcance</span>
                    </div>
                    <ChevronRight size={16} className="text-gray-400" />
                  </button>
                </div>
              </div>

              <div className="bg-white p-5 rounded-2xl border border-primary-100 shadow-sm space-y-4">
                <div className="flex flex-col gap-3 border-b border-primary-100 pb-3">
                  <h3 className="text-label-lg font-bold text-primary-850 flex items-center gap-2">
                    <Terminal size={16} className="text-secondary" />
                    Consola de Depuración
                  </h3>
                  <div className="flex bg-gray-100 p-1 rounded-lg text-[12px] font-semibold text-gray-600">
                    <span className="flex-1 text-center py-1.5 rounded-md bg-white text-primary-900 shadow-sm">
                      Detalle de Carga Útil JSON de Depuración
                    </span>
                  </div>
                </div>

                {!activeQuery ? (
                  <div className="p-8 text-center text-gray-400 text-body-sm">
                    Realiza una búsqueda para ver los subconjuntos estructurados.
                  </div>
                ) : (
                  <div className="space-y-4">
                    {scenarioInfo && (
                      <div className={`p-3.5 rounded-xl border ${scenarioInfo.color} space-y-1`}>
                        <div className="flex justify-between items-center">
                          <span className="font-bold text-body-sm">{scenarioInfo.title}</span>
                          <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-white/70 border border-primary-200">{scenarioInfo.badge}</span>
                        </div>
                        <p className="text-[11px] opacity-90">{scenarioInfo.description}</p>
                      </div>
                    )}
                    
                    <div className="space-y-3">
                      <div>
                        <span className="block text-[10px] text-gray-700 font-extrabold mb-1">PAYLOAD DE DEPURACIÓN (Solicitud y Respuesta/Error):</span>
                        <pre className="p-3 bg-black rounded-xl font-mono text-[11px] text-amber-300 overflow-x-auto max-h-96 overflow-y-auto border border-neutral-800">
{getDebugJson()}
                        </pre>
                      </div>
                    </div>
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
