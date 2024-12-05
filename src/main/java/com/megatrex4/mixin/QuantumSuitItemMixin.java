package com.megatrex4.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.megatrex4.items.ActivatableItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techreborn.config.TechRebornConfig;
import techreborn.items.armor.QuantumSuitItem;

import java.util.UUID;

@Mixin(QuantumSuitItem.class)
public abstract class QuantumSuitItemMixin {


    /**
     * @author
     * @reason
     */
    @Overwrite
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        // Return an empty HashMultimap to override all default attributes.
        return HashMultimap.create();
    }


    private static final UUID LEGGINGS_SPEED_BOOST_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"); // Unique ID for the speed boost modifier

    /**
     * @author MEGATREX4
     * @reason fixing bugs on modpack
     */
    @Overwrite
    public void tickArmor(ItemStack stack, PlayerEntity playerEntity) {
        QuantumSuitItem item = (QuantumSuitItem) (Object) this;

        switch (item.getSlotType()) {
            case HEAD:
                handleHelmet(stack, playerEntity, item);
                break;
            case CHEST:
                enableFlight(stack, playerEntity, item);
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

    private void enableFlight(ItemStack stack, PlayerEntity player, QuantumSuitItem item) {
        if (stack.getItem() instanceof ActivatableItem activatable && activatable.isActivated(stack)) {
            ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);

            boolean isAlreadyFlying = player.getAbilities().allowFlying;

            if (item.getStoredEnergy(stack) > TechRebornConfig.quantumSuitFlyingCost) {
                if (!isAlreadyFlying) {
                    player.getAbilities().allowFlying = true;
                    player.sendAbilitiesUpdate();
                }

                if (player.getAbilities().flying) {
                    item.tryUseEnergy(chestItem, TechRebornConfig.quantumSuitFlyingCost);
                }
            } else {
                disableFlight(player);
            }

            if (player.isOnFire() && item.tryUseEnergy(stack, TechRebornConfig.fireExtinguishCost)) {
                player.extinguish();
            }
        } else {
            disableFlight(player);
        }
    }


    private void handleLeggings(ItemStack stack, PlayerEntity player, QuantumSuitItem item) {

        if (player.isSprinting()) {
            if (item.tryUseEnergy(stack, TechRebornConfig.quantumSuitSprintingCost)) {
                if (player.getAttributes()
                        .getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                        .getModifier(LEGGINGS_SPEED_BOOST_UUID) == null) {
                    EntityAttributeModifier speedBoostModifier = new EntityAttributeModifier(
                            LEGGINGS_SPEED_BOOST_UUID,
                            "Quantum Leggings Speed Boost",
                            1.5,
                            EntityAttributeModifier.Operation.MULTIPLY_BASE
                    );
                    player.getAttributes()
                            .getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                            .addPersistentModifier(speedBoostModifier);
                }
            }
        } else {
            // Remove the speed boost modifier if it exists
            EntityAttributeModifier modifier = player.getAttributes()
                    .getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                    .getModifier(LEGGINGS_SPEED_BOOST_UUID);
            if (modifier != null) {
                player.getAttributes()
                        .getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                        .removeModifier(modifier);
            }
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
        // Get the player's abilities through the public method
        PlayerAbilities abilities = player.getAbilities();

        // Disable flight if the player is not in creative mode and flight is enabled by the Quantum Suit
        if (!player.isCreative() && abilities.allowFlying) {
            abilities.flying = false;
            abilities.allowFlying = false;
            player.sendAbilitiesUpdate();  // Update the player's flight state
        }
    }

}
