import type { DomainCopilotKind } from '../types/domainCopilotKind';

export interface DomainCopilotPresentation {
  contextLabel: string;
  defaultTitle: string;
  typingLabel: string;
  fabAriaLabel: string;
}

export const DOMAIN_COPILOT_PRESENTATION: Record<DomainCopilotKind, DomainCopilotPresentation> = {
  phases: {
    contextLabel: 'Fases',
    defaultTitle: 'Copiloto de fases',
    typingLabel: 'Consultando fases…',
    fabAriaLabel: 'Abrir copiloto de fases',
  },
  evidence: {
    contextLabel: 'Evidencias',
    defaultTitle: 'Copiloto documental',
    typingLabel: 'Consultando evidencias…',
    fabAriaLabel: 'Abrir copiloto de evidencias',
  },
  users: {
    contextLabel: 'Usuarios',
    defaultTitle: 'Copiloto de usuarios',
    typingLabel: 'Consultando usuarios…',
    fabAriaLabel: 'Abrir copiloto de usuarios',
  },
};
