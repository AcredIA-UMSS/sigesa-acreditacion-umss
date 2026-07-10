import type { CompositeSummaryResponse, ObservationDetail } from '../features/dashboard/types';

export const mockPersonaCC: CompositeSummaryResponse = {
  "userId": "usr-cc-sistemas-01",
  "grantedPermissions": ["READ_CC_DASHBOARD"],
  "coordinatorSection": {
    "programId": "prog-sistemas-umss",
    "programName": "Ingeniería de Sistemas",
    "totalIndicadores": 48,
    "porcentajeAvanceGlobal": 72.5,
    "evidenciasAprobadas": 110,
    "evidenciasRechazadas": 12,
    "observacionesPendientes": 5,
    "fasesAvance": [
      {
        "faseId": 1,
        "nombre": "Fase 1: Autoevaluación",
        "porcentaje": 100.0,
        "estado": "COMPLETADA"
      },
      {
        "faseId": 2,
        "nombre": "Fase 2: Verificación de Evidencias",
        "porcentaje": 70.0,
        "estado": "EN_PROCESO"
      },
      {
        "faseId": 3,
        "nombre": "Fase 3: Visita de Pares Externos",
        "porcentaje": 45.0,
        "estado": "EN_PROCESO"
      },
      {
        "faseId": 4,
        "nombre": "Fase 4: Plan de Mejoras Post-Acreditación",
        "porcentaje": 0.0,
        "estado": "PENDIENTE"
      }
    ],
    "cuellosDeBotella": [
      {
        "indicadorId": "IND-104",
        "codigoCriterio": "CRIT-4.2",
        "diasEstancado": 18
      },
      {
        "indicadorId": "IND-201",
        "codigoCriterio": "CRIT-6.1",
        "diasEstancado": 11
      },
      {
        "indicadorId": "IND-305",
        "codigoCriterio": "CRIT-3.3",
        "diasEstancado": 25
      }
    ]
  },
  "technicianSection": null,
  "executiveSection": null
};

export const mockPersonaTD: CompositeSummaryResponse = {
  "userId": "usr-td-general-02",
  "grantedPermissions": ["READ_TD_DASHBOARD"],
  "coordinatorSection": null,
  "technicianSection": {
    "evidenciasPendientesRevision": 28,
    "indicadoresAsignados": 45,
    "openActions": 12,
    "available": 24,
    "ultimasEvaluaciones": [
      {
        "evidenciaId": "EVID-2026-101",
        "programa": "Ingeniería de Sistemas",
        "fechaRevision": "2026-07-05",
        "resultado": "APROBADO"
      },
      {
        "evidenciaId": "EVID-2026-098",
        "programa": "Ingeniería Civil",
        "fechaRevision": "2026-07-04",
        "resultado": "RECHAZADO"
      },
      {
        "evidenciaId": "EVID-2026-095",
        "programa": "Ingeniería Química",
        "fechaRevision": "2026-07-03",
        "resultado": "APROBADO"
      },
      {
        "evidenciaId": "EVID-2026-090",
        "programa": "Ingeniería Electrónica",
        "fechaRevision": "2026-07-01",
        "resultado": "APROBADO"
      },
      {
        "evidenciaId": "EVID-2026-085",
        "programa": "Ingeniería Mecánica",
        "fechaRevision": "2026-06-28",
        "resultado": "RECHAZADO"
      },
      {
        "evidenciaId": "EVID-2026-081",
        "programa": "Licenciatura en Biología",
        "fechaRevision": "2026-06-25",
        "resultado": "APROBADO"
      }
    ]
  },
  "executiveSection": null
};

