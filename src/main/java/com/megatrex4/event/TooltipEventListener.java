package com.megatrex4.event;

import com.megatrex4.AugmentedReborn;
import com.megatrex4.items.ActivatableItem;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
        // Check if the item is an instance of ActivatableItem or its specific class
        if (stack.getItem() instanceof ActivatableItem activatable && isHelmet(stack)) {
            boolean isActivated = activatable.isActivated(stack);
            tooltip.add(Text.translatable("item.augmented_reborn.night_vision.status")
                    .append(" ")
                    .append(Text.translatable("item.augmented_reborn.night_vision." + (isActivated ? "enabled" : "disabled"))
                            .formatted(isActivated ? Formatting.GREEN : Formatting.RED)));
        } else if (stack.getItem() instanceof NanoSuitItem || stack.getItem() instanceof QuantumSuitItem) {
            tooltip.add(Text.translatable("item.augmented_reborn.night_vision.status")
                    .append(" ")
                    .append(Text.translatable("item.augmented_reborn.night_vision.disabled")
                            .formatted(Formatting.RED))); // Default status
        }
    }

    private static boolean isHelmet(ItemStack stack) {
        // Check if the item is an ArmorItem and is for the head slot
        return stack.getItem() instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.HEAD;
    }
}
