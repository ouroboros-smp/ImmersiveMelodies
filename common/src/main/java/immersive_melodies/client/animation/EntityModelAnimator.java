package immersive_melodies.client.animation;

import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.MelodyProgressManager;
import immersive_melodies.client.animation.accessors.ModelAccessor;
import immersive_melodies.item.InstrumentItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EntityModelAnimator {
    public static Item getInstrument(Entity entity) {
        for (ItemStack handItem : entity.getHandSlots()) {
            if (handItem.getItem() instanceof InstrumentItem instrument && instrument.isPlaying(handItem)) {
                return handItem.getItem();
            }
        }

        for (ItemStack handItem : entity.getHandSlots()) {
            if (handItem.getItem() instanceof InstrumentItem) {
                return handItem.getItem();
            }
        }

        return null;
    }

    private static boolean isInMainHand(LivingEntity entity) {
        return entity.getMainHandItem().getItem() instanceof InstrumentItem;
    }

    @Deprecated
    public static ModelPart getLeftArm(HumanoidModel<?> model, LivingEntity entity) {
        return isInMainHand(entity) ? model.leftArm : model.rightArm;
    }

    @Deprecated
    public static ModelPart getRightArm(HumanoidModel<?> model, LivingEntity entity) {
        return isInMainHand(entity) ? model.rightArm : model.leftArm;
    }

    public static <T extends Entity> void setAngles(ModelAccessor<T> accessor) {
        T entity = accessor.getEntity();
        Item item = getInstrument(entity);
        if (item != null) {
            float time = (Minecraft.getInstance().isPaused() ? 0.0f : Minecraft.getInstance().getFrameTime()) + entity.tickCount;

            MelodyProgress progress = MelodyProgressManager.INSTANCE.getProgress(entity);
            progress.visualTick(time);

            // Apply animations
            ItemAnimators.get(BuiltInRegistries.ITEM.getKey(item)).setAngles(accessor, progress, time);
        }
    }
}
