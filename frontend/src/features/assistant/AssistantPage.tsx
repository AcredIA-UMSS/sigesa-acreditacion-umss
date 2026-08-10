import { Sidebar } from '../../components/layout/Sidebar';
import { AssistantChatUI } from './components/AssistantChatUI';
import { useAssistantChat } from './hooks/useAssistantChat';

export function AssistantPage() {
  const chat = useAssistantChat();

  return (
    <div className="flex h-screen bg-body">
      <Sidebar activeNav="help" />
      <AssistantChatUI
        messages={chat.messages}
        draft={chat.draft}
        onDraftChange={chat.setDraft}
        onSend={chat.sendMessage}
        onClear={chat.clearConversation}
        model={chat.model}
        isAssistantEnabled={chat.isAssistantEnabled}
        isStatusError={chat.isStatusError}
        isStatusLoading={chat.isStatusLoading}
        isSending={chat.isSending}
        errorMessage={chat.errorMessage}
        messagesEndRef={chat.messagesEndRef}
      />
    </div>
  );
}
