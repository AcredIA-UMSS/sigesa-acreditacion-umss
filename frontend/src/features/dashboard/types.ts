// 1. Composite Summary Contract
export interface CompositeSummaryResponse {
  userId: string;
  grantedPermissions: string[];
  coordinatorSection: CoordinatorSection | null;
  technicianSection: TechnicianSection | null;
  executiveSection: ExecutiveSection | null;
}

export interface CoordinatorSection {
  programId: string;
  programName: string;
  totalIndicadores: number;
  porcentajeAvanceGlobal: number;
  evidenciasAprobadas: number;
  evidenciasRechazadas: number;
  observacionesPendientes: number;
  fasesAvance: Array<{
    faseId: number;
    nombre: string;
    porcentaje: number;
    estado: 'COMPLETADA' | 'EN_PROCESO' | 'PENDIENTE';
  }>;
  cuellosDeBotella: Array<{
    indicadorId: string;
    codigoCriterio: string;
    diasEstancado: number;
  }>;
}

export interface TechnicianSection {
  evidenciasPendientesRevision: number;
  indicadoresAsignados: number;
}

export type ExecutiveSection = Record<string, unknown>; // Expandable per v1.0 specs

// 2. Paginated Details Contract
export interface ObservationDetail {
  observacionId: string;
  indicadorId: string;
  codigoIndicador: string;
  tituloIndicador: string;
  descripcion: string;
  fechaEmision: string;
  fechaLimite: string;
  diasRestantes: number;
  estado: string;
  urlSubsanacion: string;
}

export interface PaginatedObservationsResponse {
  content: ObservationDetail[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
