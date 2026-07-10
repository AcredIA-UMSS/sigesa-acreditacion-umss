import type { ProcessResponseStatus } from './processResponseStatus';
import type { CreateProcessRequestType } from './createProcessRequestType';

export interface ProcessSummaryResponse {
  processId?: string;
  templateId?: string;
  careerId?: string;
  period?: string;
  type?: CreateProcessRequestType;
  status?: ProcessResponseStatus;
  taxonomySnapshotVersion?: string;
  createdAt?: string;
}
