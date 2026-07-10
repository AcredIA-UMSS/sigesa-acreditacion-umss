export interface EvidenceSearchItemResponse {
  evidenceId?: string;
  indicatorId?: string;
  indicatorCode?: string;
  indicatorTitle?: string;
  programId?: string;
  phaseId?: number;
  latestVersion?: number;
  description?: string;
  createdAt?: string;
}

export interface EvidenceSearchPageResponse {
  content?: EvidenceSearchItemResponse[];
  page?: number;
  size?: number;
  totalElements?: number;
  totalPages?: number;
}

export interface SearchEvidencesParams {
  programId?: string;
  phaseId?: number;
  indicatorId?: string;
  q?: string;
  page?: number;
  size?: number;
}

export interface ListIndicatorsParams {
  programId?: string;
  phaseId?: number;
}

export interface ListProcessesParams {
  status?: string;
  careerId?: string;
  period?: string;
}
