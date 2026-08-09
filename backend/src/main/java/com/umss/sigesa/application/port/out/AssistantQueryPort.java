package com.umss.sigesa.application.port.out;

import java.util.Map;

public interface AssistantQueryPort {
    Map<String, String> classifyAndRoute(String query);
}
