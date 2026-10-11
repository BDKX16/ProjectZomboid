package org.example.pz.projectzomboid_prd.guide;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every route advancement and every manual chapter has its texts in Spanish and English. */
class RouteLangTest {
    private static final Path ROUTE = Path.of("src/main/generated/data/projectzomboid_prd/advancement/route");
    private static final Path LANG = Path.of("src/main/resources/assets/projectzomboid_prd/lang");
    private static final List<String> CHAPTERS = List.of("route", "seasons", "crops", "soil", "climate", "processing",
            "kitchen", "brewing");
    private static final List<String> TEXT_CHAPTERS = List.of("soil", "climate", "processing", "brewing");

    @Test
    void routeAndChaptersAreTranslated() throws IOException {
        List<String> nodes;
        try (Stream<Path> files = Files.list(ROUTE)) {
            nodes = files.map(file -> file.getFileName().toString().replace(".json", "")).toList();
        }
        assertFalse(nodes.isEmpty(), "run ./gradlew runDatagen first");
        for (String lang : List.of("es_ar", "es_es", "es_mx", "en_us")) {
            JsonObject json = JsonParser.parseString(Files.readString(LANG.resolve(lang + ".json"))).getAsJsonObject();
            for (String node : nodes) {
                assertTrue(json.has("route.projectzomboid_prd." + node), lang + ": no title for " + node);
                assertTrue(json.has("route.projectzomboid_prd." + node + ".desc"), lang + ": no description for " + node);
            }
            for (String chapter : CHAPTERS) {
                assertTrue(json.has("manual.projectzomboid_prd." + chapter), lang + ": no tab name for " + chapter);
            }
            for (String chapter : TEXT_CHAPTERS) {
                assertTrue(json.has("manual.projectzomboid_prd." + chapter + ".text"), lang + ": no text for " + chapter);
            }
        }
    }
}
