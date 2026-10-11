package org.example.pz.projectzomboid_prd.client;

import net.fabricmc.api.ClientModInitializer;
import org.example.pz.projectzomboid_prd.client.hud.SeasonHud;
import org.example.pz.projectzomboid_prd.client.season.ClientSeasonState;

public class Projectzomboid_prdClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientSeasonState.init();
        SeasonHud.init();
    }
}
