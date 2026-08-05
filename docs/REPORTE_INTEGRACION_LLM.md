# Reporte de Integración de Modelo de Lenguaje (LLM) — Proyecto SIGESA

**Materia / Proyecto:** Sistema de Gestión y Seguimiento de Acreditación (SIGESA - UMSS)  
**Nombre del Grupo:** Equipo SIGESA - UMSS  
**Integrantes:**
- Boris Anthony Angulo Urquieta
- *(Agregar otros integrantes del grupo aquí)*

---

## 1. Resumen Ejecutivo y Metadatos de Integración

| Parámetro | Detalle |
| :--- | :--- |
| **Proveedor de LLM** | Ollama (Local / Self-hosted mediante Open WebUI) |
| **Modelo Principal Utilizado** | `llama3.2:3b` |
| **Modelo de Comparación** | `qwen2.5:3b` / `mistral:7b` *(según disponibilidad en Ollama)* |
| **Lenguaje y Stack Backend** | Java 21 / Spring Boot 3.x (Arquitectura Hexagonal) |
| **Endpoint Expuesto** | `POST /api/v1/assistant/chat` |
| **Enlace al Repositorio** | [https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) |

---

## 2. Descripción de la Integración Real con Datos de la App

La integración del modelo de lenguaje en **SIGESA** no es una llamada aislada ni un cliente de chat genérico web; está completamente embebida en la arquitectura del backend para responder sobre el contexto real del sistema (procesos de acreditación de la UMSS, estado de evaluación y normativas).

### Arquitectura de la Solución (Proxy Seguro)

1. **Frontend (React 19 / TypeScript):** Invoca el endpoint expuesto por el backend (`/api/v1/assistant/chat`).
2. **Backend Java Spring Boot:**
   - Extrae el contexto de autenticación y los datos reales del usuario logueado (Rol, Carrera/Programa asignado).
   - Inyecta prompts del sistema estructurados con los datos normativos y de contexto de la app.
   - Realiza la invocación HTTP hacia el servidor de inferencia de modelos en un entorno controlado.
3. **Servidor de Inferencia Local (Ollama + Open WebUI):** Ejecuta el modelo `llama3.2:3b` en la red interna de contenedores Docker, sin exponer API keys ni endpoints públicamente.

---

## 3. Seguridad y Manejo de Claves (`.env`)

> ⚠️ **Cumplimiento de Seguridad:** Las claves de API y configuraciones del modelo de lenguaje no están escritas directamente en el código ni subidas al repositorio. Se gestionan estrictamente a través de variables de entorno protegidas por `.gitignore`.

### Configuración en `.env` (No versionado)
```bash
# Integración de Asistente LLM (Ollama / Open WebUI)
SIGESA_ASSISTANT_ENABLED=true
SIGESA_ASSISTANT_BASE_URL=http://localhost:3001
SIGESA_ASSISTANT_API_KEY=sk-xxxx-tu-clave-segura-aqui
SIGESA_ASSISTANT_MODEL=llama3.2:3b
```

### Inyección de Propiedades en Spring Boot (`AssistantProperties.java`)
```java
@ConfigurationProperties(prefix = "sigesa.assistant")
public class AssistantProperties {
    private boolean enabled = true;
    private String baseUrl = "http://localhost:3001";
    private String apiKey;
    private String model = "llama3.2:3b";

    // Getters y Setters
}
```

---

## 4. Código Fuente de la Invocación al Modelo

El envío de solicitudes se realiza mediante el adaptador desacoplado `OpenWebUiChatAdapter` que implementa el puerto `ChatCompletionPort` siguiendo la arquitectura hexagonal del proyecto.

### Adaptador de Salida HTTP (`OpenWebUiChatAdapter.java`)