export const mockPersonaJD: CompositeSummaryResponse = {
  "userId": "usr-jd-gerencia-03",
  "grantedPermissions": ["READ_JD_DASHBOARD"],
  "coordinatorSection": null,
  "technicianSection": null,
  "executiveSection": {
    "totalProgramasEnAcreditacion": 24,
    "porcentajeAvanceInstitucional": 71.3,
    "criticalObservations": 42,
    "alertPrograms": 4,
    "semaforoProgramas": [
      {
        "programaId": "prog-sistemas-umss",
        "nombre": "Ingeniería de Sistemas",
        "estado": "VERDE",
        "observacionesCriticas": 2
      },
      {
        "programaId": "prog-civil-umss",
        "nombre": "Ingeniería Civil",
        "estado": "AMARILLO",
        "observacionesCriticas": 7
      },
      {
        "programaId": "prog-quimica-umss",
        "nombre": "Ingeniería Química",
        "estado": "ROJO",
        "observacionesCriticas": 14
      },
      {
        "programaId": "prog-electrica-umss",
        "nombre": "Ingeniería Eléctrica",
        "estado": "VERDE",
        "observacionesCriticas": 0
      },
      {
        "programaId": "prog-industrial-umss",
        "nombre": "Ingeniería Industrial",
        "estado": "AMARILLO",
        "observacionesCriticas": 5
      },
      {
        "programaId": "prog-mecanica-umss",
        "nombre": "Ingeniería Mecánica",
        "estado": "ROJO",
        "observacionesCriticas": 11
      },
      {
        "programaId": "prog-alimentos-umss",
        "nombre": "Ingeniería de Alimentos",
        "estado": "VERDE",
        "observacionesCriticas": 1
      },
      {
        "programaId": "prog-biologia-umss",
        "nombre": "Licenciatura en Biología",
        "estado": "VERDE",
        "observacionesCriticas": 2
      }
    ]
  }
};

export const mockPersonaMulti: CompositeSummaryResponse = {
  "userId": "usr-multi-role-04",
  "grantedPermissions": ["READ_CC_DASHBOARD", "READ_TD_DASHBOARD"],
  "coordinatorSection": {
    "programId": "prog-sistemas-umss",
    "programName": "Ingeniería de Sistemas",
    "totalIndicadores": 48,
    "porcentajeAvanceGlobal": 72.5,
    "evidenciasAprobadas": 110,
    "evidenciasRechazadas": 12,
    "observacionesPendientes": 5,
    "fasesAvance": [
      { "faseId": 1, "nombre": "Fase 1: Autoevaluación", "porcentaje": 100.0, "estado": "COMPLETADA" },
      { "faseId": 2, "nombre": "Fase 2: Verificación de Evidencias", "porcentaje": 70.0, "estado": "EN_PROCESO" }
    ],
    "cuellosDeBotella": []
  },
  "technicianSection": {
    "evidenciasPendientesRevision": 9,
    "indicadoresAsignados": 15,
    "openActions": 5,
    "available": 10,
    "ultimasEvaluaciones": [
      { "evidenciaId": "EVID-2026-105", "programa": "Ingeniería Mecánica", "fechaRevision": "2026-07-06", "resultado": "APROBADO" },
      { "evidenciaId": "EVID-2026-108", "programa": "Ingeniería Electrónica", "fechaRevision": "2026-07-07", "resultado": "RECHAZADO" }
    ]
  },
  "executiveSection": null
};

export const mockPersonas = {
  'CC': mockPersonaCC,
  'TD': mockPersonaTD,
  'JD': mockPersonaJD,
  'MULTI': mockPersonaMulti,
} as const;

export type MockPersonaKey = keyof typeof mockPersonas;

