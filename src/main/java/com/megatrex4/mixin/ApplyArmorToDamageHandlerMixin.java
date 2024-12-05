package com.megatrex4.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import techreborn.events.ApplyArmorToDamageHandler;
import techreborn.items.armor.QuantumSuitItem;

import java.util.logging.Logger;

import static com.megatrex4.AugmentedReborn.LOGGER;

@Mixin(ApplyArmorToDamageHandler.class)
public class ApplyArmorToDamageHandlerMixin {
    private static final double DAMAGE_ENERGY_MULTIPLIER = 5000.0;  // Energy cost multiplier for damage

    /**
     * Overwrites the applyArmorToDamage method to absorb 25% of the damage per armor piece.
     * Damage is absorbed only if the QuantumSuitItem has energy; if not, the damage is dealt to the player.
     */
    @Overwrite
    public float applyArmorToDamage(PlayerEntity player, DamageSource source, float amount) {
        float totalDamageAbsorbed = 0.0f;
        int armorPiecesWithEnergy = 0;

        for (ItemStack stack : player.getArmorItems()) {
            Item item = stack.getItem();

            if (item instanceof QuantumSuitItem quantumSuitItem) {
                double stackEnergy = (double) quantumSuitItem.getStoredEnergy(stack);

                if (stackEnergy > 0.0) {
                    armorPiecesWithEnergy++;
                    double energyRequired = amount * 0.25 * DAMAGE_ENERGY_MULTIPLIER;

                    quantumSuitItem.tryUseEnergy(stack, (long) energyRequired);

                    totalDamageAbsorbed += (float) (amount * 0.25);
                }
            }
        }



            if (armorPiecesWithEnergy > 0) {
                // Log the absorbed damage
                //LOGGER.info("Absorbed damage: " + totalDamageAbsorbed + " from " + armorPiecesWithEnergy + " armor pieces.");

                // Return the remaining damage after absorption
                return amount - totalDamageAbsorbed;
            }

            return amount;

    }
}
