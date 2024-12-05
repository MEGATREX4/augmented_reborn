package com.megatrex4.items;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import techreborn.config.TechRebornConfig;
import techreborn.items.armor.QuantumSuitItem;

public class QuantumChestplateHUD {

    public static void renderHUD(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player != null && client.world != null) {
            // Get the chestplate item
            ItemStack chestplateStack = player.getInventory().getArmorStack(2);
            if (chestplateStack.getItem() instanceof QuantumSuitItem) {
                QuantumSuitItem item = (QuantumSuitItem) chestplateStack.getItem();

                boolean isEnabled = chestplateStack.getNbt() != null && chestplateStack.getNbt().getBoolean("Activated");
                int energy = getEnergyPercentage(chestplateStack);

                Text statusText = Text.translatable("hud.augmented_reborn.status")
                        .append(": ")
                        .append(Text.translatable("hud.augmented_reborn." + (isEnabled ? "enabled" : "disabled"))
                                .styled(style -> style.withColor(isEnabled ? Formatting.GREEN : Formatting.RED)));

                Text energyText = Text.translatable("hud.augmented_reborn.energy", energy + "%");

                TextRenderer fontRenderer = client.textRenderer;
                int x = 5;
                int y = 5;

                context.drawText(fontRenderer, statusText, x, y, 0xFFFFFF, true);
                context.drawText(fontRenderer, energyText, x, y + 10, 0xFFFFFF, true);
            }
        }
    }

    private static int getEnergyPercentage(ItemStack stack) {
        if (stack.getItem() instanceof QuantumSuitItem quantumSuitItem) {
            long storedEnergy = quantumSuitItem.getStoredEnergy(stack);
            long maxEnergy = TechRebornConfig.quantumSuitCapacity;
            return (int) (storedEnergy * 100 / maxEnergy);
        }
        return 0;
    }
}
