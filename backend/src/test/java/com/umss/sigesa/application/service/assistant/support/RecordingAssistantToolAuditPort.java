package com.umss.sigesa.application.service.assistant.support;

import com.umss.sigesa.application.model.assistant.AssistantToolAuditRecord;
import com.umss.sigesa.application.port.out.AssistantToolAuditPort;

import java.util.ArrayList;
import java.util.List;

public class RecordingAssistantToolAuditPort implements AssistantToolAuditPort {

    private final List<AssistantToolAuditRecord> records = new ArrayList<>();

    @Override
    public void logToolInvocation(AssistantToolAuditRecord record) {
        records.add(record);
    }

    public List<AssistantToolAuditRecord> records() {
        return List.copyOf(records);
    }

    public void clear() {
        records.clear();
    }
}
