package immersive_melodies.network.c2s;

import immersive_melodies.item.InstrumentItem;
import immersive_melodies.network.ImmersivePayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ItemActionMessage(State state, ResourceLocation melody) implements ImmersivePayload {
    public static ItemActionMessage fromStateAndMelody(State state, ResourceLocation melody) {
        return new ItemActionMessage(state, melody);
    }

    public static ItemActionMessage fromState(State state) {
        return new ItemActionMessage(state, new ResourceLocation("empty"));
    }

    public ItemActionMessage(FriendlyByteBuf b) {
        this(b.readEnum(State.class), b.readResourceLocation());
    }

    @Override
    public void encode(FriendlyByteBuf b) {
        b.writeEnum(state);
        b.writeResourceLocation(melody);
    }

    @Override
    public void handle(Player e) {
        e.getHandSlots().forEach(stack -> {
            if (stack.getItem() instanceof InstrumentItem instrument) {
                switch (state) {
                    case PLAY -> instrument.play(stack, melody, e.level(), e);
                    case CONTINUE -> instrument.play(stack, e.level());
                    case PAUSE -> instrument.pause(stack, e.level());
                }
            }
        });
    }

    public enum State {
        PLAY,
        CONTINUE,
        PAUSE
    }
}
