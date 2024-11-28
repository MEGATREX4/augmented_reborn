package com.megatrex4.items;

import com.megatrex4.AugmentedReborn;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import reborncore.common.powerSystem.RcEnergyTier;

public class ItemRegistry {

    public static final Item VAJRA = registerItems("vajra", new vajraItem(RcEnergyTier.INSANE));

    public static Item registerItems(String name, Item item){
        return Registry.register(Registries.ITEM, new Identifier(AugmentedReborn.MOD_ID, name.toLowerCase()), item
        );
    }

    public static void ItemRegistry() {
        AugmentedReborn.LOGGER.info("Registering Items for " + AugmentedReborn.MOD_ID);
    }

}
