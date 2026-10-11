package org.example.pz.projectzomboid_prd.client.season;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.example.pz.projectzomboid_prd.network.SeasonConfigPayload;
import org.example.pz.projectzomboid_prd.season.ClimateModel;
import org.example.pz.projectzomboid_prd.season.SeasonCalendar;
import org.jspecify.annotations.Nullable;

/** Calendar and climate as known by the client, received from the server on join. */
public final class ClientSeasonState {
    private static @Nullable ClimateModel climate;

    private ClientSeasonState() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SeasonConfigPayload.TYPE, (payload, context) ->
                climate = new ClimateModel(new SeasonCalendar(payload.daysPerSeason()), payload.climateSeed()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> climate = null);
    }

    /** Null while not connected to a server with this mod. */
    public static @Nullable ClimateModel climate() {
        return climate;
    }
}
