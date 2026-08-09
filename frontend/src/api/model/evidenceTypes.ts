/**
 * Parámetros del cliente axios con progreso (US-025).
 * Respuesta: ver uploadEvidenceResponse.ts (Orval / EvidenceController).
 */
export interface UploadEvidenceParams {
  indicatorId: string;
  criterionId: string;
  description: string;
  file: File;
}
