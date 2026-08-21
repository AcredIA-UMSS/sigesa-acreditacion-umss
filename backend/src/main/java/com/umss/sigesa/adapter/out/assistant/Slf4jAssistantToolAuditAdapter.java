package com.umss.sigesa.adapter.out.assistant;

import com.umss.sigesa.application.model.assistant.AssistantToolAuditRecord;
import com.umss.sigesa.application.port.out.AssistantToolAuditPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Slf4jAssistantToolAuditAdapter implements AssistantToolAuditPort {

    private static final Logger log = LoggerFactory.getLogger(Slf4jAssistantToolAuditAdapter.class);

    @Override
    public void logToolInvocation(AssistantToolAuditRecord record) {
        log.info(
                "AUDIT_ASSISTANT_TOOL userId={} role={} agent={} tool={} sideEffect={} success={} outcome={}",
                record.userId(),
                record.role(),
                record.agentId(),
                record.toolId(),
                record.sideEffect(),
                record.success(),
                record.outcomeCode());
    }
}
