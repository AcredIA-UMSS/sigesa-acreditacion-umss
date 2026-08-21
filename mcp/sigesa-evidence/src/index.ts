import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { z } from 'zod';

const API_URL = (process.env.SIGESA_API_URL ?? 'http://localhost:8080').replace(/\/$/, '');
const JWT = process.env.SIGESA_JWT ?? '';
const DEFAULT_PROGRAM_ID = process.env.SIGESA_PROGRAM_ID;

interface ChatEvidenceContext {
  agent: string;
  programId?: string;
}

interface ChatEvidenceResponse {
  reply: string;
  toolId: string | null;
  path: string | null;
  sourceTables: string[];
}

async function chatEvidence(message: string, programId?: string): Promise<ChatEvidenceResponse> {
  if (!JWT) {
    throw new Error('SIGESA_JWT no configurado. Exporte un JWT de JD/TD/CC.');
  }
  const context: ChatEvidenceContext = { agent: 'evidence' };
  const pid = programId || DEFAULT_PROGRAM_ID;
  if (pid) {
    context.programId = pid;
  }

  const response = await fetch(`${API_URL}/api/v1/assistant/chat`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${JWT}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ message, context, history: [] }),
  });

  const text = await response.text();
  let body: any;
  try {
    body = text ? JSON.parse(text) : {};
  } catch {
    body = { raw: text };
  }

  if (!response.ok) {
    const detail = body?.message ?? body?.error ?? text ?? response.statusText;
    throw new Error(`SIGESA API ${response.status}: ${detail}`);
  }

  const payload = body.data ?? body;
  return {
    reply: payload.reply ?? '',
    toolId: payload.toolId ?? null,
    path: payload.path ?? null,
    sourceTables: payload.sourceTables ?? [],
  };
}

function asTextResult(data: ChatEvidenceResponse) {
  return {
    content: [
      {
        type: 'text' as const,
        text: data.reply,
      },
    ],
  };
}

const server = new McpServer({
  name: 'sigesa-evidence',
  version: '1.0.0',
});

server.tool(
  'list_pending_evidences',
  'Lista indicadores con documentación en estado SUBIDO (pendientes de control TD). PBAC JD/TD/CC.',
  {
    programId: z
      .string()
      .uuid()
      .optional()
      .describe('UUID opcional de carrera para acotar la consulta'),
  },
  async ({ programId }) => {
    const result = await chatEvidence(
      'Lista las evidencias pendientes de revisión',
      programId,
    );
    return asTextResult(result);
  }
);

server.tool(
  'get_evidence_detail',
  'Obtiene metadatos de la evidencia/versión de un indicador (hash, descripción, criterio, estado).',
  {
    indicatorId: z.string().uuid().describe('UUID del indicador'),
    programId: z.string().uuid().optional(),
  },
  async ({ indicatorId, programId }) => {
    const result = await chatEvidence(
      `Detalle de la evidencia del indicador ${indicatorId}`,
      programId,
    );
    return asTextResult(result);
  }
);

server.tool(
  'check_evidence_completeness',
  'Evalúa checklist de completitud (archivo, descripción, criterio, hash) para un indicador.',
  {
    indicatorId: z.string().uuid().describe('UUID del indicador'),
    programId: z.string().uuid().optional(),
  },
  async ({ indicatorId, programId }) => {
    const result = await chatEvidence(
      `Verifica si está completa la evidencia del indicador ${indicatorId}`,
      programId,
    );
    return asTextResult(result);
  }
);

server.tool(
  'approve_indicator',
  'Aprueba un indicador en estado SUBIDO o SUBSANADO (FSD-UC-009). Solo TD y JD.',
  {
    indicatorId: z.string().uuid().describe('UUID del indicador a aprobar'),
    confirmed: z
      .boolean()
      .optional()
      .describe('Si es false (default), solicita vista previa de aprobación. Si es true, ejecuta la aprobación en la base de datos PostgreSQL.'),
    programId: z.string().uuid().optional().describe('UUID opcional de carrera'),
  },
  async ({ indicatorId, confirmed, programId }) => {
    const isConfirmed = confirmed === true;
    const msg = isConfirmed
      ? `Aprueba el indicador ${indicatorId} con confirmed=true`
      : `Aprueba el indicador ${indicatorId} con confirmed=false`;
    const result = await chatEvidence(msg, programId);
    return asTextResult(result);
  }
);

server.tool(
  'reject_indicator',
  'Rechaza u observa un indicador en estado SUBIDO o SUBSANADO registrando justificación obligatoria de al menos 20 caracteres (FSD-UC-008). Solo TD y JD.',
  {
    indicatorId: z.string().uuid().describe('UUID del indicador a rechazar'),
    justification: z
      .string()
      .min(20)
      .describe('Justificación u observación del rechazo (mínimo 20 caracteres)'),
    confirmed: z
      .boolean()
      .optional()
      .describe('Si es false (default), solicita vista previa de rechazo. Si es true, ejecuta el rechazo en la base de datos PostgreSQL.'),
    programId: z.string().uuid().optional().describe('UUID opcional de carrera'),
  },
  async ({ indicatorId, justification, confirmed, programId }) => {
    const isConfirmed = confirmed === true;
    const msg = isConfirmed
      ? `Rechaza el indicador ${indicatorId} con justificación "${justification}" y confirmed=true`
      : `Rechaza el indicador ${indicatorId} con justificación "${justification}" y confirmed=false`;
    const result = await chatEvidence(msg, programId);
    return asTextResult(result);
  }
);

const transport = new StdioServerTransport();
await server.connect(transport);
