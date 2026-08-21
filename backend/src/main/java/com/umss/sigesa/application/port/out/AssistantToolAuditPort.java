package com.umss.sigesa.application.port.out;

import com.umss.sigesa.application.model.assistant.AssistantToolAuditRecord;

public interface AssistantToolAuditPort {

    void logToolInvocation(AssistantToolAuditRecord record);
}
