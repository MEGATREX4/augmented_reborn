package com.megatrex4.items;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;
import techreborn.config.TechRebornConfig;
import techreborn.items.armor.QuantumSuitItem;

public class QuantumChestplateHUD {

    public static void renderHUD(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player != null && client.world != null) {
            ItemStack chestplateStack = player.getInventory().getArmorStack(2);
            if (chestplateStack.getItem() instanceof QuantumSuitItem) {
                QuantumSuitItem item = (QuantumSuitItem) chestplateStack.getItem();

                boolean isEnabled = chestplateStack.getNbt() != null && chestplateStack.getNbt().getBoolean("Activated");
                String status = isEnabled ? "Enabled" : "Disabled";
                int energy = getEnergyPercentage(chestplateStack);

                TextRenderer fontRenderer = client.textRenderer;
                int x = 5;
                int y = 5;

                VertexConsumerProvider vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();

                int light = 0xF000F0;

                Text statusText = Text.translatable("hud.augmented_reborn.status")
                        .append(": ")
                        .append(Text.translatable("hud.augmented_reborn." + (isEnabled ? "enabled" : "disabled"))
                                .styled(style -> style.withColor(isEnabled ? Formatting.GREEN : Formatting.RED)));

                Text energyText = Text.translatable("hud.augmented_reborn.energy", energy + "%");

                Matrix4f modelMatrix = context.getMatrices().peek().getPositionMatrix();

                fontRenderer.draw(statusText, x, y, 0xFFFFFF, true, modelMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x000000, light);
                fontRenderer.draw(energyText, x, y + 10, 0xFFFFFF, true, modelMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x000000, light);
            }
        }
    }

    private static int getEnergyPercentage(ItemStack stack) {
        if (stack.getItem() instanceof QuantumSuitItem quantumSuitItem) {
            long storedEnergy = quantumSuitItem.getStoredEnergy(stack);
            long maxEnergy = TechRebornConfig.quantumSuitCapacity;
            int percentage = (int) (storedEnergy * 100 / maxEnergy);
            return percentage;
        }
        return 0;
    }
}
