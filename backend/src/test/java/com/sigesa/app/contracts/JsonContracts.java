package com.sigesa.app.contracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Forma JSON camelCase de DTOs web (Jackson default = Spring Boot sin override). */
public final class JsonContracts {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private JsonContracts() {
    }

    public static JsonNode tree(Object dto) {
        return MAPPER.valueToTree(dto);
    }

    public static void assertObjectFields(JsonNode node, String... fieldNames) {
        assertThat(node.isObject()).isTrue();
        Set<String> actual = new LinkedHashSet<>();
        node.fieldNames().forEachRemaining(actual::add);
        assertThat(actual).containsExactlyInAnyOrder(fieldNames);
    }
}
