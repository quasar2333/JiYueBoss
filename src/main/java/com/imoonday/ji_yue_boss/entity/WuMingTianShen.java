package com.imoonday.ji_yue_boss.entity;

import com.imoonday.ji_yue_boss.init.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class WuMingTianShen extends Monster implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("无名天神"),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.PROGRESS
    );

    private boolean isAttacking = false;
    private int attackCooldown = 0;
    private int attackAnimationTick = 0;
    private static final int ATTACK_ANIMATION_LENGTH = 20; // 1 second (20 ticks)
    private static final int DAMAGE_DELAY_TICKS = 10; // 0.5 seconds delay for damage
    private boolean hasDamagedThisAttack = false;

    private int outOfCombatTicks = 0;
    private static final int OUT_OF_COMBAT_THRESHOLD = 100; // 5 seconds to consider out of combat
    private boolean inCombat = false;

    public WuMingTianShen(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.bossEvent.setVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D); // 霸体
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new WuMingTianShenAttackGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Update boss bar
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            // Attack cooldown
            if (attackCooldown > 0) {
                attackCooldown--;
            }

            // Attack animation tick
            if (isAttacking) {
                attackAnimationTick++;

                // Apply damage at the delay point
                if (attackAnimationTick == DAMAGE_DELAY_TICKS && !hasDamagedThisAttack) {
                    performAttackDamage();
                    hasDamagedThisAttack = true;
                }

                // End attack animation
                if (attackAnimationTick >= ATTACK_ANIMATION_LENGTH) {
                    isAttacking = false;
                    attackAnimationTick = 0;
                    hasDamagedThisAttack = false;
                    attackCooldown = 20; // 1 second cooldown between attacks
                }
            }

            // Combat tracking
            if (inCombat) {
                outOfCombatTicks++;
                if (outOfCombatTicks >= OUT_OF_COMBAT_THRESHOLD) {
                    exitCombat();
                }
            }

            // Check for players in front (1 block range) and trigger attack
            if (!isAttacking && attackCooldown <= 0) {
                checkAndAttackPlayersInFront();
            }
        }
    }

    private void checkAndAttackPlayersInFront() {
        Vec3 lookVec = this.getLookAngle();
        Vec3 pos = this.position();

        // Create detection box in front (1 block range)
        AABB detectionBox = new AABB(
                pos.x - 1, pos.y, pos.z - 1,
                pos.x + 1, pos.y + 2, pos.z + 1
        ).move(lookVec.x * 0.75, 0, lookVec.z * 0.75);

        List<Player> playersInFront = this.level().getEntitiesOfClass(Player.class, detectionBox,
                player -> {
                    if (player.isSpectator() || player.isCreative()) return false;
                    // Check if player is actually in front
                    Vec3 toPlayer = player.position().subtract(this.position()).normalize();
                    double dot = toPlayer.x * lookVec.x + toPlayer.z * lookVec.z;
                    return dot > 0.3; // In front cone
                });

        if (!playersInFront.isEmpty()) {
            startAttack();
        }
    }

    private void performAttackDamage() {
        if (this.level().isClientSide) return;

        // Get entities in front (2 blocks range)
        Vec3 lookVec = this.getLookAngle();
        Vec3 pos = this.position();

        // Create AABB for 2 blocks in front
        AABB attackBox = new AABB(
                pos.x - 1, pos.y, pos.z - 1,
                pos.x + 1, pos.y + 2, pos.z + 1
        ).move(lookVec.x * 1.5, 0, lookVec.z * 1.5);

        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, attackBox,
                entity -> entity != this && entity instanceof Player);

        for (LivingEntity target : targets) {
            target.hurt(this.damageSources().mobAttack(this), 8.0f);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            // Check if attack is from the front
            if (isAttackFromFront(attacker)) {
                // Play front hit sound
                if (!this.level().isClientSide) {
                    this.level().playSound(null, this.blockPosition(), ModSounds.WEI_YANG_HIT.get(), SoundSource.HOSTILE, 1.0f, 1.0f);
                }
                return false; // No damage from front
            }

            // Enter combat
            if (!inCombat) {
                enterCombat();
            }
            outOfCombatTicks = 0;
        }

        return super.hurt(source, amount);
    }

    private boolean isAttackFromFront(LivingEntity attacker) {
        Vec3 toAttacker = attacker.position().subtract(this.position()).normalize();
        Vec3 lookVec = this.getLookAngle();

        // Dot product: positive means in front, negative means behind
        double dot = toAttacker.x * lookVec.x + toAttacker.z * lookVec.z;

        // If dot > 0, attacker is in front (within 180 degrees forward cone)
        return dot > 0;
    }

    @Override
    public int getMaxHeadYRot() {
        return 5; // Half of normal turn speed (normally around 10)
    }

    private void enterCombat() {
        inCombat = true;
        outOfCombatTicks = 0;

        // Start playing battle music
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.blockPosition(), ModSounds.BATTLE_MUSIC.get(), SoundSource.RECORDS, 1.0f, 1.0f);
        }
    }

    private void exitCombat() {
        inCombat = false;
        outOfCombatTicks = 0;

        // Heal to full
        this.setHealth(this.getMaxHealth());

        // Stop BGM is handled by the sound system naturally when source moves away
        // For BLOCK source type, sound will stop when entity is removed or player moves away
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);

        // Execute fill command on death
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().getCommands().performPrefixedCommand(
                    serverLevel.getServer().createCommandSourceStack(),
                    "/fill 1761 -40 1153 1761 -42 1153 minecraft:air"
            );
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public void startAttack() {
        if (!isAttacking && attackCooldown <= 0) {
            isAttacking = true;
            attackAnimationTick = 0;
            hasDamagedThisAttack = false;
            // Trigger attack animation
            this.triggerAnim("attackController", "attack");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("attack")));
    }

    private <T extends GeoEntity> PlayState predicate(AnimationState<T> state) {
        if (state.isMoving()) {
            state.getController().setAnimation(RawAnimation.begin().then("walk", Animation.LoopType.LOOP));
        } else {
            state.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    private <T extends GeoEntity> PlayState attackPredicate(AnimationState<T> state) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("InCombat", this.inCombat);
        compound.putInt("OutOfCombatTicks", this.outOfCombatTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.inCombat = compound.getBoolean("InCombat");
        this.outOfCombatTicks = compound.getInt("OutOfCombatTicks");
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // Boss doesn't despawn
    }

    @Override
    public boolean isPushable() {
        return false; // 霸体，不会被推动
    }

    @Override
    protected void pushEntities() {
        // 不推动其他实体
    }

    // Custom attack goal for detecting players in front
    public static class WuMingTianShenAttackGoal extends Goal {
        private final WuMingTianShen entity;

        public WuMingTianShenAttackGoal(WuMingTianShen entity) {
            this.entity = entity;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return entity.getTarget() != null && !entity.isAttacking();
        }

        @Override
        public void tick() {
            LivingEntity target = entity.getTarget();
            if (target == null) return;

            // Look at target
            entity.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Check if player is within 1 block in front (1.5 distance = ~1 block)
            double distance = entity.distanceTo(target);
            if (distance <= 1.5 && isTargetInFront(target)) {
                entity.startAttack();
            } else {
                // Move towards target
                entity.getNavigation().moveTo(target, 1.0);
            }
        }

        private boolean isTargetInFront(LivingEntity target) {
            Vec3 toTarget = target.position().subtract(entity.position()).normalize();
            Vec3 lookVec = entity.getLookAngle();

            double dot = toTarget.x * lookVec.x + toTarget.z * lookVec.z;
            return dot > 0.5; // Roughly 60 degree cone in front
        }
    }
}