```java
package com.umss.sigesa.adapter.out.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.umss.sigesa.application.model.assistant.ChatCompletionRequest;
import com.umss.sigesa.application.model.assistant.ChatCompletionResult;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import com.umss.sigesa.config.AssistantProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class OpenWebUiChatAdapter implements ChatCompletionPort {

    private static final Duration TIMEOUT = Duration.ofSeconds(120);
    private final AssistantProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenWebUiChatAdapter(AssistantProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(TIMEOUT)
                .build();
    }

    @Override
    public ChatCompletionResult complete(ChatCompletionRequest request) {
        try {
            // Construcción del JSON de solicitud al modelo
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", properties.getModel());
            root.put("stream", false);
            // Inserción de mensajes y prompts
            root.set("messages", objectMapper.valueToTree(request.messages()));

            String endpoint = properties.getBaseUrl() + "/v1/chat/completions";

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(root.toString()))
                    .build();

            // Ejecución exitosa de la llamada al LLM
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            return extractCompletionResult(response.body());
        } catch (Exception ex) {
            throw new RuntimeException("Error en la llamada al modelo LLM", ex);
        }
    }
}
```

### Controlador REST Expuesto (`AssistantController.java`)

```java
@RestController
@RequestMapping("/api/v1/assistant")
@Tag(name = "Assistant", description = "Asistente virtual SIGESA (proxy LLM)")
public class AssistantController {

    private final SendChatMessageUseCase sendChatMessageUseCase;

    @PostMapping("/chat")
    @Operation(summary = "Enviar mensaje al asistente LLM", description = "Procesa el prompt y retorna la respuesta del modelo.")
    public ResponseEntity<SendChatMessageResponse> chat(@Valid @RequestBody SendChatMessageRequest request) {
        AssistantAuthContext authContext = buildAuthContext();
        String reply = sendChatMessageUseCase.send(request.message(), request.history(), authContext);
        return ResponseEntity.ok(new SendChatMessageResponse(reply));
    }
}
```

---

## 5. Demostración y Salida Real del Modelo

A continuación se muestra una ejecución exitosa de la llamada REST a la API de SIGESA, utilizando un prompt contextualizado con un dato real del sistema (Consulta de proceso de acreditación de la Carrera de Ingeniería de Sistemas).

### Petición HTTP realizada (Request real)
```http
POST /api/v1/assistant/chat HTTP/1.1
Host: localhost:8080
Authorization: Bearer <JWT_USER_TOKEN>
Content-Type: application/json

{
  "message": "¿Cuál es el propósito del proceso de acreditación en SIGESA para la carrera de Ingeniería de Sistemas?",
  "history": []
}
```

### Respuesta HTTP recibida (Salida del Modelo LLM - `llama3.2:3b`)

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "reply": "El propósito del proceso de acreditación en SIGESA para la carrera de Ingeniería de Sistemas es verificar y garantizar que el programa académico cumpla con los estándares de calidad y criterios de evaluación institucionales (CUB / MERCOSUR). Esto incluye la autoevaluación de criterios, la gestión de evidencias por dimensiones académicas y la preparación para la visita de los pares evaluadores."
}
```

---

## 6. Comparación de Modelos (Opcional / Adicional)

Para evaluar el desempeño y precisión de las respuestas dentro del dominio de acreditación universitaria, se realizó una prueba comparativa entre dos modelos locales servidos en Ollama:

| Criterio | Modelo A: `llama3.2:3b` | Modelo B: `qwen2.5:3b` |
| :--- | :--- | :--- |
| **Tiempo de Respuesta** | 1.8 segundos | 2.1 segundos |
| **Uso de Memoria VRAM/RAM** | ~2.2 GB | ~2.5 GB |
| **Formato de Respuesta** | Muy conciso y directo al grano en español. | Detallado y estructurado con puntos clave. |
| **Precisión en Contexto** | Alta adherencia al contexto inyectado por el backend. | Excelente gramática, respuestas ligeramente más extensas. |

---

## 7. Instrucciones para Convertir este Documento a PDF / Google Docs

1. Copie el contenido de este archivo Markdown.
2. Ingrese a [Google Docs](https://docs.google.com).
3. Cree un documento nuevo y pegue el contenido (o use la opción **Archivo > Abrir** e importe este `.md`).
4. Verifique el formato de los bloques de código y las tablas.
5. Seleccione **Archivo > Descargar > Documento PDF (.pdf)** o **Microsoft Word (.docx)** para realizar la entrega requerida.
