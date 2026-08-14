package com.umss.sigesa.adapter.out.assistant.mcp;

import com.umss.sigesa.adapter.out.assistant.mcp.dto.UserContextDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Configuration
public class AcademicContextMcpServer {

    public record TokenRequest(String phrase, UserContextDto userContext) {}
    public record Subset(String label, String keyword) {}
    public record TokenResponse(List<Subset> subsets) {}

    @Bean
    @Description("Resuelve tokens de búsqueda y retorna subconjuntos basados en los filtros y seguridad del usuario")
    public Function<TokenRequest, TokenResponse> resolveSearchTokensAndSubsets() {
        return request -> {
            String role = request.userContext().getRole();
            String scope = request.userContext().getProgramScope();
            
            // Lógica dummy para la estructura, puedes ajustarlo si necesitas hacer query a pg_trgm.
            // Aquí el MCP empaqueta la consulta en subconjuntos.
            return new TokenResponse(Arrays.asList(
                    new Subset("Resultados Exactos", request.phrase()),
                    new Subset("Búsqueda Ampliada", request.phrase().split(" ")[0])
            ));
        };
    }
}
