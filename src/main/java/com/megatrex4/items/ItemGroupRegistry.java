package com.megatrex4.items;

import com.megatrex4.AugmentedReborn;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemGroupRegistry {

    public static final ItemGroup AUGMENTED_REBORN = Registry.register(Registries.ITEM_GROUP,
            new Identifier(AugmentedReborn.MOD_ID, "augmented_reborn"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("group.augmented_reborn.title"))
                    .icon(() -> {
                        // Set the icon as a fully charged Vajra
                        ItemStack vajraStack = new ItemStack(ItemRegistry.VAJRA);
                        vajraStack.getOrCreateNbt().putLong("energy", 100_000_000L); // Maximum energy capacity
                        return vajraStack;
                    })
                    .entries((displayContext, itemEntries) -> {
                        // Add the uncharged Vajra
                        itemEntries.add(ItemRegistry.VAJRA);

                        // Add the fully charged Vajra to the tab
                        ItemStack fullyChargedVajra = new ItemStack(ItemRegistry.VAJRA);
                        fullyChargedVajra.getOrCreateNbt().putLong("energy", 100_000_000L); // Maximum energy capacity
                        itemEntries.add(fullyChargedVajra);
                    })
                    .build()
    );




    public static void ItemGroupRegistry(){
        AugmentedReborn.LOGGER.info("Registering Group for " + AugmentedReborn.MOD_ID);
    }

}
