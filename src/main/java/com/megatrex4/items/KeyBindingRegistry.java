package com.megatrex4.items;

import com.megatrex4.util.NightVisionHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class KeyBindingRegistry {

    private static final String CATEGORY = "category.augmented_reborn.keys";
    public static KeyBinding toggleNightVisionKey;

    public static void registerKeybindings() {
        // Register the keybinding
        toggleNightVisionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.augmented_reborn.toggle_night_vision",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N, // Press 'N' to toggle
                CATEGORY
        ));

        // Attach event handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleNightVisionKey.wasPressed() && client.player != null) {
                NightVisionHandler.toggleNightVision(); // Toggle the night vision state

                boolean isEnabled = NightVisionHandler.isNightVisionEnabled();
                Text message = Text.translatable("item.augmented_reborn.nanosuit.night_vision.status")
                        .append(" ")
                        .append(Text.translatable("item.augmented_reborn.nanosuit.night_vision." + (isEnabled ? "enabled" : "disabled"))
                                .styled(style -> style.withColor(isEnabled ? Formatting.GREEN : Formatting.RED)));

                client.player.sendMessage(message, true);
            }
        });
    }
}
