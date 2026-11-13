import type { MethodologicalStageResponseDto } from '../../../api/model';

export function currentStage(stages: MethodologicalStageResponseDto[]): MethodologicalStageResponseDto | undefined {
  const ordered = [...stages].sort((left, right) => (left.order ?? 0) - (right.order ?? 0));
  return ordered.find((stage) => stage.status === 'IN_PROGRESS')
    ?? ordered.find((stage) => stage.status === 'OBSERVED')
    ?? ordered.find((stage) => stage.status === 'SUBMITTED_FOR_REVIEW')
    ?? ordered.find((stage) => stage.status === 'PENDING')
    ?? ordered.at(-1);
}

export interface GuideStageContent {
  code: string;
  guideTitle: string;
  result: string;
  activities: string[];
  note?: string;
}

export const START_DOCUMENTS = [
  'Carta de solicitud de participación',
];

export const ARCU_STAGE_GUIDE: GuideStageContent[] = [
  {
    code: 'PREPARATORY',
    guideTitle: 'Etapa preparatoria',
    result: 'Comisión organizada y proceso planificado.',
    activities: [
      'Sensibilización de la comunidad universitaria',
      'Organización de la Comisión de Autoevaluación',
      'Evaluación de cumplimiento de recomendaciones anteriores',
      'Designación del coordinador del proceso',
    ],
  },
  {
    code: 'COLLECTION',
    guideTitle: 'Etapa de recolección de información',
    result: 'Información recopilada y fundamentada.',
    activities: [
      'Recolección de información secundaria',
      'Recolección de información primaria',
      'Aplicación de encuestas',
      'Interpretación y fundamentación de resultados',
    ],
  },
  {
    code: 'SYSTEMATIZATION',
    guideTitle: 'Etapa de sistematización de información',
    result: 'Información sistematizada.',
    activities: [
      'Organización de la información',
      'Clasificación de la información',
      'Análisis de la información',
    ],
  },
  {
    code: 'DRAFTING',
    guideTitle: 'Etapa de elaboración de documentos',
    result: 'Documentación de autoevaluación elaborada.',
    activities: [
      'Organización y elaboración del informe de autoevaluación',
      'Identificación de fortalezas y debilidades',
      'Formulación del plan de mejoras',
    ],
  },
  {
    code: 'PRESENTATION',
    guideTitle: 'Etapa de presentación de documentos',
    result: 'Documentación presentada.',
    activities: [
      'Revisión y consolidación de documentos',
      'Presentación de documentos requeridos',
    ],
  },
  {
    code: 'EXTERNAL_PREP',
    guideTitle: 'Preparación de la evaluación externa',
    result: 'Carrera preparada para recibir a los pares evaluadores.',
    activities: [
      'Simulación del proceso de evaluación externa',
      'Preparación de documentación e información',
      'Instalación de la sala de pares evaluadores',
    ],
  },
  {
    code: 'POST_ACCREDITATION',
    guideTitle: 'Continuación del proceso',
    result: 'Evaluación externa y decisión de acreditación.',
    activities: [
      'Evaluación externa por pares',
      'Decisión de acreditación y comunicación oficial de resultados',
    ],
    note: 'La guía metodológica de autoevaluación no detalla estas fases. Deben complementarse con la normativa oficial de ARCU-SUR.',
  },
];

export const COST_GUIDE = {
  title: 'Etapa de previsión de costos',
  result: 'Recursos financieros previstos.',
  activities: [
    'Estimación y planificación presupuestaria',
    'Seguimiento y control de gastos',
  ],
  note: 'La guía ARCU-SUR ubica esta etapa entre la presentación de documentos y la preparación de la evaluación externa. En el proceso, la previsión presupuestaria se registra como entregable de la etapa preparatoria.',
};

export const DELIVERABLE_LABEL: Record<string, string> = {
  HCC_RESOLUTION: 'Resolución del Honorable Consejo de Carrera',
  WORK_SCHEDULE: 'Cronograma de trabajo',
  PRIOR_RECOMMENDATIONS_REPORT: 'Informe de recomendaciones anteriores',
  BUDGET_FORECAST: 'Previsión presupuestaria',
  SECONDARY_EVIDENCE_COMPLETE: 'Evidencia secundaria completa',
  PRIMARY_SURVEY_REPORT: 'Informe de encuesta primaria',
  COLLECTION_GAP_REPORT: 'Informe de brechas de recolección',
  FODA_FORMS_COMPLETE: 'Formularios FODA completos',
  SYNTHESIS_MATRIX: 'Matriz de síntesis',
  SELF_ASSESSMENT_REPORT: 'Informe de autoevaluación',
  OFFICIAL_DATA_FORM: 'Formulario oficial de datos',
  IMPROVEMENT_PLAN: 'Plan de mejoras',
  ACADEMIC_DEV_PLAN: 'Plan de desarrollo académico',
  COMMUNITY_VALIDATION_MINUTES: 'Acta de validación con la comunidad',
  FORMAL_SUBMISSION_PACKAGE: 'Paquete de presentación formal',
  SIMULATION_REPORT: 'Informe de simulación',
  PEER_ROOM_CHECKLIST: 'Lista de verificación de la sala de pares',
  IMPROVEMENT_PROGRESS_REPORT: 'Informe de avance del plan de mejoras',
};

export const APPROVAL_LABEL: Record<string, string> = {
  PENDING: 'Pendiente',
  APPROVED: 'Aprobado',
  REJECTED: 'Rechazado',
};
