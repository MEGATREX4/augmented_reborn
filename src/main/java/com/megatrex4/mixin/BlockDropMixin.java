package com.megatrex4.mixin;

import com.megatrex4.items.vajraItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(Block.class)
public abstract class BlockDropMixin {

    @Inject(method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void suppressNaturalDrops(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
        if (shouldOverrideDrops(tool)) {
            ci.cancel();
        }
    }

    private static boolean shouldOverrideDrops(ItemStack tool) {
        // Check if the tool is a vajraItem and in SILK_TOUCH mode
        if (tool.getItem() instanceof vajraItem) {
            String mode = vajraItem.getEnergyMode(tool);
            return "SILK_TOUCH".equals(mode);
        }
        return false;
    }

}