export const MOCK_COORDINATOR_OBSERVATIONS: ObservationDetail[] = [
  {
    observacionId: "OBS-2026-001",
    indicadorId: "IND-101",
    codigoIndicador: "IND-1.1.1",
    tituloIndicador: "Misión y Visión del Programa Académico",
    descripcion: "El documento adjunto no cuenta con la firma digital del Honorable Consejo de Carrera.",
    fechaEmision: "2026-06-20",
    fechaLimite: "2026-07-08",
    diasRestantes: 2,
    estado: "PENDIENTE_SUBSANACION",
    urlSubsanacion: "/coordinator/evidences/IND-101/subsanar"
  },
  {
    observacionId: "OBS-2026-002",
    indicadorId: "IND-102",
    codigoIndicador: "IND-1.2.3",
    tituloIndicador: "Plan de Estudios Actualizado",
    descripcion: "Falta adjuntar la malla curricular con la carga horaria desglosada por semestre.",
    fechaEmision: "2026-06-22",
    fechaLimite: "2026-07-09",
    diasRestantes: 3,
    estado: "PENDIENTE_SUBSANACION",
    urlSubsanacion: "/coordinator/evidences/IND-102/subsanar"
  },
  {
    observacionId: "OBS-2026-003",
    indicadorId: "IND-105",
    codigoIndicador: "IND-2.1.4",
    tituloIndicador: "Reglamento de Modalidades de Graduación",
    descripcion: "La normativa citada fue derogada en la gestión 2024. Actualizar al nuevo reglamento.",
    fechaEmision: "2026-06-25",
    fechaLimite: "2026-07-15",
    diasRestantes: 9,
    estado: "EN_REVISION_TECNICA",
    urlSubsanacion: "/coordinator/evidences/IND-105/subsanar"
  },
  {
    observacionId: "OBS-2026-004",
    indicadorId: "IND-201",
    codigoIndicador: "IND-3.1.2",
    tituloIndicador: "Infraestructura de Laboratorios de Computación",
    descripcion: "Evidencia incompleta: falta certificado de calibración y mantenimiento de equipos.",
    fechaEmision: "2026-06-10",
    fechaLimite: "2026-07-18",
    diasRestantes: 12,
    estado: "PENDIENTE_SUBSANACION",
    urlSubsanacion: "/coordinator/evidences/IND-201/subsanar"
  },
  {
    observacionId: "OBS-2026-005",
    indicadorId: "IND-204",
    codigoIndicador: "IND-3.2.1",
    tituloIndicador: "Licencias de Software Especializado",
    descripcion: "El convenio con Microsoft Azure caducó. Adjuntar renovación vigente para la gestión 2026.",
    fechaEmision: "2026-06-15",
    fechaLimite: "2026-07-20",
    diasRestantes: 14,
    estado: "PENDIENTE_SUBSANACION",
    urlSubsanacion: "/coordinator/evidences/IND-204/subsanar"
  },
  {
    observacionId: "OBS-2026-006",
    indicadorId: "IND-301",
    codigoIndicador: "IND-4.1.1",
    tituloIndicador: "Grado Académico del Personal Docente",
    descripcion: "No se adjuntaron los títulos de maestría de los 4 nuevos docentes a dedicación exclusiva.",
    fechaEmision: "2026-06-18",
    fechaLimite: "2026-07-22",
    diasRestantes: 16,
    estado: "PENDIENTE_SUBSANACION",
    urlSubsanacion: "/coordinator/evidences/IND-301/subsanar"
  },
  {
    observacionId: "OBS-2026-007",
    indicadorId: "IND-302",
    codigoIndicador: "IND-4.1.3",
    tituloIndicador: "Evaluación del Desempeño Docente",
    descripcion: "Falta el informe estadístico de satisfacción estudiantil del semestre II/2025.",
    fechaEmision: "2026-06-20",
    fechaLimite: "2026-07-25",
    diasRestantes: 19,
    estado: "EN_REVISION_TECNICA",
    urlSubsanacion: "/coordinator/evidences/IND-302/subsanar"
  },
  {
    observacionId: "OBS-2026-008",
    indicadorId: "IND-305",
    codigoIndicador: "IND-4.3.2",
    tituloIndicador: "Producción Científica y Publicaciones",
    descripcion: "Los enlaces a los artículos indexados en Scopus devuelven error 404.",
    fechaEmision: "2026-06-21",
    fechaLimite: "2026-07-26",
    diasRestantes: 20,
    estado: "PENDIENTE_SUBSANACION",
    urlSubsanacion: "/coordinator/evidences/IND-305/subsanar"
  },
  {
    observacionId: "OBS-2026-009",
    indicadorId: "IND-401",
    codigoIndicador: "IND-5.1.1",
    tituloIndicador: "Seguimiento a Graduados y Titulados",
    descripcion: "Muestra estadística insuficiente en el estudio de inserción laboral (menor al 15%).",
    fechaEmision: "2026-06-22",
    fechaLimite: "2026-07-28",
    diasRestantes: 22,
    estado: "PENDIENTE_SUBSANACION",
    urlSubsanacion: "/coordinator/evidences/IND-401/subsanar"
  },
  {
    observacionId: "OBS-2026-010",
    indicadorId: "IND-402",
    codigoIndicador: "IND-5.2.1",
    tituloIndicador: "Convenios de Prácticas Profesionales",
    descripcion: "Los convenios empresariales presentados no tienen firmas legalizadas por Asesoría Legal.",
    fechaEmision: "2026-06-25",
    fechaLimite: "2026-07-30",
    diasRestantes: 24,
    estado: "RECHAZADO",
    urlSubsanacion: "/coordinator/evidences/IND-402/subsanar"
  },
  {
    observacionId: "OBS-2026-011",
    indicadorId: "IND-501",
    codigoIndicador: "IND-6.1.1",
    tituloIndicador: "Presupuesto Operativo Anual (POA)",
    descripcion: "El monto asignado a investigación no concuerda con las directrices de la DUEA.",
    fechaEmision: "2026-06-26",
    fechaLimite: "2026-08-02",
    diasRestantes: 27,
    estado: "PENDIENTE_SUBSANACION",
    urlSubsanacion: "/coordinator/evidences/IND-501/subsanar"
  },
  {
    observacionId: "OBS-2026-012",
    indicadorId: "IND-502",
    codigoIndicador: "IND-6.2.1",
    tituloIndicador: "Adquisición de Material Bibliográfico",
    descripcion: "Falta el catálogo de nuevos libros digitales adquiridos para la biblioteca de facultad.",
    fechaEmision: "2026-06-27",
    fechaLimite: "2026-08-05",
    diasRestantes: 30,
    estado: "EN_REVISION_TECNICA",
    urlSubsanacion: "/coordinator/evidences/IND-502/subsanar"
  },
  {
    observacionId: "OBS-2026-013",
    indicadorId: "IND-601",
    codigoIndicador: "IND-7.1.1",
    tituloIndicador: "Políticas de Bienestar Estudiantil",
    descripcion: "No se evidencia el registro de beneficiarios del comedor universitario de la carrera.",
    fechaEmision: "2026-06-28",
    fechaLimite: "2026-08-10",
    diasRestantes: 35,
    estado: "PENDIENTE_SUBSANACION",
    urlSubsanacion: "/coordinator/evidences/IND-601/subsanar"
  },
  {
    observacionId: "OBS-2026-014",
    indicadorId: "IND-602",
    codigoIndicador: "IND-7.2.1",
    tituloIndicador: "Programas de Apoyo Psicopedagógico",
    descripcion: "El informe semestral no incluye estadísticas de deserción escolar mitigada.",
    fechaEmision: "2026-06-28",
    fechaLimite: "2026-08-12",
    diasRestantes: 37,
    estado: "PENDIENTE_SUBSANACION",
    urlSubsanacion: "/coordinator/evidences/IND-602/subsanar"
  },
  {
    observacionId: "OBS-2026-015",
    indicadorId: "IND-701",
    codigoIndicador: "IND-8.1.1",
    tituloIndicador: "Interacción Social y Extensión Universitaria",
    descripcion: "Falta adjuntar actas de conformidad de las comunidades beneficiadas por los proyectos.",
    fechaEmision: "2026-06-29",
    fechaLimite: "2026-08-15",
    diasRestantes: 40,
    estado: "RECHAZADO",
    urlSubsanacion: "/coordinator/evidences/IND-701/subsanar"
  }
];

export const mockObservations = MOCK_COORDINATOR_OBSERVATIONS;
