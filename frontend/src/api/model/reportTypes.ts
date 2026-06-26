export interface GenerateExecutiveReportRequest {
  facultyId?: string;
  programId?: string;
  managementYear: number;
}

export interface ReportJobAcceptedResponse {
  jobId: string;
}

export interface ReportJobStatusResponse {
  jobId: string;
  status: string;
  downloadUrl?: string;
  errorCode?: string;
}
