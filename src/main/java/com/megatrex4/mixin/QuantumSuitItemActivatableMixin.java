package com.megatrex4.mixin;

import com.megatrex4.items.ActivatableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import techreborn.items.armor.QuantumSuitItem;

@Mixin(QuantumSuitItem.class)
public abstract class QuantumSuitItemActivatableMixin implements ActivatableItem {

    @Override
    public boolean isActivated(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.getBoolean("Activated");
    }

    @Override
    public void setActivated(ItemStack stack, boolean activated) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putBoolean("Activated", activated);
    }

    @Override
    public void toggleActivation(ItemStack stack) {
        setActivated(stack, !isActivated(stack));
    }
}
