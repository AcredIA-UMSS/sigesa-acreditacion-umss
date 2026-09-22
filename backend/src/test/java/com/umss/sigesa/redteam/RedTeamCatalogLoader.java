package com.umss.sigesa.redteam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class RedTeamCatalogLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RedTeamCatalogLoader() {
    }

    public static List<RedTeamAttackRecord> loadAttacks() {
        try (InputStream in = RedTeamCatalogLoader.class.getResourceAsStream("/redteam/attacks.catalog.json")) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource /redteam/attacks.catalog.json — run tools/red-team-agent/run.sh sync");
            }
            JsonNode root = MAPPER.readTree(in);
            JsonNode attacks = root.get("attacks");
            if (attacks == null || !attacks.isArray()) {
                throw new IllegalStateException("Invalid red team catalog: missing attacks array");
            }
            return MAPPER.readerForListOf(RedTeamAttackRecord.class).readValue(attacks);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load red team catalog", ex);
        }
    }
}
