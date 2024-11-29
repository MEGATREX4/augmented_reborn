package com.megatrex4.network;

import com.megatrex4.items.ActivatableItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static com.mojang.text2speech.Narrator.LOGGER;

public class ActivateNanoHelmetPacket {
    public static final Identifier ID = new Identifier("augmented_reborn", "activate_nano_helmet");

    public static void send(boolean activated) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(activated);
//        LOGGER.info("Sending packet to server: [activated=" + activated + "]");
        ClientPlayNetworking.send(ID, buf); // Send the packet to the server
    }


    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            boolean activated = buf.readBoolean();
            server.execute(() -> toggleActivation(player, activated));
        });
    }

    private static void toggleActivation(ServerPlayerEntity player, boolean activated) {
        var helmet = player.getInventory().getArmorStack(3);
        if (helmet.getItem() instanceof ActivatableItem activatable) {
            activatable.setActivated(helmet, activated);
        }
    }
}
