package com.imoonday.ji_yue_boss.entity.goal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class HitAndRunMeleeAttackGoal extends Goal {

    private final PathfinderMob mob;
    private final double speedModifier;
    private final boolean followTarget;
    private int ticksUntilNextAttack;
    private int retreatCooldown; // 撤退后的冷却时间
    private final double retreatDistanceSqr; // 撤退的目标距离平方
    private boolean isRetreating; // 是否处于撤退状态

    public HitAndRunMeleeAttackGoal(PathfinderMob mob, double speed, boolean follow, float retreatDistance) {
        this.mob = mob;
        this.speedModifier = speed;
        this.followTarget = follow;
        this.retreatDistanceSqr = retreatDistance * retreatDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    // 启动条件：有目标且未处于撤退冷却期
    @Override
    public boolean canUse() {
        if (this.retreatCooldown > 0) {
            return false; // 冷却期间不启动
        }
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive() &&
               (this.followTarget || this.mob.getSensing().hasLineOfSight(target));
    }

    @Override
    public void start() {
        this.isRetreating = false; // 初始为接近状态
        this.retreatCooldown = 0;
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
        this.isRetreating = false;
        this.retreatCooldown = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        double distanceSq = this.mob.distanceToSqr(target);

        if (isRetreating) {
            handleRetreatPhase(target, distanceSq);
        } else {
            handleAttackPhase(target, distanceSq);
        }
    }

    //--- 攻击阶段逻辑 ---
    private void handleAttackPhase(LivingEntity target, double distanceSq) {
        // 注视目标
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 计算攻击距离
        double attackReach = this.getAttackReachSqr(target);

        // 移动到攻击范围
        if (distanceSq > attackReach) {
            this.mob.getNavigation().moveTo(target, this.speedModifier);
        } else {
            this.mob.getNavigation().stop();
        }

        // 执行攻击
        if (distanceSq <= attackReach && this.ticksUntilNextAttack <= 0) {
            performAttack(target);
            startRetreat(); // 攻击后立即触发撤退
        }

        // 更新攻击冷却
        if (this.ticksUntilNextAttack > 0) {
            this.ticksUntilNextAttack--;
        }
    }

    //--- 撤退阶段逻辑 ---
    private void handleRetreatPhase(LivingEntity target, double currentDistanceSq) {
        // 如果已经撤退到安全距离，进入冷却
        if (currentDistanceSq >= this.retreatDistanceSqr) {
            this.retreatCooldown = 40; // 冷却 2 秒（40 ticks）
            this.isRetreating = false;
            this.mob.getNavigation().stop();
            return;
        }

        // 计算远离目标的方向
        double dx = this.mob.getX() - target.getX();
        double dz = this.mob.getZ() - target.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);

        // 标准化方向并设置移动速度
        if (length > 0.0001) {
            dx /= length;
            dz /= length;
            double speed = this.speedModifier * 1.2; // 撤退速度更快

            // 向反方向移动
            this.mob.getNavigation().moveTo(
                    this.mob.getX() + dx * 10,
                    this.mob.getY(),
                    this.mob.getZ() + dz * 10,
                    speed
            );
        }
    }

    private void performAttack(LivingEntity target) {
        this.mob.swing(InteractionHand.MAIN_HAND);
        this.mob.doHurtTarget(target);
        this.ticksUntilNextAttack = 20; // 攻击冷却 1 秒
    }

    private void startRetreat() {
        this.isRetreating = true;
        this.mob.getNavigation().stop(); // 停止当前路径
    }

    // 其他辅助方法与原版 MeleeAttackGoal 类似...
    protected double getAttackReachSqr(LivingEntity target) {
        return (this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + target.getBbWidth());
    }
}