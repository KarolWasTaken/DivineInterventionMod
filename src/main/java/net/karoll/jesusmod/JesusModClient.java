package net.karoll.jesusmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.karoll.jesusmod.client.JesusHudOverlay;

public class JesusModClient implements ClientModInitializer {

    // runs on startup
    @Override
    public void onInitializeClient() {
        // registers hud
        HudRenderCallback.EVENT.register(new JesusHudOverlay());
        // registers sound
        SoundRegistry.CHURCHBELLS = SoundRegistry.registerSoundEvent("churchbells");
    }
}
