package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.entity.HowlingCelestialDog;
import com.imoonday.ji_yue_boss.init.ModSounds;
import com.imoonday.ji_yue_boss.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.sonmok14.fromtheshadows.server.items.ThirstforBloodItem;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ThirstforBloodItemHandler {

    public static void onUse(ThirstforBloodItem item, Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (user.isShiftKeyDown()) {
            if (!world.isClientSide && world instanceof ServerLevel) {
                ServerLevel server = (ServerLevel) world;
                ItemStack stack = user.getItemInHand(hand);
                long now = server.getGameTime();
                long end = stack.getOrCreateTag().getLong("TFB_SHIFT_CD_END");
                if (end > now) {
                    long sec = Math.max(1, (end - now + 19) / 20);
                    // 与激光提示相同格式，不同名称与颜色（哮天犬，AQUA）
                    user.displayClientMessage(Component.literal("哮天犬冷却中，剩余" + sec + "秒").withStyle(ChatFormatting.AQUA), false);
                    cir.setReturnValue(InteractionResultHolder.fail(stack));
                    return;
                }
            }
            HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(user, EntitySelector.NO_SPECTATORS.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE), 20);
            if (hitResult.getType() == HitResult.Type.ENTITY && hitResult instanceof EntityHitResult) {
                EntityHitResult ehr = (EntityHitResult) hitResult;
                Entity entity = ehr.getEntity();
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    if (world.isClientSide) {
                        cir.setReturnValue(InteractionResultHolder.sidedSuccess(user.getItemInHand(hand), true));
                        return;
                    }
                    HowlingCelestialDog dog = new HowlingCelestialDog(world, user, living);
                    dog.setPos(living.position());
                    Utils.playRandomSound(ModSounds.HOWLING_CELESTIAL_DOG_SOUNDS, world, user);
                    world.addFreshEntity(dog);
                    // SHIFT 技能独立冷却：默认 7 秒，如需调整可改这里
                    ItemStack stack = user.getItemInHand(hand);
                    long now = ((ServerLevel) world).getGameTime();
                    stack.getOrCreateTag().putLong("TFB_SHIFT_CD_END", now + 20L * 7);

                    Utils.addParticlesAround(ParticleTypes.SNOWFLAKE, (ServerLevel) world, dog, 30);
                    dog.playAmbientSound();
                }
            }

            cir.setReturnValue(InteractionResultHolder.sidedSuccess(user.getItemInHand(hand), world.isClientSide));
        }
    }
}
