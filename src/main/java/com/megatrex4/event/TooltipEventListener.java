package com.megatrex4.event;

import com.megatrex4.AugmentedReborn;
import com.megatrex4.items.ActivatableItem;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import techreborn.items.armor.NanoSuitItem;
import techreborn.items.armor.QuantumSuitItem;

import java.util.List;

import static com.mojang.text2speech.Narrator.LOGGER;

public class TooltipEventListener {

    public static void register() {
        LOGGER.info("Registering TooltipEventListener for " + AugmentedReborn.MOD_ID);
        ItemTooltipCallback.EVENT.register(TooltipEventListener::addTooltip);
    }

    private static void addTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip) {
        // Add tooltips only for techreborn:nano_helmet and techreborn:quantum_helmet
        if (isTechRebornHelmet(stack, "techreborn:nano_helmet") || isTechRebornHelmet(stack, "techreborn:quantum_helmet")) {
            if (stack.getItem() instanceof ActivatableItem activatable) {
                boolean isActivated = activatable.isActivated(stack);
                tooltip.add(Text.translatable("item.augmented_reborn.night_vision.status")
                        .append(" ")
                        .append(Text.translatable("item.augmented_reborn.night_vision." + (isActivated ? "enabled" : "disabled"))
                                .formatted(isActivated ? Formatting.GREEN : Formatting.RED)));
            } else {
                tooltip.add(Text.translatable("item.augmented_reborn.night_vision.status")
                        .append(" ")
                        .append(Text.translatable("item.augmented_reborn.night_vision.disabled")
                                .formatted(Formatting.RED)));
            }
        }
    }

    private static boolean isTechRebornHelmet(ItemStack stack, String helmetId) {
        // Check if the item's ID matches the given helmet ID
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        return itemId.toString().equals(helmetId);
    }
}
