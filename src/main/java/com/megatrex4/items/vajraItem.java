package com.megatrex4.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import reborncore.common.powerSystem.RcEnergyItem;
import reborncore.common.powerSystem.RcEnergyTier;
import reborncore.common.util.ItemUtils;
import techreborn.init.TRToolMaterials;
import techreborn.items.tool.DrillItem;
import techreborn.items.tool.MiningLevel;

import java.util.List;
import java.util.Map;

public class vajraItem extends DrillItem {


    // Define the fortune level (V for 5)
    public static final int FORTUNE_LEVEL = 5;
    // Constants for modes
    private static final String ENERGY_MODE_KEY = "EnergyMode";
    private static final String[] ENERGY_MODES = {"LOW", "MEDIUM", "INSANE", "FORTUNE", "SILK_TOUCH"};

    private final RcEnergyTier tier;

    public vajraItem(RcEnergyTier tier) {
        super(
                TRToolMaterials.INDUSTRIAL_DRILL, // Tool material
                100_000_000,                      // Max energy capacity
                RcEnergyTier.INSANE,              // Energy tier
                1500,                             // Energy cost per block
                20.0F,                            // Powered mining speed
                0.1F,                             // Unpowered mining speed
                MiningLevel.DIAMOND               // Mining level
        );
        this.tier = tier;
    }

    private static final Map<String, Long> MODE_VALUES = Map.of(
            "LOW", 1000L,
            "MEDIUM", 5000L,
            "INSANE", 10000L,
            "FORTUNE", 15000L,
            "SILK_TOUCH", 15000L
    );

    private static final Map<String, Float> MINING_SPEED_VALUES = Map.of(
            "LOW", 12.0f,
            "MEDIUM", 50.0f,
            "INSANE", 5000.0f,
            "FORTUNE", 5000.0f,
            "SILK_TOUCH", 5000.0f
    );

    public static String getEnergyMode(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.getString(ENERGY_MODE_KEY).isEmpty() ? "MEDIUM" : nbt.getString(ENERGY_MODE_KEY);
    }
    public static void setEnergyMode(ItemStack stack, String mode) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putString(ENERGY_MODE_KEY, mode);

        if ("FORTUNE".equals(mode)) {
            NbtCompound enchantment = new NbtCompound();
            enchantment.putString("id", "minecraft:fortune");
            enchantment.putInt("lvl", FORTUNE_LEVEL);

            NbtList enchantments = new NbtList();
            enchantments.add(enchantment);
            nbt.put("Enchantments", enchantments);

            nbt.put("HideFlags", NbtInt.of(1));
        } else {
            nbt.remove("Enchantments");
            nbt.remove("HideFlags");
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        String energyMode = getEnergyMode(stack);
        return "FORTUNE".equals(energyMode) && false;
    }


    // Helper to cycle energy modes
    public static void cycleEnergyMode(ItemStack stack) {
        String currentMode = getEnergyMode(stack);
        int index = (indexOf(ENERGY_MODES, currentMode) + 1) % ENERGY_MODES.length;
        String newMode = ENERGY_MODES[index];
        setEnergyMode(stack, newMode);
    }


