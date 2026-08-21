package com.umss.sigesa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sigesa.assistant")
public class AssistantProperties {

    private boolean enabled = true;
    private boolean llmEnabled = true;
    private String baseUrl = "http://localhost:3000/api";
    private String apiKey = "";
    private String model = "llama3.2:3b";
    private String systemPrompt =
            "Eres el asistente virtual de SIGESA (Sistema de Gestión de Acreditación UMSS). "
                    + "Ayudas a coordinadores, jefes de departamento y técnicos con procesos de acreditación, "
                    + "evidencias, indicadores y uso del sistema. Responde en español, de forma clara y concisa.";
    private int maxToolIterations = 3;
    private boolean ragEnabled = true;
    private int ragMaxChunks = 3;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLlmEnabled() {
        return llmEnabled;
    }

    public void setLlmEnabled(boolean llmEnabled) {
        this.llmEnabled = llmEnabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public int getMaxToolIterations() {
        return maxToolIterations;
    }

    public void setMaxToolIterations(int maxToolIterations) {
        this.maxToolIterations = maxToolIterations;
    }

    public boolean isRagEnabled() {
        return ragEnabled;
    }

    public void setRagEnabled(boolean ragEnabled) {
        this.ragEnabled = ragEnabled;
    }

    public int getRagMaxChunks() {
        return ragMaxChunks;
    }

    public void setRagMaxChunks(int ragMaxChunks) {
        this.ragMaxChunks = ragMaxChunks;
    }
}
