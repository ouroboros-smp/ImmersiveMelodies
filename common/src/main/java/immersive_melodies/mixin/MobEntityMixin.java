package immersive_melodies.mixin;

import immersive_melodies.Config;
import immersive_melodies.item.InstrumentItem;
import immersive_melodies.util.EntityEquiper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends LivingEntity {
    @Shadow
    protected abstract Vec3i getPickupReach();

    @Shadow
    protected abstract void pickUpItem(ItemEntity item);

    @Shadow
    @Final
    private NonNullList<ItemStack> handItems;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void immersiveMelodies$injectInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!player.hasPermissions(Config.getInstance().rightClickToDropEntityInstrumentPermissionLevel)) {
            return;
        }

        for (ItemStack handItem : this.handItems) {
            if (handItem.getItem() instanceof InstrumentItem) {
                ItemEntity itemEntity = spawnAtLocation(handItem.copyAndClear());
                if (itemEntity != null) {
                    itemEntity.setThrower(getUUID());
                }
                cir.setReturnValue(InteractionResult.CONSUME);
                break;
            }
        }
    }

    @Inject(method = "baseTick()V", at = @At("TAIL"))
    private void immersiveMelodies$injectBaseTick(CallbackInfo ci) {
        if (Config.getInstance().forceMobsToPickUp && EntityEquiper.canPickUp(this) && !this.level().isClientSide && this.isAlive() && !this.dead) {
            Vec3i vec3i = this.getPickupReach();
            for (ItemEntity itementity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(vec3i.getX(), vec3i.getY(), vec3i.getZ()))) {
                if ((itementity.getOwner() == null || !itementity.getOwner().getUUID().equals(getUUID())) && !itementity.isRemoved() && !itementity.getItem().isEmpty() && itementity.getItem().getItem() instanceof InstrumentItem) {
                    pickUpItem(itementity);
                }
            }
        }
    }

    @Inject(method = "canReplaceCurrentItem", at = @At("HEAD"), cancellable = true)
    private void immersiveMelodies$injectCanReplaceCurrentItem(ItemStack newStack, ItemStack oldStack, CallbackInfoReturnable<Boolean> cir) {
        if (newStack.getItem() instanceof InstrumentItem && !(oldStack.getItem() instanceof InstrumentItem)) {
            cir.setReturnValue(true);
        } else if (oldStack.getItem() instanceof InstrumentItem) {
            cir.setReturnValue(false);
        }
    }
}