    private static int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) return i;
        }
        return -1;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (player.isSneaking()) {
            cycleEnergyMode(stack); // Shift + Right-Click changes energy mode
            if (!world.isClient) {
                String energyMode = getEnergyMode(stack);
                player.sendMessage(Text.translatable("item.augmented_reborn.vajra.mode_change",
                        Text.translatable("item.augmented_reborn.vajra.mode." + energyMode.toLowerCase())
                                .styled(style -> style.withColor(getModeColor(energyMode)))), true);
            }
        }

        return TypedActionResult.success(stack);
    }


    private Formatting getModeColor(String mode) {
        return switch (mode) {
            case "LOW" -> Formatting.BLUE;
            case "MEDIUM" -> Formatting.GREEN;
            case "INSANE" -> Formatting.RED;
            case "FORTUNE" -> Formatting.GOLD;
            case "SILK_TOUCH" -> Formatting.LIGHT_PURPLE;
            default -> Formatting.GREEN;
        };
    }




    private boolean consumeEnergyForMining(ItemStack stack, PlayerEntity player, long energyRequired) {
        if (!hasSufficientEnergy(stack, energyRequired)) {
            return false;
        }
        tryUseEnergy(stack, energyRequired);
        if (player != null) {
            displayEnergy(player, stack);
        }
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false; // Prevents the item from being enchanted
    }


    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        String energyMode = getEnergyMode(stack);

        long energyRequired = MODE_VALUES.getOrDefault(energyMode, 0L);
        if (!hasSufficientEnergy(stack, energyRequired)) {
            return 0.1f; // Unpowered mining speed
        }

        return MINING_SPEED_VALUES.getOrDefault(energyMode, super.getMiningSpeedMultiplier(stack, state));
    }

    @Override
    public boolean isSuitableFor(BlockState state) {
        return true; // Override to always return true, making the tool effective for all blocks.
    }


    private boolean consumeEnergyForAction(ItemStack stack, PlayerEntity player, long energyRequired) {
        if (!hasSufficientEnergy(stack, energyRequired)) {
            return false; // Insufficient energy
        }
        tryUseEnergy(stack, energyRequired); // Deduct energy
        if (player != null) {
            displayEnergy(player, stack); // Display remaining energy
        }
        return true; // Energy successfully consumed
    }


    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        String energyMode = getEnergyMode(stack);
        String damage = String.valueOf(hasSufficientEnergy(stack, 1500) ? 40.0F : 5.0F);

        // Add tooltip for energy mode with "Mode" in regular color and mode name colored
        tooltip.add(Text.translatable("item.augmented_reborn.vajra.mode")
                .append(" ")
                .append(Text.translatable("item.augmented_reborn.vajra.mode." + energyMode.toLowerCase())
                        .styled(style -> style.withColor(getModeColor(energyMode)))));

        // Add tooltip for damage in red
        tooltip.add(Text.translatable("item.augmented_reborn.vajra.tooltip.damage", damage)
                .styled(style -> style.withColor(Formatting.RED)));

        // Add gray italic tooltip for SILK_TOUCH or FORTUNE mode
        if ("SILK_TOUCH".equals(energyMode)) {
            tooltip.add(Text.translatable("enchantment.minecraft.silk_touch")
                    .styled(style -> style.withColor(Formatting.GRAY).withItalic(true)));
        } else if ("FORTUNE".equals(energyMode)) {
            String fortuneRoman = toRoman(FORTUNE_LEVEL);
            tooltip.add(Text.translatable("enchantment.minecraft.fortune")
                    .append(" ")
                    .append(Text.literal(fortuneRoman))
                    .styled(style -> style.withColor(Formatting.GRAY).withItalic(true)));
        }
    }


    /**
     * Converts an integer to a Roman numeral using a modular system.
     *
     * @param number The integer to convert.
     * @return The Roman numeral as a string.
     */
    private String toRoman(int number) {
        if (number <= 0 || number > 3999) {
            throw new IllegalArgumentException("Number out of range (must be 1-3999)");
        }

        // Roman numeral mapping in modular notation
        String[][] romanModules = {
                {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"},  // Units
                {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"}, // Tens
                {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"}, // Hundreds
                {"", "M", "MM", "MMM"}                                       // Thousands
        };

        StringBuilder roman = new StringBuilder();
        int place = 0;

        // Process number modularly
        while (number > 0) {
            int digit = number % 10;
            roman.insert(0, romanModules[place][digit]); // Insert corresponding Roman numeral part
            number /= 10;
            place++;
        }

        return roman.toString();
    }



    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player) {
            // Get the damage value dynamically
            float damage = getDamage(stack);

            // Use the DamageSource to create a player attack source
            DamageSource playerDamageSource = player.getDamageSources().playerAttack(player);

            // Apply damage to the target
            target.damage(playerDamageSource, damage);

            // Consume energy if the attack is powered
            if (damage > 5.0F) { // Powered damage
                tryUseEnergy(stack, 1500);
            }
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (entityLiving instanceof PlayerEntity player) {
            String energyMode = getEnergyMode(stack);
            long energyRequired = MODE_VALUES.getOrDefault(energyMode, 0L);

            // Consume energy for mining
            if (!consumeEnergyForMining(stack, player, energyRequired)) {
                return false;
            }

            if (!world.isClient && "SILK_TOUCH".equals(energyMode)) {
                // Apply Silk Touch logic
                ItemStack silkTouchDrop = new ItemStack(state.getBlock().asItem());
                Block.dropStack(world, pos, silkTouchDrop);
            }
        }
        return true;
    }


    private void displayEnergy(PlayerEntity player, ItemStack stack) {
        long remainingEnergy = getStoredEnergy(stack);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return false; // Prevents the item from being repaired in a grindstone
    }


    // Energy item methods
    @Override
    public long getEnergyCapacity() {
        return 100_000_000L;
    }

    @Override
    public RcEnergyTier getTier() {
        return this.tier;
    }

    @Override
    public long getEnergyMaxInput() {
        return this.tier.getMaxInput();
    }

    @Override
    public long getEnergyMaxOutput() {
        return this.tier.getMaxOutput();
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return ItemUtils.getPowerForDurabilityBar(stack);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return ItemUtils.getColorForDurabilityBar(stack);
    }

    private boolean hasSufficientEnergy(ItemStack stack, long energyRequired) {
        return getStoredEnergy(stack) >= energyRequired;
    }


    public float getDamage(ItemStack stack) {
        return hasSufficientEnergy(stack, 1500) ? 40.0F : 5.0F;
    }

}
