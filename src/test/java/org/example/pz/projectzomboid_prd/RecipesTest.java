package org.example.pz.projectzomboid_prd;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every craftable block, tool and meal must have a generated recipe (run {@code ./gradlew runDatagen} after changes). */
class RecipesTest {
    private static final Path RECIPES = Path.of("src/main/generated/data/projectzomboid_prd/recipe");
    private static final List<String> CRAFTABLE = List.of(
            "grow_light", "extractor", "weather_station", "compost_bin",
            "pruning_shears", "thermohygrometer", "soil_test_kit", "straw_mulch",
            "hamburger", "green_salad", "steamed_broccoli", "soy_stew", "squash_soup", "pesto_pasta", "tomato_juice",
            "roasted_sunflower_seeds", "roasted_sunflower_seeds_from_smoking", "roasted_sunflower_seeds_from_campfire_cooking",
            "drying_rack", "curing_jar", "mate_gourd", "mate", "coffee", "wort", "ground_chili", "chimichurri", "asado",
            "locro", "joint", "brownie", "cigar", "roasted_coffee", "roasted_corn");

    @Test
    void craftableThingsHaveRecipes() {
        for (String name : CRAFTABLE) {
            assertTrue(Files.exists(RECIPES.resolve(name + ".json")), "missing recipe for " + name);
        }
    }
}
