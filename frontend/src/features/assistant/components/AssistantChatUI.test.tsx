import { createRef } from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { AssistantChatUI } from './AssistantChatUI';

describe('AssistantChatUI', () => {
  it('shouldRenderEmptyChatAndSendDraft', async () => {
    const user = userEvent.setup();
    const onSend = vi.fn();
    const onDraftChange = vi.fn();
    render(
      <AssistantChatUI
        messages={[]}
        draft="¿Cómo cargo evidencias?"
        onDraftChange={onDraftChange}
        onSend={onSend}
        onClear={vi.fn()}
        onSampleSelect={vi.fn()}
        onOpenActionHistory={vi.fn()}
        actionHistoryCount={0}
        model="llama3.2"
        llmEnabled
        capabilities={['Consultar procesos']}
        demoScenarios={[
          {
            number: 1,
            title: 'Listar procesos',
            sampleQuestion: 'Muéstrame los procesos activos',
            expectedPath: 'KEYWORD',
          },
        ]}
        isAssistantEnabled
        isStatusError={false}
        isStatusLoading={false}
        isSending={false}
        errorMessage={null}
        messagesContainerRef={createRef<HTMLDivElement>()}
        messagesEndRef={createRef<HTMLDivElement>()}
      />,
    );

    expect(screen.getByRole('heading', { name: 'Asistente virtual' })).toBeInTheDocument();
    expect(screen.getByText('Escenarios demo')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /Enviar/ }));
    expect(onSend).toHaveBeenCalledOnce();
  });

  it('shouldShowStatusAndChatErrors', () => {
    render(
      <AssistantChatUI
        messages={[]}
        draft=""
        onDraftChange={vi.fn()}
        onSend={vi.fn()}
        onClear={vi.fn()}
        onSampleSelect={vi.fn()}
        onOpenActionHistory={vi.fn()}
        actionHistoryCount={0}
        model="llama3.2"
        llmEnabled={false}
        capabilities={[]}
        demoScenarios={[]}
        isAssistantEnabled
        isStatusError
        isStatusLoading={false}
        isSending={false}
        errorMessage="Mensaje demasiado largo."
        messagesContainerRef={createRef<HTMLDivElement>()}
        messagesEndRef={createRef<HTMLDivElement>()}
      />,
    );

    expect(screen.getByText(/No se pudo consultar el estado del asistente/)).toBeInTheDocument();
    expect(screen.getByText('Mensaje demasiado largo.')).toBeInTheDocument();
  });
});
