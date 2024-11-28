package com.megatrex4.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public interface ActivatableItem {

    default boolean getDefaultActivatedState() {
        return false; // Default is deactivated
    }

    default boolean isActivated(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.getBoolean("Activated");
    }

    default void setActivated(ItemStack stack, boolean activated) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putBoolean("Activated", activated);
    }

    default void toggleActivation(ItemStack stack) {
        setActivated(stack, !isActivated(stack));
    }
}
