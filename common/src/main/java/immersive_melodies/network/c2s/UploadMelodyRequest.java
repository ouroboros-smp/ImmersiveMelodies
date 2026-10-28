package immersive_melodies.network.c2s;

import immersive_melodies.Config;
import immersive_melodies.network.FragmentedMessage;
import immersive_melodies.network.Network;
import immersive_melodies.network.PacketSplitter;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.ServerMelodyManager;
import immersive_melodies.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record UploadMelodyRequest(String name, byte[] fragment, int length) implements FragmentedMessage {
    public UploadMelodyRequest(FriendlyByteBuf b) {
        this(
                b.readUtf(),
                b.readByteArray(),
                b.readVarInt()
        );
    }

    @Override
    public void finish(Player e, String name, Melody melody) {
        if (!e.hasPermissions(Config.getInstance().uploadPermissionLevel)) {
            e.sendSystemMessage(Component.translatable("immersive_melodies.error.upload.no_permission"));
            return;
        }
        String id = Utils.getPlayerName(e) + "/" + UUID.randomUUID();
        ResourceLocation identifier = new ResourceLocation("player", id);

        // Register
        ServerMelodyManager.registerMelody(
                identifier,
                melody
        );

        // Update the index
        Network.sendToPlayer(new MelodyListMessage(e), (ServerPlayer) e);

        // Send the melody to all players
        e.level().players().forEach(player -> {
            PacketSplitter.sendToPlayer(identifier, melody, (ServerPlayer) player);
        });
    }
}
