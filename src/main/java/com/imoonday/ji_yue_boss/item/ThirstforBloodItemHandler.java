package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.entity.HowlingCelestialDog;
import com.imoonday.ji_yue_boss.init.ModSounds;
import com.imoonday.ji_yue_boss.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
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
            HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(user, EntitySelector.NO_SPECTATORS.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE), 20);
            if (hitResult.getType() == HitResult.Type.ENTITY && hitResult instanceof EntityHitResult result) {
                Entity entity = result.getEntity();
                if (entity instanceof LivingEntity living && !world.isClientSide) {
                    HowlingCelestialDog dog = new HowlingCelestialDog(world, user, living);
                    dog.setPos(living.position());
                    Utils.playRandomSound(ModSounds.HOWLING_CELESTIAL_DOG_SOUNDS, world, user);
                    world.addFreshEntity(dog);
                    user.getCooldowns().addCooldown(item, 20 * 30);

                    Utils.addParticlesAround(ParticleTypes.SNOWFLAKE, (ServerLevel) world, dog, 30);
                    dog.playAmbientSound();
                }
            }

            cir.setReturnValue(InteractionResultHolder.sidedSuccess(user.getItemInHand(hand), world.isClientSide));
        }
    }
}
