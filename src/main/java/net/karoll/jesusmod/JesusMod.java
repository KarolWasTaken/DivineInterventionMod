package net.karoll.jesusmod;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JesusMod implements ModInitializer {
	public static final String MOD_ID = "jesusmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// lol need to change this from default
		LOGGER.info("Hello Fabric world!");
	}
}