package com.megatrex4;

import com.megatrex4.event.TooltipEventListener;
import com.megatrex4.items.KeyBindingFlyRegistry;
import com.megatrex4.items.KeyBindingRegistry;
import com.megatrex4.items.QuantumChestplateHUD;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class AugmentedRebornClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingRegistry.registerKeybindings();
        KeyBindingFlyRegistry.registerKeybindings();
        TooltipEventListener.register();

        // Register the HUD rendering callback for rendering custom HUD elements
        HudRenderCallback.EVENT.register(QuantumChestplateHUD::renderHUD);
    }
}
