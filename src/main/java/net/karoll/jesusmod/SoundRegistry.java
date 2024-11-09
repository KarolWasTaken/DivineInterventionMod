package net.karoll.jesusmod;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.Registries;

public class SoundRegistry {
    // register your sounds here
    // remember to add sounds to sounds.json in /resources/assets/jesusmod/sounds.json
    public static SoundEvent CHURCHBELLS;

    public static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(JesusMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
