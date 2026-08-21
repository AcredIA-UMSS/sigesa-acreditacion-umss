import { Sidebar } from '../../components/layout/Sidebar';
import { AssistantActionDebugModal } from './components/AssistantActionDebugModal';
import { AssistantChatUI } from './components/AssistantChatUI';
import { useAssistantChat } from './hooks/useAssistantChat';

export function AssistantPage() {
  const chat = useAssistantChat();

  return (
    <div className="flex h-screen bg-body">
      <Sidebar activeNav="help" />
      <AssistantActionDebugModal
        isOpen={chat.actionModalOpen}
        isSending={chat.isSending}
        actions={chat.actionHistory}
        onClose={() => chat.setActionModalOpen(false)}
        showDevBadge={chat.showDevBadge}
      />
      <AssistantChatUI
        messages={chat.messages}
        draft={chat.draft}
        onDraftChange={chat.setDraft}
        onSend={() => void chat.sendMessage()}
        onClear={chat.clearConversation}
        onSampleSelect={chat.setDraft}
        onOpenActionHistory={() => chat.setActionModalOpen(true)}
        actionHistoryCount={chat.actionHistory.length}
        model={chat.model}
        llmEnabled={chat.llmEnabled}
        capabilities={chat.capabilities}
        demoScenarios={chat.demoScenarios}
        isAssistantEnabled={chat.isAssistantEnabled}
        isStatusError={chat.isStatusError}
        isStatusLoading={chat.isStatusLoading}
        isSending={chat.isSending}
        errorMessage={chat.errorMessage}
        messagesContainerRef={chat.messagesContainerRef}
        messagesEndRef={chat.messagesEndRef}
      />
    </div>
  );
}
