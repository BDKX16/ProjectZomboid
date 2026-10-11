package org.example.pz.projectzomboid_prd.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.example.pz.projectzomboid_prd.client.hud.RelaxedHud;
import org.example.pz.projectzomboid_prd.client.hud.SeasonHud;
import org.example.pz.projectzomboid_prd.client.hud.ThermohygrometerHud;
import org.example.pz.projectzomboid_prd.client.screen.WeatherStationScreen;
import org.example.pz.projectzomboid_prd.client.season.ClientSeasonState;
import org.example.pz.projectzomboid_prd.network.OpenManualPayload;
import org.example.pz.projectzomboid_prd.network.OpenWeatherStationPayload;
import org.example.pz.projectzomboid_prd.client.screen.manual.ManualScreen;

public class Projectzomboid_prdClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientSeasonState.init();
        SeasonHud.init();
        ThermohygrometerHud.init();
        RelaxedHud.init();
        ClientPlayNetworking.registerGlobalReceiver(OpenManualPayload.TYPE,
                (payload, context) -> context.client().setScreenAndShow(new ManualScreen()));
        ClientPlayNetworking.registerGlobalReceiver(OpenWeatherStationPayload.TYPE,
                (payload, context) -> context.client().setScreenAndShow(new WeatherStationScreen(payload.pos())));
    }
}
