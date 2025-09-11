package com.imoonday.ji_yue_boss.mixin;

import com.imoonday.ji_yue_boss.data.CharacterData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;inventoryTick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V"), cancellable = true)
    private void inventoryTick(Level pLevel, Entity pEntity, int pInventorySlot, boolean pIsCurrentItem, CallbackInfo ci) {
        if (pLevel instanceof ServerLevel level && pEntity instanceof Player player) {
            if (player.getAbilities().instabuild) return;

            CharacterData data = CharacterData.fromServer(level.getServer());
            ItemStack stack = (ItemStack) (Object) this;
            if (!stack.isEmpty() && data.isInvalidOwner(player, stack)) {
                player.getInventory().setItem(pInventorySlot, ItemStack.EMPTY);
                ci.cancel();
            }
        }
    }
}
