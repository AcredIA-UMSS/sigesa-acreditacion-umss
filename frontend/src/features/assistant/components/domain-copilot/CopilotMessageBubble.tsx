import { Bot, User } from 'lucide-react';
import type { ChatMessage } from '../../../../api/model/assistantTypes';
import { CopilotAssistantMetadata } from '../CopilotAssistantMetadata';

interface CopilotMessageBubbleProps {
  message: ChatMessage;
}

export function CopilotMessageBubble({ message }: CopilotMessageBubbleProps) {
  const isUser = message.role === 'user';

  return (
    <div className={`flex gap-2 ${isUser ? 'flex-row-reverse' : 'flex-row'}`}>
      <div
        className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full ${
          isUser ? 'bg-primary-600 text-body' : 'bg-secondary-50 text-secondary-600'
        }`}
      >
        {isUser ? <User size={14} /> : <Bot size={14} />}
      </div>
      <div
        className={`max-w-[90%] rounded-xl px-3 py-2 text-body-md leading-relaxed ${
          isUser
            ? 'bg-primary-600 text-body'
            : 'border border-gray-200 bg-gray-50 text-gray-900'
        }`}
      >
        <p className="whitespace-pre-wrap">{message.content}</p>
        {!isUser && message.metadata && (
          <CopilotAssistantMetadata metadata={message.metadata} compact />
        )}
      </div>
    </div>
  );
}
