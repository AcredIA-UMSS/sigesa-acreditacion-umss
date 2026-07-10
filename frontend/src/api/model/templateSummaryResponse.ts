import type { CreateProcessRequestType } from './createProcessRequestType';

export interface TemplateSummaryResponse {
  id?: string;
  validated?: boolean;
  taxonomyVersion?: string;
  activePeriod?: string;
  activatedAt?: string;
  type?: CreateProcessRequestType;
}
