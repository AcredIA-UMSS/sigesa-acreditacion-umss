/**
 * Tipos auxiliares de carga de evidencia (UI / cliente).
 * Contrato Orval: uploadParams.ts, uploadBody.ts, uploadEvidenceResponse.ts.
 */
export interface EvidenceUploadParams {
  indicatorId: string;
  criterionId: string;
  description: string;
  file: File;
}
