package com.megatrex4.mixin;

import com.megatrex4.items.ActivatableItem;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techreborn.config.TechRebornConfig;
import techreborn.items.armor.QuantumSuitItem;

@Mixin(QuantumSuitItem.class)
public abstract class QuantumSuitItemMixin {

    @Inject(method = "tickArmor", at = @At("HEAD"), cancellable = true)
    private void tickArmor(ItemStack stack, PlayerEntity playerEntity, CallbackInfo info) {
        QuantumSuitItem item = (QuantumSuitItem) (Object) this;

        switch (item.getSlotType()) {
            case HEAD:
                handleHelmet(stack, playerEntity, item);
                break;
            case CHEST:
                handleChestplate(stack, playerEntity, item);
                break;
            case LEGS:
                handleLeggings(stack, playerEntity, item);
                break;
            case FEET:
                handleBoots(stack, playerEntity, item);
                break;
        }
    }

    private void handleHelmet(ItemStack stack, PlayerEntity player, QuantumSuitItem item) {
        if (player.isSubmergedInWater() && item.tryUseEnergy(stack, TechRebornConfig.quantumSuitBreathingCost)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 5, 1));
        }

        if (stack.getItem() instanceof ActivatableItem activatable && activatable.isActivated(stack)) {
            if (item.tryUseEnergy(stack, TechRebornConfig.nanoSuitNightVisionCost)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 1, false, false));
            } else {
                removeNightVision(player);
            }
        } else {
            removeNightVision(player);
        }
    }

    private void handleChestplate(ItemStack stack, PlayerEntity player, QuantumSuitItem item) {
        if (TechRebornConfig.quantumSuitEnableFlight) {
            if (item.getStoredEnergy(stack) > TechRebornConfig.quantumSuitFlyingCost) {
                player.getAbilities().allowFlying = true;
                player.sendAbilitiesUpdate();
                if (player.getAbilities().flying) {
                    item.tryUseEnergy(stack, TechRebornConfig.quantumSuitFlyingCost);
                }
            } else {
                disableFlight(player);
            }
        }

        if (player.isOnFire() && item.tryUseEnergy(stack, TechRebornConfig.fireExtinguishCost)) {
            player.extinguish();
        }
    }

    private void handleLeggings(ItemStack stack, PlayerEntity player, QuantumSuitItem item) {
        if (player.isSprinting() && TechRebornConfig.quantumSuitEnableSprint) {
            item.tryUseEnergy(stack, TechRebornConfig.quantumSuitSprintingCost);
        }
    }

    private void handleBoots(ItemStack stack, PlayerEntity player, QuantumSuitItem item) {
        if (player.isSwimming() && item.tryUseEnergy(stack, TechRebornConfig.quantumSuitSwimmingCost)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 5, 1, false, false));
        }
    }

    private void removeNightVision(PlayerEntity player) {
        if (player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    private void disableFlight(PlayerEntity player) {
        player.getAbilities().allowFlying = false;
        player.getAbilities().flying = false;
        player.sendAbilitiesUpdate();
    }
}
