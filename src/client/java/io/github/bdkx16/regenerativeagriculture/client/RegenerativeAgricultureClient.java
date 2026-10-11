package io.github.bdkx16.regenerativeagriculture.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import io.github.bdkx16.regenerativeagriculture.client.render.ProcessingRenderer;
import io.github.bdkx16.regenerativeagriculture.registry.ModBlockEntities;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import io.github.bdkx16.regenerativeagriculture.client.hud.RelaxedHud;
import io.github.bdkx16.regenerativeagriculture.client.hud.SeasonHud;
import io.github.bdkx16.regenerativeagriculture.client.hud.ThermohygrometerHud;
import io.github.bdkx16.regenerativeagriculture.client.screen.WeatherStationScreen;
import io.github.bdkx16.regenerativeagriculture.client.season.ClientSeasonState;
import io.github.bdkx16.regenerativeagriculture.network.OpenManualPayload;
import io.github.bdkx16.regenerativeagriculture.network.OpenWeatherStationPayload;
import io.github.bdkx16.regenerativeagriculture.client.screen.manual.ManualScreen;

public class RegenerativeAgricultureClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientSeasonState.init();
        SeasonHud.init();
        ThermohygrometerHud.init();
        RelaxedHud.init();
        BlockEntityRenderers.register(ModBlockEntities.DRYING_RACK, ProcessingRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.CURING_JAR, ProcessingRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(OpenManualPayload.TYPE,
                (payload, context) -> context.client().setScreenAndShow(new ManualScreen()));
        ClientPlayNetworking.registerGlobalReceiver(OpenWeatherStationPayload.TYPE,
                (payload, context) -> context.client().setScreenAndShow(new WeatherStationScreen(payload.pos())));
    }
}
