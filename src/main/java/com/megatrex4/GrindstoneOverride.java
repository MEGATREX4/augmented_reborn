package com.megatrex4;

import com.megatrex4.screens.CustomGrindstoneScreenHandler;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import static com.megatrex4.AugmentedReborn.LOGGER;

public class GrindstoneOverride {
    public static void register() {
        LOGGER.info("Registering GrindstoneOverride for " + AugmentedReborn.MOD_ID);
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient && hand == Hand.MAIN_HAND) {
                if (world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.GRINDSTONE)) {
                    openCustomGrindstone(player, hitResult.getBlockPos());
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
    }

    private static void openCustomGrindstone(PlayerEntity player, BlockPos pos) {
        player.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new CustomGrindstoneScreenHandler(syncId, inv, ScreenHandlerContext.create(player.getWorld(), pos));
            }

            @Override
            public Text getDisplayName() {
                return Text.translatable("container.grindstone_title");
            }
        });
    }
}
