package org.example.pz.projectzomboid_prd;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every craftable block and tool must have a generated recipe (run {@code ./gradlew runDatagen} after changes). */
class RecipesTest {
    private static final Path RECIPES = Path.of("src/main/generated/data/projectzomboid_prd/recipe");
    private static final List<String> CRAFTABLE = List.of(
            "grow_light", "extractor", "weather_station", "compost_bin",
            "pruning_shears", "thermohygrometer", "soil_test_kit", "straw_mulch");

    @Test
    void craftableThingsHaveRecipes() {
        for (String name : CRAFTABLE) {
            assertTrue(Files.exists(RECIPES.resolve(name + ".json")), "missing recipe for " + name);
        }
    }
}
