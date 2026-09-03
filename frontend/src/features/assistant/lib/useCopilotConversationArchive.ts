import { useCallback, useMemo, useState } from 'react';
import type { ChatMessage } from '../../../api/model/assistantTypes';
import type { CopilotConversationArchive, DomainCopilotKind } from '../types/domainCopilotKind';

const STORAGE_PREFIX = 'sigesa-copilot-archive';
const MAX_ARCHIVES = 20;

function storageKey(kind: DomainCopilotKind, contextKey: string): string {
  return `${STORAGE_PREFIX}:${kind}:${contextKey}`;
}

function readArchives(kind: DomainCopilotKind, contextKey: string): CopilotConversationArchive[] {
  if (typeof window === 'undefined') {
    return [];
  }
  try {
    const raw = window.sessionStorage.getItem(storageKey(kind, contextKey));
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw) as CopilotConversationArchive[];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function writeArchives(
  kind: DomainCopilotKind,
  contextKey: string,
  archives: CopilotConversationArchive[],
): void {
  if (typeof window === 'undefined') {
    return;
  }
  window.sessionStorage.setItem(
    storageKey(kind, contextKey),
    JSON.stringify(archives.slice(0, MAX_ARCHIVES)),
  );
}

function toArchiveMessages(messages: ChatMessage[]) {
  return messages.map(({ role, content, createdAt }) => ({
    role,
    content,
    createdAt,
  }));
}

function firstUserPreview(messages: ChatMessage[]): string {
  const first = messages.find((message) => message.role === 'user');
  if (!first) {
    return 'Conversación sin mensajes';
  }
  return first.content.length > 72 ? `${first.content.slice(0, 72).trim()}…` : first.content;
}

export function useCopilotConversationArchive(
  kind: DomainCopilotKind,
  contextKey: string,
  title: string,
  subtitle?: string,
) {
  const [archives, setArchives] = useState<CopilotConversationArchive[]>(() =>
    readArchives(kind, contextKey),
  );

  const refreshArchives = useCallback(() => {
    setArchives(readArchives(kind, contextKey));
  }, [kind, contextKey]);

  const archiveCurrentConversation = useCallback(
    (messages: ChatMessage[]) => {
      if (messages.length === 0) {
        return;
      }
      const now = new Date().toISOString();
      const firstAt = messages[0]?.createdAt ?? now;
      const entry: CopilotConversationArchive = {
        id: crypto.randomUUID(),
        kind,
        contextKey,
        title,
        subtitle,
        startedAt: firstAt,
        updatedAt: now,
        preview: firstUserPreview(messages),
        messages: toArchiveMessages(messages),
      };
      const next = [entry, ...readArchives(kind, contextKey)].slice(0, MAX_ARCHIVES);
      writeArchives(kind, contextKey, next);
      setArchives(next);
    },
    [kind, contextKey, title, subtitle],
  );

  const removeArchive = useCallback(
    (archiveId: string) => {
      const next = readArchives(kind, contextKey).filter((entry) => entry.id !== archiveId);
      writeArchives(kind, contextKey, next);
      setArchives(next);
    },
    [kind, contextKey],
  );

  const archiveCount = useMemo(() => archives.length, [archives.length]);

  return {
    archives,
    archiveCount,
    refreshArchives,
    archiveCurrentConversation,
    removeArchive,
  };
}
