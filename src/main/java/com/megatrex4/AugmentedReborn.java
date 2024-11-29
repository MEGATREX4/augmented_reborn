package com.megatrex4;

import com.megatrex4.event.TooltipEventListener;
import com.megatrex4.items.ItemGroupRegistry;
import com.megatrex4.items.ItemRegistry;
import com.megatrex4.items.KeyBindingRegistry;
import com.megatrex4.items.vajraItem;
import com.megatrex4.network.ActivateNanoHelmetPacket;
import com.megatrex4.screens.CustomGrindstoneScreenHandler;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AugmentedReborn implements ModInitializer {
	public static final String MOD_ID = "augmented_reborn";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ScreenHandlerType<CustomGrindstoneScreenHandler> CUSTOM_GRINDSTONE_HANDLER;

	@Override
	public void onInitialize() {
		ItemRegistry.ItemRegistry();
		ItemGroupRegistry.ItemGroupRegistry();
		KeyBindingRegistry.registerKeybindings();
		ActivateNanoHelmetPacket.register();

		TooltipEventListener.register();

		CUSTOM_GRINDSTONE_HANDLER = ScreenHandlerRegistry.registerSimple(
				new Identifier(MOD_ID, "custom_grindstone"),
				(syncId, inventory) -> new CustomGrindstoneScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY)
		);

		GrindstoneOverride.register();

		LOGGER.info("Hello Fabric world! its " + AugmentedReborn.MOD_ID);
	}

}