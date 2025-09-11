package com.imoonday.ji_yue_boss.entity;

import com.imoonday.ji_yue_boss.init.ModEntities;
import com.imoonday.ji_yue_boss.init.ModSounds;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class HowlingCelestialDog extends PathfinderMob implements GeoEntity, OwnableEntity {

    protected static final RawAnimation SKILL_ANIMATION = RawAnimation.begin().thenLoop("skill");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private UUID ownerUUID;
    private LivingEntity owner;

    private UUID targetUUID;

    private boolean attacked;
    private int discardTimeLeft;

    public HowlingCelestialDog(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public HowlingCelestialDog(Level level, LivingEntity owner, LivingEntity target) {
        this(ModEntities.HOWLING_CELESTIAL_DOG.get(), level);
        this.setOwner(owner);
        this.setTarget(target);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (attacked && discardTimeLeft > 0) {
                discardTimeLeft--;
                if (discardTimeLeft == 0) {
                    this.discard();
                }
                return;
            }

            LivingEntity target = this.getTarget();
            if (target == null || attacked && discardTimeLeft <= 0) {
                this.discard();
            } else if (!attacked) {
                target.hurt(this.damageSources().mobAttack(this), 10);
                this.attacked = true;
                this.discardTimeLeft = 60;
            }
        }
    }

    public void setOwner(LivingEntity entity) {
        this.ownerUUID = entity.getUUID();
        this.owner = entity;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player pPlayer) {
        return false;
    }

    @Override
    public boolean canBeSeenByAnyone() {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity p_20971_) {

    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return Util.getRandom(ModSounds.BARK_SOUNDS, this.random).get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (ownerUUID != null) pCompound.putUUID("Owner", ownerUUID);
        if (targetUUID != null) pCompound.putUUID("Target", targetUUID);
        pCompound.putBoolean("Attacked", attacked);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.hasUUID("Owner")) ownerUUID = pCompound.getUUID("Owner");
        if (pCompound.hasUUID("Target")) targetUUID = pCompound.getUUID("Target");
        if (pCompound.contains("Attacked")) attacked = pCompound.getBoolean("Attacked");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "skill", 5, this::getPlayState));
    }

    protected <E extends HowlingCelestialDog> PlayState getPlayState(final AnimationState<E> event) {
        return event.setAndContinue(SKILL_ANIMATION);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        LivingEntity target = super.getTarget();
        if (target == null && targetUUID != null) {
            Level level = this.level();
            if (level instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(targetUUID);
                if (entity instanceof LivingEntity livingEntity) {
                    target = livingEntity;
                    this.setTarget(livingEntity);
                }
            }
        }
        return target;
    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        if (owner != null) {
            return owner;
        }

        LivingEntity entity = OwnableEntity.super.getOwner();
        if (entity != null) {
            setOwner(entity);
        }

        return entity;
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1).add(Attributes.ATTACK_DAMAGE, 10);
    }
}
