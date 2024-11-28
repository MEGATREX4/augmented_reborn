package com.megatrex4.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import techreborn.items.armor.NanoSuitItem;
import techreborn.items.armor.QuantumSuitItem;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    private static final long MAX_ENERGY_COST = 10_000L;
    private static final long NANO_ENERGY_MULTIPLIER = 100L;
    private static final long QUANTUM_ENERGY_MULTIPLIER = 1000L;

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void preventKineticAndFallDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Handle kinetic damage (FLY_INTO_WALL)
        if (source.isOf(DamageTypes.FLY_INTO_WALL)) {
            ItemStack helmet = player.getInventory().getArmorStack(3); // Helmet slot
            if (helmet.getItem() instanceof NanoSuitItem nanoHelmet) {
                handleDamage(helmet, nanoHelmet, amount, NANO_ENERGY_MULTIPLIER, info);
            } else if (helmet.getItem() instanceof QuantumSuitItem quantumHelmet) {
                handleDamage(helmet, quantumHelmet, amount, QUANTUM_ENERGY_MULTIPLIER, info);
            }
        }

        // Handle fall damage
        if (source.isOf(DamageTypes.FALL)) {
            ItemStack boots = player.getInventory().getArmorStack(0); // Boots slot
            if (boots.getItem() instanceof NanoSuitItem nanoBoots) {
                handleDamage(boots, nanoBoots, amount, NANO_ENERGY_MULTIPLIER, info);
            } else if (boots.getItem() instanceof QuantumSuitItem quantumBoots) {
                handleDamage(boots, quantumBoots, amount, QUANTUM_ENERGY_MULTIPLIER, info);
            }
        }
    }

    private void handleDamage(ItemStack armorPiece, Object armorItem, float amount, long multiplier, CallbackInfoReturnable<Boolean> info) {
        long energyCost = Math.min((long) (amount * multiplier), MAX_ENERGY_COST);

        if (armorItem instanceof NanoSuitItem nanoItem) {
            if (nanoItem.getStoredEnergy(armorPiece) >= energyCost && nanoItem.tryUseEnergy(armorPiece, energyCost)) {
                info.setReturnValue(false); // Cancel the damage
            }
        } else if (armorItem instanceof QuantumSuitItem quantumItem) {
            if (quantumItem.getStoredEnergy(armorPiece) >= energyCost && quantumItem.tryUseEnergy(armorPiece, energyCost)) {
                info.setReturnValue(false); // Cancel the damage
            }
        }
    }
}
