import { http, HttpResponse } from 'msw';
import type { CompositeDashboardSummary } from '../../api/model/compositeDashboardSummary';
import type { ProcessSummaryResponseDto } from '../../api/model/processSummaryResponseDto';

export const dashboardSummaryCc: CompositeDashboardSummary = {
  userId: 'usr-cc-sistemas-01',
  grantedPermissions: ['READ_CC_DASHBOARD'],
  coordinatorSection: {
    programId: 'prog-sistemas-umss',
    programName: 'Ingeniería de Sistemas',
    totalIndicators: 48,
    overallProgressPercentage: 72.5,
    approvedEvidences: 110,
    rejectedEvidences: 12,
    pendingObservations: 5,
    phaseProgressList: [
      { phaseId: 1, name: 'Fase 1: Autoevaluación', percentage: 100, status: 'COMPLETADA' },
      { phaseId: 2, name: 'Fase 2: Verificación de Evidencias', percentage: 70, status: 'EN_PROCESO' },
    ],
    bottlenecks: [{ indicatorId: 'IND-104', criterionCode: 'CRIT-4.2', daysStagnant: 18 }],
  },
  technicianSection: undefined,
  executiveSection: undefined,
};

export const dashboardSummaryTd: CompositeDashboardSummary = {
  userId: 'usr-td-general-02',
  grantedPermissions: ['READ_TD_DASHBOARD'],
  technicianSection: {
    evidencesPendingReview: 28,
    assignedIndicators: 45,
    openActions: 12,
    available: 24,
    recentEvaluations: [
      {
        evidenceId: 'EVID-2026-101',
        program: 'Ingeniería de Sistemas',
        revisionDate: '2026-07-05',
        result: 'APROBADO',
      },
    ],
  },
};

export const dashboardSummaryJd: CompositeDashboardSummary = {
  userId: 'usr-jd-duea-03',
  grantedPermissions: ['READ_JD_DASHBOARD'],
  executiveSection: {
    totalPrograms: 12,
    averageGlobalProgress: 61,
    criticalObservations: 7,
    alertPrograms: 3,
    programTrafficLights: [
      { programId: 'p1', name: 'Ingeniería de Sistemas', status: 'AMARILLO', criticalObservations: 2 },
    ],
  },
};

export const sampleProcess: ProcessSummaryResponseDto = {
  id: '950e8400-e29b-41d4-a716-446655440020',
  careerName: 'Ingeniería de Sistemas',
  careerCode: 'INF-SIS',
  templateName: 'CEUB 2026',
  templateType: 'CEUB',
  status: 'ACTIVE',
};

export const handlers = [
  http.post('/api/v1/auth/login', async ({ request }) => {
    const body = (await request.json()) as { email?: string; password?: string };
    if (body.password === 'wrong') {
      return HttpResponse.json(
        { error: 'AUTH_INVALID_CREDENTIALS', message: 'Credenciales inválidas' },
        { status: 401 },
      );
    }
    if (body.email === 'inactive@umss.edu.bo') {
      return HttpResponse.json(
        { error: 'ACCESS_DENIED', message: 'La cuenta no tiene un rol asignado.' },
        { status: 403 },
      );
    }
    return HttpResponse.json({
      accessToken: 'jwt-from-msw',
      expiresIn: 3600,
      role: 'CC',
      programScope: ['prog-sistemas-umss'],
    });
  }),

  http.get('/api/v1/dashboards/me/summary', () => {
    return HttpResponse.json(dashboardSummaryCc);
  }),

  http.get('/api/v1/processes', () => {
    return HttpResponse.json([sampleProcess]);
  }),

  http.post('/api/v1/processes/:processId/phases/:phaseId/complete', () => {
    return HttpResponse.json({
      phaseId: 'phase-1',
      previousState: 'ABIERTA',
      newState: 'COMPLETADA',
      event: 'PhaseCompleted',
    });
  }),

  http.post('/api/v1/subphases/:subphaseId/approve', ({ params }) => {
    return HttpResponse.json({
      subphaseId: params.subphaseId,
      transition: {
        subphaseId: params.subphaseId,
        previousState: 'SUBIDO',
        newState: 'APROBADO',
      },
    });
  }),

  http.post('/api/v1/subphases/:subphaseId/reject', async ({ params, request }) => {
    const body = (await request.json()) as { justification?: string };
    if (!body.justification || body.justification.length < 20) {
      return HttpResponse.json(
        { error: 'JUSTIFICATION_REQUIRED', message: 'La justificación debe tener al menos 20 caracteres.' },
        { status: 400 },
      );
    }
    return HttpResponse.json({
      subphaseId: params.subphaseId,
      observationId: 'obs-1',
      transition: {
        subphaseId: params.subphaseId,
        previousState: 'SUBIDO',
        newState: 'OBSERVADO',
      },
    });
  }),
];
