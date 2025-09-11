package com.imoonday.ji_yue_boss.entity;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class FakeAmon extends BaseAmon {

    public FakeAmon(EntityType<? extends BaseAmon> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1, false));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BaseAmon.createBaseAttributes()
                      .add(Attributes.MAX_HEALTH, 500)
                      .add(Attributes.ATTACK_DAMAGE, 1);
    }

    @Override
    protected void tickDeath() {
        Level level = this.level();
        if (this.deathTime == 0 && !level.isClientSide()) {
            BlockPos pos = this.blockPosition();
            for (Player player : level.getNearbyPlayers(TargetingConditions.forNonCombat(), this, new AABB(pos).inflate(40))) {
                player.sendSystemMessage(Component.translatable("msg.ji_yue_boss.fake_amon.death"));
            }
        }

        ++this.deathTime;
        if (this.isRemoved()) return;
        if (this.deathTime >= 20) {
            if (level.isClientSide()) {
                this.remove(RemovalReason.KILLED);
            } else {
                if (this.deathTime == 20) {
                    level.broadcastEntityEvent(this, (byte) 60);
                } else if (this.deathTime >= 20 * 4) {
                    this.remove(Entity.RemovalReason.KILLED);

                    AmonBoss amonBoss = new AmonBoss(level, this.position());
                    Player player = level.getNearestPlayer(this, 8);
                    if (player != null) {
                        amonBoss.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
                    }
                    level.addFreshEntity(amonBoss);
                }
            }
        }
    }
}
