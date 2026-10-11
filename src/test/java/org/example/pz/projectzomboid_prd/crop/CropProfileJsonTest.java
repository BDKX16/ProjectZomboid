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

/** Every crop profile JSON shipped with the mod must decode and be internally consistent. */
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
            CropProfile profile = CropProfile.CODEC.parse(JsonOps.INSTANCE, json)
                    .getOrThrow(error -> new AssertionError(file + ": " + error));
            assertNotEquals(Blocks.AIR, profile.block(), file + ": unknown block");

            GrowthRequirements.TemperatureRange t = profile.requirements().temperature();
            assertTrue(t.min() < t.optimalMin() && t.optimalMin() <= t.optimalMax() && t.optimalMax() < t.max(),
                    file + ": temperatures must be min < optimal_min <= optimal_max < max");
            assertFalse(profile.requirements().seasons().isEmpty(), file + ": no seasons");
        }
    }
}
