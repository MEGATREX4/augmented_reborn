package com.megatrex4.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.megatrex4.items.ActivatableItem;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techreborn.config.TechRebornConfig;
import techreborn.items.armor.QuantumSuitItem;
import techreborn.items.armor.TREnergyArmourItem;

import java.util.UUID;

@Mixin(QuantumSuitItem.class)
public abstract class QuantumSuitItemMixin {


    /**
     * @author
     * @reason
     */
    @Overwrite
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return HashMultimap.create();
    }

    private static final Identifier QUANTUM_ARMOR_FLIGHT_ABILITY_SOURCE_ID = new Identifier("techreborn", "quantum_armor");
    private static final AbilitySource TECHREBORN_QUANTUM_ARMOR_ABILITY_SOURCE = Pal.getAbilitySource(QUANTUM_ARMOR_FLIGHT_ABILITY_SOURCE_ID, AbilitySource.CONSUMABLE);

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
                flightTick(stack, playerEntity, item);
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

    // Correct method signature to match TREnergyArmourItem
    private static void flightTick(ItemStack stack, PlayerEntity player, TREnergyArmourItem item) {
        // Only execute on the server side
        if (!player.getWorld().isClient()) {
            if (stack.getItem() instanceof ActivatableItem activatable && activatable.isActivated(stack)) {
                // Ensure enough energy is available for flight
                if (item.getStoredEnergy(stack) > TechRebornConfig.quantumSuitFlyingCost) {
                    allowFlying(player);
                } else {
                    disableFlying(player);
                }

                if (!hasIndirectFlight(player) && isAllowingFlight(player)) {
                    // Use energy for flight
                    item.tryUseEnergy(stack, TechRebornConfig.quantumSuitFlyingCost);
                }
            } else {
                // Disable flight if config disables it
                disableFlying(player);
            }
        }
    }

    @Inject(method = "onRemoved", at = @At("HEAD"))
    private void onUnequipInject(PlayerEntity playerEntity, CallbackInfo ci) {
        // Capture the armor inventory state before the item is removed
        DefaultedList<ItemStack> previousArmorInventory = playerEntity.getInventory().armor;

        // Check if the chestplate is being removed
        ItemStack chestplate = previousArmorInventory.get(2); // Index 2 is for the chestplate
        if (!(chestplate.getItem() instanceof QuantumSuitItem)) {
            disableFlying(playerEntity);
        }

        // Check if the helmet is being removed
        ItemStack helmet = previousArmorInventory.get(3); // Index 3 is for the helmet
        if (!(helmet.getItem() instanceof QuantumSuitItem)) {
            removeNightVision(playerEntity);
        }
    }


    private static boolean isAllowingFlight(PlayerEntity player) {
        return TECHREBORN_QUANTUM_ARMOR_ABILITY_SOURCE.grants(player, VanillaAbilities.ALLOW_FLYING) &&
                TECHREBORN_QUANTUM_ARMOR_ABILITY_SOURCE.isActivelyGranting(player, VanillaAbilities.ALLOW_FLYING);
    }

    // Enable flight for the player
    private static void allowFlying(PlayerEntity playerEntity) {
        if (!playerEntity.getWorld().isClient()) {
            TECHREBORN_QUANTUM_ARMOR_ABILITY_SOURCE.grantTo(playerEntity, VanillaAbilities.ALLOW_FLYING);
            playerEntity.setOnGround(true);
        }
    }

    // Disable flight for the player
    private static void disableFlying(PlayerEntity playerEntity) {
        if (!playerEntity.getWorld().isClient()) {
            // PAL revokes flight ability from the player
            TECHREBORN_QUANTUM_ARMOR_ABILITY_SOURCE.revokeFrom(playerEntity, VanillaAbilities.ALLOW_FLYING);
        }
    }

    private static boolean hasIndirectFlight(PlayerEntity player) {
        return VanillaAbilities.CREATIVE_MODE.isEnabledFor(player) || player.isCreative() || player.isSpectator();
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
                            1.3,
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
}
