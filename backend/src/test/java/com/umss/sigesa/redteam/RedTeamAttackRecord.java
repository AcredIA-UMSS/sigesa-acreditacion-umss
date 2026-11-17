package com.umss.sigesa.redteam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RedTeamAttackRecord(
        String id,
        String category,
        String title,
        String agent,
        String userMessage,
        List<HistoryEntry> history,
        List<String> forbiddenInReply,
        List<String> forbidRegex,
        Integer expectHttpStatus,
        String expectErrorCode) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HistoryEntry(String role, String content) {
    }
}
