import type {
  CompositeDashboardSummary,
  CoordinatorKpiSection,
  TechnicianKpiSection,
  ExecutiveKpiSection,
  ObservationSummary,
  PageObservationSummary,
} from '../../../api/model';
import type {
  CompositeSummaryResponse,
  CoordinatorSection,
  TechnicianSection,
  ExecutiveSection,
  ObservationDetail,
  PaginatedObservationsResponse,
  DashboardRolePermission,
} from '../types';

export function mapCompositeSummary(
  dto?: CompositeDashboardSummary
): CompositeSummaryResponse {
  if (!dto) {
    return {
      userId: '',
      grantedPermissions: [],
      coordinatorSection: null,
      technicianSection: null,
      executiveSection: null,
    };
  }

  return {
    userId: dto.userId ?? '',
    grantedPermissions: (dto.grantedPermissions ?? []) as DashboardRolePermission[],
    coordinatorSection: mapCoordinatorSection(dto.coordinatorSection),
    technicianSection: mapTechnicianSection(dto.technicianSection),
    executiveSection: mapExecutiveSection(dto.executiveSection),
  };
}

function mapCoordinatorSection(
  dto?: CoordinatorKpiSection
): CoordinatorSection | null {
  if (!dto) return null;

  return {
    programId: dto.programId ?? '',
    programName: dto.programName ?? '',
    totalIndicadores: dto.totalIndicators ?? 0,
    porcentajeAvanceGlobal: dto.overallProgressPercentage ?? 0,
    evidenciasAprobadas: dto.approvedEvidences ?? 0,
    evidenciasRechazadas: dto.rejectedEvidences ?? 0,
    observacionesPendientes: dto.pendingObservations ?? 0,
    fasesAvance: (dto.phaseProgressList ?? []).map((p) => ({
      faseId: p.phaseId ?? 0,
      nombre: p.name ?? '',
      porcentaje: p.percentage ?? 0,
      estado: (p.status ?? 'PENDIENTE') as 'COMPLETADA' | 'EN_PROCESO' | 'PENDIENTE',
    })),
    cuellosDeBotella: (dto.bottlenecks ?? []).map((b) => ({
      indicadorId: b.indicatorId ?? '',
      codigoCriterio: b.criterionCode ?? '',
      diasEstancado: b.daysStagnant ?? 0,
    })),
  };
}

function mapTechnicianSection(
  dto?: TechnicianKpiSection
): TechnicianSection | null {
  if (!dto) return null;

  return {
    evidenciasPendientesRevision: dto.evidencesPendingReview ?? 0,
    indicadoresAsignados: dto.assignedIndicators ?? 0,
    ultimasEvaluaciones: (dto as any).ultimasEvaluaciones ?? [],
  };
}

function mapExecutiveSection(
  dto?: ExecutiveKpiSection
): ExecutiveSection | null {
  if (!dto) return null;

  return {
    totalProgramasEnAcreditacion: dto.totalPrograms ?? 0,
    porcentajeAvanceInstitucional: dto.averageGlobalProgress ?? 0,
    semaforoProgramas: (dto as any).semaforoProgramas ?? [],
  };
}

export function mapObservationDetail(
  dto: ObservationSummary
): ObservationDetail {
  return {
    observacionId: dto.observationId ?? '',
    indicadorId: dto.indicatorId ?? '',
    codigoIndicador: dto.indicatorCode ?? '',
    tituloIndicador: dto.indicatorTitle ?? '',
    descripcion: dto.description ?? '',
    fechaEmision: dto.issueDate ?? '',
    fechaLimite: dto.dueDate ?? '',
    diasRestantes: dto.remainingDays ?? 0,
    estado: dto.status ?? '',
    urlSubsanacion: dto.remediationUrl ?? '',
  };
}

export function mapPaginatedObservations(
  dto?: PageObservationSummary
): PaginatedObservationsResponse {
  if (!dto) {
    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 10,
      number: 0,
    };
  }

  return {
    content: (dto.content ?? []).map(mapObservationDetail),
    totalElements: dto.totalElements ?? 0,
    totalPages: dto.totalPages ?? 0,
    size: dto.size ?? 10,
    number: dto.number ?? 0,
  };
}
