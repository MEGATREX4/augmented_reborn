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
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Style;
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
import team.reborn.energy.api.base.SimpleBatteryItem;
import techreborn.init.TRToolMaterials;
import techreborn.items.tool.DrillItem;
import techreborn.items.tool.MiningLevel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class vajraItem extends DrillItem {


    private final RcEnergyTier tier;

    // Constants for modes
    private static final String ENERGY_MODE_KEY = "EnergyMode";
    private static final String[] ENERGY_MODES = {"LOW", "MEDIUM", "INSANE", "FORTUNE", "SILK_TOUCH"};


    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return ImmutableMultimap.of();
        }
        return super.getAttributeModifiers(stack, slot);
    }


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

    public static final long ENERGY_REQUIRED_FOR_ATTACK = 1500L;
    public static final float BIG_DAMAGE = 40.0F;
    public static final float SMALL_DAMAGE = 5.0F;
    public static final int FORTUNE_LEVEL = 5;

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

        if ("SILK_TOUCH".equals(mode)) {
            stack.addEnchantment(Enchantments.SILK_TOUCH, 1);
        } else {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
            enchantments.remove(Enchantments.SILK_TOUCH);
            EnchantmentHelper.set(enchantments, stack);
        }

        if ("FORTUNE".equals(mode)) {
            stack.addEnchantment(Enchantments.FORTUNE, FORTUNE_LEVEL);
        } else {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
            enchantments.remove(Enchantments.FORTUNE);
            EnchantmentHelper.set(enchantments, stack);
        }

        nbt.putInt("HideFlags", 1);
    }



    @Override
    public boolean hasGlint(ItemStack stack) {
        return false;
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
            cycleEnergyMode(stack);
            if (!world.isClient) {
                String energyMode = getEnergyMode(stack);
                player.sendMessage(Text.translatable("item.augmented_reborn.vajra.mode_change",
                        Text.translatable("item.augmented_reborn.vajra.mode." + energyMode.toLowerCase())
                                .styled(style -> style.withColor(getModeColor(energyMode)))), true);
            }
            return TypedActionResult.success(stack);
        } else {
            return TypedActionResult.pass(stack);
        }
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
        return false;
    }


    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        String energyMode = getEnergyMode(stack);

        long energyRequired = MODE_VALUES.getOrDefault(energyMode, 0L);
        if (!hasSufficientEnergy(stack, energyRequired)) {
            return 0.3f;
        }

        return MINING_SPEED_VALUES.getOrDefault(energyMode, super.getMiningSpeedMultiplier(stack, state));
    }

    @Override
    public boolean isSuitableFor(BlockState state) {
        return true;
    }


    private boolean consumeEnergyForAction(ItemStack stack, PlayerEntity player, long energyRequired) {
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
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasEnchantments()) {
            tooltip.removeIf(line -> line.getString().contains("Enchantments"));
        }
        tooltip.clear();

        tooltip.add(Text.translatable("item.augmented_reborn.vajra"));
        String energyMode = getEnergyMode(stack);

        tooltip.add(Text.empty());
        tooltip.add(Text.translatable("item.augmented_reborn.vajra.mode")
                .append(" ")
                .append(Text.translatable("item.augmented_reborn.vajra.mode." + energyMode.toLowerCase())
                        .styled(style -> style.withColor(getModeColor(energyMode)))));

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
        tooltip.add(Text.literal(""));
        tooltip.add(Text.translatable("item.modifiers.mainhand").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        String damage = "none";

        if (hasSufficientEnergy(stack, ENERGY_REQUIRED_FOR_ATTACK)) {
            damage = BIG_DAMAGE + "";
        } else {
            damage = SMALL_DAMAGE + "";
        }

        tooltip.add(Text.literal(" ") // Adds spacing before the attribute modifier
                .append(Text.translatable("attribute.modifier.plus.0", damage,
                                Text.translatable("attribute.name.generic.attack_damage"))
                        .setStyle(Style.EMPTY.withColor(Formatting.DARK_GREEN))));
        tooltip.add(Text.empty());
    }




    /**
     * Converts an integer to a Roman numeral.
     *
     * @param number The integer to convert.
     * @return The Roman numeral as a string.
     */
    private String toRoman(int number) {
        if (number <= 0 || number > 3999) {
            throw new IllegalArgumentException("Number out of range (must be 1-3999)");
        }

        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] units = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

        return thousands[number / 1000] +
               hundreds[(number % 1000) / 100] +
               tens[(number % 100) / 10] +
               units[number % 10];
    }




    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player) {

            float damage = getDamage(stack);

            DamageSource playerDamageSource = player.getDamageSources().playerAttack(player);

            target.damage(playerDamageSource, damage);

            if (damage > SMALL_DAMAGE) {
                tryUseEnergy(stack, ENERGY_REQUIRED_FOR_ATTACK);
            }
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (entityLiving instanceof PlayerEntity player) {
            String energyMode = getEnergyMode(stack);
            long energyRequired = MODE_VALUES.getOrDefault(energyMode, 0L);

            if (!consumeEnergyForMining(stack, player, energyRequired)) {
                return false;
            }
        }
        return super.postMine(stack, world, state, pos, entityLiving);
    }



    private void displayEnergy(PlayerEntity player, ItemStack stack) {
        long remainingEnergy = getStoredEnergy(stack);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return false;
    }


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

    private boolean hasSufficientEnergy(@Nullable ItemStack stack, long energyRequired) {
        if (stack == null || !stack.hasNbt()) {
            return false;
        }

        long storedEnergy = SimpleBatteryItem.getStoredEnergyUnchecked(stack);
        return storedEnergy >= energyRequired;
    }

    public float getDamage(@Nullable ItemStack stack) {
        if (stack == null || !hasSufficientEnergy(stack, ENERGY_REQUIRED_FOR_ATTACK)) {
            return SMALL_DAMAGE;
        }
        return BIG_DAMAGE;
    }

}
