package com.megatrex4.screens;

import com.megatrex4.items.vajraItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public class CustomGrindstoneScreenHandler extends GrindstoneScreenHandler {
    public CustomGrindstoneScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(syncId, playerInventory, context);

        // Modify Slot 0
        Slot originalSlot0 = this.getSlot(0);
        this.slots.set(0, new Slot(originalSlot0.inventory, 0, 49, 19) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return !isVajra(stack) && (stack.isDamageable() || stack.isOf(Items.ENCHANTED_BOOK) || stack.hasEnchantments());
            }
        });

        // Modify Slot 1
        Slot originalSlot1 = this.getSlot(1);
        this.slots.set(1, new Slot(originalSlot1.inventory, 1, 49, 40) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return !isVajra(stack) && (stack.isDamageable() || stack.isOf(Items.ENCHANTED_BOOK) || stack.hasEnchantments());
            }
        });
    }

    private boolean isVajra(ItemStack stack) {
        return stack.getItem() instanceof vajraItem;
    }
}
