package com.megatrex4.items;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import techreborn.items.armor.NanoSuitItem;

public class ActivatableNanoHelmet extends NanoSuitItem implements ActivatableItem {
    public ActivatableNanoHelmet(ArmorMaterial material, ArmorItem.Type type, Settings settings) {
        super(material, type); // Pass the required arguments to the superclass constructor
    }

    @Override
    public boolean isActivated(ItemStack stack) {
        return ActivatableItem.super.isActivated(stack);
    }

    @Override
    public void setActivated(ItemStack stack, boolean activated) {
        ActivatableItem.super.setActivated(stack, activated);
    }

    @Override
    public void toggleActivation(ItemStack stack) {
        ActivatableItem.super.toggleActivation(stack);
    }
}
