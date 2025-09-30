package com.imoonday.ji_yue_boss.mixin;

import com.imoonday.ji_yue_boss.item.PoltergeistSkillHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin来限制Poltergeist变身时玩家的操作
 */
@Mixin(Player.class)
public class PoltergeistPlayerControlMixin {

    /**
     * 禁止变身时攻击
     */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void preventAttack(net.minecraft.world.entity.Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (PoltergeistSkillHandler.isTransforming(serverPlayer)) {
                ci.cancel();
            }
        }
    }

    /**
     * 禁止变身时丢弃物品
     */
    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", 
            at = @At("HEAD"), cancellable = true)
    private void preventDrop(ItemStack stack, boolean throwRandomly, boolean traceItem, 
                            CallbackInfoReturnable<net.minecraft.world.entity.item.ItemEntity> cir) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (PoltergeistSkillHandler.isTransforming(serverPlayer)) {
                // 把物品放回背包，阻止丢弃
                ((ServerPlayer) player).getInventory().placeItemBackInInventory(stack.copy());
                cir.setReturnValue(null);
            }
        }
    }
}
