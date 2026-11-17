import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const FIXTURE_DIR = path.join(path.dirname(fileURLToPath(import.meta.url)), '../../fixtures');

/** Playwright a veces envía application/octet-stream; forzamos application/pdf. */
export function evidencePdfUpload() {
  const filePath = path.join(FIXTURE_DIR, 'evidencia-e2e.pdf');
  return {
    name: 'evidencia-e2e.pdf',
    mimeType: 'application/pdf',
    buffer: fs.readFileSync(filePath),
  };
}
