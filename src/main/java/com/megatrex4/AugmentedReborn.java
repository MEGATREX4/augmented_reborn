package com.megatrex4;

import com.megatrex4.event.TooltipEventListener;
import com.megatrex4.items.ItemGroupRegistry;
import com.megatrex4.items.ItemRegistry;
import com.megatrex4.items.KeyBindingRegistry;
import com.megatrex4.network.ActivateNanoHelmetPacket;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AugmentedReborn implements ModInitializer {
	public static final String MOD_ID = "augmented_reborn";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ItemRegistry.ItemRegistry();
		ItemGroupRegistry.ItemGroupRegistry();
		KeyBindingRegistry.registerKeybindings();
		ActivateNanoHelmetPacket.register();

		TooltipEventListener.register();

		LOGGER.info("Hello Fabric world! its " + AugmentedReborn.MOD_ID);
	}
}