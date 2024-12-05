package com.megatrex4.items;

import com.megatrex4.network.ActivatableItemPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class KeyBindingFlyRegistry {

    public static KeyBinding toggleFlightKey;
    private static final String CATEGORY = "category.augmented_reborn.keys";

    public static void registerKeybindings() {
        toggleFlightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.augmented_reborn.toggle_flight",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleFlightKey.wasPressed() && client.player != null) {
                ItemStack chestItem = client.player.getInventory().getArmorStack(2);
                if (chestItem.getItem() instanceof ActivatableItem activatable) {
                    boolean newState = !activatable.isActivated(chestItem);
                    activatable.toggleActivation(chestItem);
                    ActivatableItemPacket.send(newState);

                    Text message = Text.translatable("item.augmented_reborn.flight.status")
                            .append(" ")
                            .append(Text.translatable("hud.augmented_reborn." + (newState ? "enabled" : "disabled"))
                                    .styled(style -> style.withColor(newState ? Formatting.GREEN : Formatting.RED)));
                    client.player.sendMessage(message, true);

                }
            }
        });
    }
}
