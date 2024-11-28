package com.megatrex4.items;

import com.megatrex4.network.ActivateNanoHelmetPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class KeyBindingRegistry {

    private static final String CATEGORY = "category.augmented_reborn.keys";
    public static KeyBinding toggleNightVisionKey;

    public static void registerKeybindings() {
        toggleNightVisionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.augmented_reborn.toggle_night_vision",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleNightVisionKey.wasPressed() && client.player != null) {
                ItemStack helmet = client.player.getInventory().getArmorStack(3);
                if (helmet.getItem() instanceof ActivatableItem activatable) {
                    boolean newState = !activatable.isActivated(helmet);
                    activatable.toggleActivation(helmet); // Update locally for feedback
                    ActivateNanoHelmetPacket.send(newState); // Send the state to the server

                    // Display feedback to the player
                    Text message = Text.translatable("item.augmented_reborn.night_vision.status")
                            .append(" ")
                            .append(Text.translatable("item.augmented_reborn.night_vision." + (newState ? "enabled" : "disabled"))
                                    .styled(style -> style.withColor(newState ? Formatting.GREEN : Formatting.RED)));
                    client.player.sendMessage(message, true);
                }
            }
        });


    }
}
