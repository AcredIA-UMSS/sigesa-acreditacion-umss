export interface UploadEvidenceResponse {
  evidenceId: string;
  version: number;
  contentHash: string;
  event: string;
  currentState: string;
}

export interface EvidenceUploadParams {
  indicatorId: string;
  criterionId: string;
  description: string;
  file: File;
}
