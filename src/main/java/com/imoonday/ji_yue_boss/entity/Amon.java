package com.imoonday.ji_yue_boss.entity;

import com.imoonday.ji_yue_boss.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class Amon extends BaseAmon implements OwnableEntity {

    private AmonBoss owner;
    private UUID ownerUUID;

    public Amon(EntityType<? extends BaseAmon> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public Amon(Level level, Vec3 pos, AmonBoss owner) {
        super(ModEntities.AMON.get(), level);
        this.moveTo(pos);
        this.setOwner(owner);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f, false));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (this.ownerUUID != null) {
            pCompound.putUUID("OwnerUUID", this.ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.hasUUID("OwnerUUID")) {
            this.ownerUUID = pCompound.getUUID("OwnerUUID");
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            AmonBoss owner = this.getOwner();
            boolean invalidOwner = owner != null && (!owner.isAlive() || !owner.isClone(this));
            boolean ownerNotFound = owner == null && ownerUUID != null;
            if (invalidOwner || ownerNotFound) {
                this.discard();
            }
        }
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public AmonBoss getOwner() {
        if (owner == null && ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUUID);
            if (entity instanceof AmonBoss boss && boss.addClone(this)) {
                owner = boss;
            }
        }
        return owner;
    }

    public void setOwner(AmonBoss owner) {
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BaseAmon.createBaseAttributes()
                       .add(Attributes.MAX_HEALTH, 250)
                       .add(Attributes.ARMOR, 20)
                       .add(Attributes.ATTACK_DAMAGE, 8);
    }

    public static class OwnerHurtTargetGoal extends TargetGoal {

        private final Amon amon;
        private LivingEntity ownerLastHurt;
        private int timestamp;

        public OwnerHurtTargetGoal(Amon amon) {
            super(amon, false);
            this.amon = amon;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = this.amon.getOwner();
            if (owner == null) {
                return false;
            } else {
                this.ownerLastHurt = owner.getLastHurtMob();
                int i = owner.getLastHurtMobTimestamp();
                return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT);
            }
        }

        @Override
        public void start() {
            this.mob.setTarget(this.ownerLastHurt);
            LivingEntity livingentity = this.amon.getOwner();
            if (livingentity != null) {
                this.timestamp = livingentity.getLastHurtMobTimestamp();
            }

            super.start();
        }
    }

    public static class OwnerHurtByTargetGoal extends TargetGoal {

        private final Amon amon;
        private LivingEntity ownerLastHurtBy;
        private int timestamp;

        public OwnerHurtByTargetGoal(Amon amon) {
            super(amon, false);
            this.amon = amon;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingentity = this.amon.getOwner();
            if (livingentity == null) {
                return false;
            } else {
                this.ownerLastHurtBy = livingentity.getLastHurtByMob();
                int i = livingentity.getLastHurtByMobTimestamp();
                return i != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
            }
        }

        @Override
        public void start() {
            this.mob.setTarget(this.ownerLastHurtBy);
            LivingEntity livingentity = this.amon.getOwner();
            if (livingentity != null) {
                this.timestamp = livingentity.getLastHurtByMobTimestamp();
            }

            super.start();
        }
    }

    public static class FollowOwnerGoal extends Goal {

        private final Amon amon;
        private AmonBoss owner;
        private final LevelReader level;
        private final double speedModifier;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private final float startDistance;
        private float oldWaterCost;
        private final boolean canFly;

        public FollowOwnerGoal(Amon amon, double speedModifier, float startDistance, float stopDistance, boolean canFly) {
            this.amon = amon;
            this.level = amon.level();
            this.speedModifier = speedModifier;
            this.navigation = amon.getNavigation();
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
            this.canFly = canFly;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
            if (!(amon.getNavigation() instanceof GroundPathNavigation) && !(amon.getNavigation() instanceof FlyingPathNavigation)) {
                throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
            }
        }

        @Override
        public boolean canUse() {
            AmonBoss owner = this.amon.getOwner();
            if (owner == null) {
                return false;
            } else if (owner.isSpectator()) {
                return false;
            } else if (this.unableToMove()) {
                return false;
            } else if (this.amon.distanceToSqr(owner) < (double) (this.startDistance * this.startDistance)) {
                return false;
            } else {
                this.owner = owner;
                return true;
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (this.navigation.isDone()) {
                return false;
            } else if (this.unableToMove()) {
                return false;
            } else {
                return !(this.amon.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
            }
        }

        private boolean unableToMove() {
            return this.amon.isPassenger() || this.amon.isLeashed();
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.amon.getPathfindingMalus(BlockPathTypes.WATER);
            this.amon.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        }

        @Override
        public void stop() {
            this.owner = null;
            this.navigation.stop();
            this.amon.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        }

        @Override
        public void tick() {
            this.amon.getLookControl().setLookAt(this.owner, 10.0F, (float) this.amon.getMaxHeadXRot());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (this.amon.distanceToSqr(this.owner) >= 144.0D) {
                    this.teleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }

            }
        }

        private void teleportToOwner() {
            BlockPos blockpos = this.owner.blockPosition();

            for (int i = 0; i < 10; ++i) {
                int j = this.randomIntInclusive(-3, 3);
                int k = this.randomIntInclusive(-1, 1);
                int l = this.randomIntInclusive(-3, 3);
                boolean flag = this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
                if (flag) {
                    return;
                }
            }

        }

        private boolean maybeTeleportTo(int pX, int pY, int pZ) {
            if (Math.abs((double) pX - this.owner.getX()) < 2.0D && Math.abs((double) pZ - this.owner.getZ()) < 2.0D) {
                return false;
            } else if (!this.canTeleportTo(new BlockPos(pX, pY, pZ))) {
                return false;
            } else {
                this.amon.moveTo((double) pX + 0.5D, pY, (double) pZ + 0.5D, this.amon.getYRot(), this.amon.getXRot());
                this.navigation.stop();
                return true;
            }
        }

        private boolean canTeleportTo(BlockPos pPos) {
            BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, pPos.mutable());
            if (blockpathtypes != BlockPathTypes.WALKABLE) {
                return false;
            } else {
                BlockState blockstate = this.level.getBlockState(pPos.below());
                if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
                    return false;
                } else {
                    BlockPos blockpos = pPos.subtract(this.amon.blockPosition());
                    return this.level.noCollision(this.amon, this.amon.getBoundingBox().move(blockpos));
                }
            }
        }

        private int randomIntInclusive(int pMin, int pMax) {
            return this.amon.getRandom().nextInt(pMax - pMin + 1) + pMin;
        }
    }
}
