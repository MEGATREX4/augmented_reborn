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

        // Check if the player is fully logged in
        if (player != null && client.world != null) {
            ItemStack chestplateStack = player.getInventory().getArmorStack(2); // Get the chestplate
            if (chestplateStack.getItem() instanceof QuantumSuitItem) {
                QuantumSuitItem item = (QuantumSuitItem) chestplateStack.getItem(); // Cast the item to QuantumSuitItem

                boolean isEnabled = chestplateStack.getNbt() != null && chestplateStack.getNbt().getBoolean("Activated");
                String status = isEnabled ? "Enabled" : "Disabled";
                int energy = getEnergyPercentage(chestplateStack); // Use the chestplate stack to get energy percentage

                // Set up the font renderer
                TextRenderer fontRenderer = client.textRenderer;
                int x = 5; // X position for the text
                int y = 5; // Y position for the text

                // Get the vertex consumer provider for rendering
                VertexConsumerProvider vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();

                // Light value (default light level for rendering)
                int light = 0xF000F0;

                // Prepare the two lines of text
                Text statusText = Text.translatable("hud.augmented_reborn.status")
                        .append(": ")
                        .append(Text.translatable("hud.augmented_reborn." + (isEnabled ? "enabled" : "disabled"))
                                .styled(style -> style.withColor(isEnabled ? Formatting.GREEN : Formatting.RED)));

                Text energyText = Text.translatable("hud.augmented_reborn.energy", energy + "%");

                // Get the model matrix
                Matrix4f modelMatrix = context.getMatrices().peek().getPositionMatrix();

                // Render the text on the HUD in two lines
                fontRenderer.draw(statusText, x, y, 0xFFFFFF, false, modelMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x000000, light);
                fontRenderer.draw(energyText, x, y + 10, 0xFFFFFF, false, modelMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x000000, light);
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
