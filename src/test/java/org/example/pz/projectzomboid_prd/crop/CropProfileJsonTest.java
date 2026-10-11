package org.example.pz.projectzomboid_prd.crop;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every crop profile JSON shipped with the mod must decode and be internally consistent.
 * Mod blocks are not registered in unit tests, so for those only the growth fields are decoded here;
 * the game test checks that they load in a real server.
 */
class CropProfileJsonTest {
    private static final Path DATA = Path.of("src/main/resources/data");

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void allProfilesDecode() throws IOException {
        List<Path> files;
        try (Stream<Path> stream = Files.walk(DATA)) {
            files = stream.filter(p -> p.toString().replace('\\', '/').contains("/projectzomboid_prd/crop_profile/"))
                    .filter(p -> p.toString().endsWith(".json"))
                    .toList();
        }
        assertFalse(files.isEmpty(), "no crop profiles found");

        for (Path file : files) {
            JsonElement json = JsonParser.parseString(Files.readString(file));
            String block = json.getAsJsonObject().get("block").getAsString();
            GrowthRequirements requirements;
            if (block.startsWith("minecraft:")) {
                CropProfile profile = CropProfile.CODEC.parse(JsonOps.INSTANCE, json)
                        .getOrThrow(error -> new AssertionError(file + ": " + error));
                assertNotEquals(Blocks.AIR, profile.block(), file + ": unknown block");
                requirements = profile.requirements();
            } else {
                PlantFamily.CODEC.parse(JsonOps.INSTANCE, json.getAsJsonObject().get("family"))
                        .getOrThrow(error -> new AssertionError(file + ": " + error));
                requirements = GrowthRequirements.MAP_CODEC.codec().parse(JsonOps.INSTANCE, json)
                        .getOrThrow(error -> new AssertionError(file + ": " + error));
            }

            GrowthRequirements.TemperatureRange t = requirements.temperature();
            assertTrue(t.min() < t.optimalMin() && t.optimalMin() <= t.optimalMax() && t.optimalMax() < t.max(),
                    file + ": temperatures must be min < optimal_min <= optimal_max < max");
            assertFalse(requirements.seasons().isEmpty(), file + ": no seasons");
        }
    }
}
