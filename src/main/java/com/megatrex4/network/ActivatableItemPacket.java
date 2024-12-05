package com.megatrex4.network;

import com.megatrex4.items.ActivatableItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ActivatableItemPacket {

    public static final Identifier ID = new Identifier("augmented_reborn", "activatable_item_toggle");

    // Sends a packet to the server to toggle activation
    public static void send(boolean activated) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(activated);
        ClientPlayNetworking.send(ID, buf); // Send the packet to the server
    }

    // Registers the packet handler on the server side
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            boolean activated = buf.readBoolean();
            server.execute(() -> handleActivationToggle(player, activated));
        });
    }

    // Handles the activation toggle logic on the server side
    private static void handleActivationToggle(ServerPlayerEntity player, boolean activated) {
        ItemStack chestItem = player.getInventory().getArmorStack(2); // Assuming chest slot for flight
        if (chestItem.getItem() instanceof ActivatableItem activatable) {
            activatable.setActivated(chestItem, activated);
        }
    }
}

