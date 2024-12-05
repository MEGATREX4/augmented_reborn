package com.megatrex4;

import com.megatrex4.event.TooltipEventListener;
import com.megatrex4.items.KeyBindingFlyRegistry;
import com.megatrex4.items.KeyBindingRegistry;
import net.fabricmc.api.ClientModInitializer;

public class AugmentedRebornClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingRegistry.registerKeybindings();
        KeyBindingFlyRegistry.registerKeybindings();
        TooltipEventListener.register();
    }
}
