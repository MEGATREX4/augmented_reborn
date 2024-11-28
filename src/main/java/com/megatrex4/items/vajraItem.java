package com.megatrex4.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
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
import net.minecraft.nbt.NbtList;
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

public class vajraItem extends DrillItem {


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

    // Helper to get current energy mode
    public static String getEnergyMode(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.getString(ENERGY_MODE_KEY).isEmpty() ? "MEDIUM" : nbt.getString(ENERGY_MODE_KEY);
    }

    // Helper to set energy mode
    public static void setEnergyMode(ItemStack stack, String mode) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putString(ENERGY_MODE_KEY, mode);
    }

    // Helper to cycle energy modes
    public static void cycleEnergyMode(ItemStack stack) {
        String currentMode = getEnergyMode(stack);
        int index = (indexOf(ENERGY_MODES, currentMode) + 1) % ENERGY_MODES.length;
        String newMode = ENERGY_MODES[index];
        setEnergyMode(stack, newMode);
        updateEnchantmentForMode(stack, newMode); // Update enchantments immediately
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

        if (!hasSufficientEnergy(stack, switch (energyMode) {
            case "LOW" -> 1000L;
            case "MEDIUM" -> 5000L;
            case "INSANE" -> 15000L;
            case "FORTUNE", "SILK_TOUCH" -> 30000L;
            default -> 0L;
        })) {
            return 0.1f;
        }

        return switch (energyMode) {
            case "LOW" -> 10.0f;
            case "MEDIUM" -> 50.0f;
            case "INSANE", "FORTUNE", "SILK_TOUCH" -> 500.0f;
            default -> super.getMiningSpeedMultiplier(stack, state);
        };
    }


    private static void updateEnchantmentForMode(ItemStack stack, String mode) {
        switch (mode) {
            case "FORTUNE" -> {
                removeEnchantment(stack, Enchantments.SILK_TOUCH);
                applyEnchantment(stack, Enchantments.FORTUNE, 5);
            }
            case "SILK_TOUCH" -> {
                removeEnchantment(stack, Enchantments.FORTUNE);
                applyEnchantment(stack, Enchantments.SILK_TOUCH, 1);
            }
            default -> {
                removeEnchantment(stack, Enchantments.FORTUNE);
                removeEnchantment(stack, Enchantments.SILK_TOUCH);
            }
        }
    }


    // Helper to remove a specific enchantment
    private static void removeEnchantment(ItemStack stack, Enchantment enchantment) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (nbt.contains("Enchantments")) {
            NbtList enchantments = nbt.getList("Enchantments", 10);

            for (int i = 0; i < enchantments.size(); i++) {
                NbtCompound existingEnchantment = enchantments.getCompound(i);
                if (existingEnchantment.getString("id").equals(EnchantmentHelper.getEnchantmentId(enchantment).toString())) {
                    enchantments.remove(i);
                    break;
                }
            }

            nbt.put("Enchantments", enchantments);
        }
    }



    private static void applyEnchantment(ItemStack stack, Enchantment enchantment, int level) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList enchantments = nbt.getList("Enchantments", 10);

        for (int i = 0; i < enchantments.size(); i++) {
            NbtCompound existingEnchantment = enchantments.getCompound(i);
            if (existingEnchantment.getString("id").equals(EnchantmentHelper.getEnchantmentId(enchantment).toString())) {
                return; // Enchantment already exists, do nothing
            }
        }

        NbtCompound enchantmentNbt = new NbtCompound();
        enchantmentNbt.putString("id", EnchantmentHelper.getEnchantmentId(enchantment).toString());
        enchantmentNbt.putInt("lvl", level);
        enchantments.add(enchantmentNbt);
        nbt.put("Enchantments", enchantments);
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
            long energyUsed = switch (getEnergyMode(stack)) {
                case "LOW" -> 1000L;
                case "MEDIUM" -> 5000L;
                case "INSANE" -> 15000L;
                case "FORTUNE", "SILK_TOUCH" -> 30000L;
                default -> 0L;
            };

            if (!consumeEnergyForMining(stack, player, energyUsed)) {
                return false;
            }
        }
        return super.postMine(stack, world, state, pos, entityLiving);
    }

    private void displayEnergy(PlayerEntity player, ItemStack stack) {
        long remainingEnergy = getStoredEnergy(stack);
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
