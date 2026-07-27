export type DashboardRolePermission = 'READ_CC_DASHBOARD' | 'READ_TD_DASHBOARD' | 'READ_JD_DASHBOARD';

export interface CompositeSummaryResponse {
  userId: string;
  grantedPermissions: DashboardRolePermission[];
  coordinatorSection: CoordinatorSection | null;
  technicianSection: TechnicianSection | null;
  executiveSection: ExecutiveSection | null;
}

// [CC] Coordinador de Carrera Contract
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

// [TD] Técnico DUEA Contract
export interface TechnicianSection {
  evidenciasPendientesRevision: number;
  indicadoresAsignados: number;
  openActions?: number;
  available?: number;
  ultimasEvaluaciones?: Array<{
    evidenciaId: string;
    programa: string;
    fechaRevision: string;
    resultado: 'APROBADO' | 'RECHAZADO';
  }>;
}

// [JD] Jefatura DUEA Contract
export interface ExecutiveSection {
  totalProgramasEnAcreditacion?: number;
  porcentajeAvanceInstitucional?: number;
  criticalObservations?: number;
  alertPrograms?: number;
  semaforoProgramas?: Array<{
    programaId: string;
    nombre: string;
    estado: 'VERDE' | 'AMARILLO' | 'ROJO';
    observacionesCriticas: number;
  }>;
}

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
