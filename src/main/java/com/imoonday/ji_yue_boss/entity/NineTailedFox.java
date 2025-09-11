package com.imoonday.ji_yue_boss.entity;

import com.imoonday.ji_yue_boss.init.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
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

import java.util.List;

public class NineTailedFox extends Monster implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private Player owner;
    private int lifespan = 20 * 10; // 默认10秒生存时间
    private int attackDamage = 3; // 默认攻击力
    private int customAge = 0; // 自定义年龄计数器，避免与Minecraft内置age冲突

    public NineTailedFox(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setInvulnerable(true); // 无敌
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, (entity) -> {
            // 不攻击自己、主人和其他九尾狐
            return entity != this && entity != owner && !(entity instanceof NineTailedFox);
        }));
    }

    @Override
    public void tick() {
        super.tick();
        // 仅在服务端计时与判定生命周期，避免客户端默认200t导致提前移除
        if (!this.level().isClientSide) {
            customAge++;
            // 每5秒输出一次调试信息（服务端）
            if (customAge % 100 == 0) {
                System.out.println("九尾狐状态检查 - CustomAge: " + customAge + ", Lifespan: " + lifespan + ", 剩余时间: " + (lifespan - customAge) + " ticks");
            }
            // 检查生命周期（服务端权威）
            if (customAge >= lifespan) {
                System.out.println("九尾狐生命周期结束 - 年龄: " + customAge + " ticks, 存活时间: " + lifespan + " ticks");
                this.discard();
                return;
            }
        }

        // 每20tick检查一次攻击目标（服务端）
        if (customAge % 20 == 0 && !this.level().isClientSide) {
            LivingEntity target = this.getTarget();
            if (target != null && this.distanceToSqr(target) < 16.0D) {
                // 10%概率释放九尾天火
                if (this.random.nextFloat() < 0.1f) {
                    castNineTailedFire();
                }
            }
        }
    }

    private void castNineTailedFire() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // 播放技能动画
        this.triggerAnim("skillController", "skill");

        // 执行/title命令并同时播放音效
        if (owner instanceof ServerPlayer serverPlayer) {
            serverLevel.getServer().getCommands().performPrefixedCommand(
                serverLevel.getServer().createCommandSourceStack(),
                "title @a title {\"text\":\"九尾天火\",\"color\":\"red\",\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false}"
            );
            // 在显示标题的同时播放九尾天火音效
            serverLevel.playSound(null, this.blockPosition(), ModSounds.NINE_TAILED_FIRE.get(), SoundSource.HOSTILE, 1.0f, 1.0f);
        }
        
        // 生成粒子效果
        Vec3 center = this.position();
        for (int i = 0; i < 30; i++) {
            double x = center.x + (random.nextDouble() - 0.5) * 14;
            double z = center.z + (random.nextDouble() - 0.5) * 14;
            double y = center.y + random.nextDouble() * 2;
            serverLevel.sendParticles(ParticleTypes.LAVA, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        
        // 对范围内敌人造成伤害和燃烧
        AABB area = this.getBoundingBox().inflate(7.0);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity target : targets) {
            if (target != this && target != owner && target.isAlive()) {
                target.hurt(this.damageSources().mobAttack(this), 2.0f);
                target.setSecondsOnFire(5); // 给予5秒燃烧效果
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false; // 无敌
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public void setLifespan(int lifespan) {
        System.out.println("设置九尾狐存活时间: " + lifespan + " ticks (" + (lifespan / 20.0) + " 秒)");
        this.lifespan = lifespan;
    }

    public void setAttackDamage(int attackDamage) {
        this.attackDamage = attackDamage;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attackDamage);
    }

    @Override
    public Component getName() {
        return Component.literal("九尾");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "skillController", 0, this::skillPredicate));
    }

    private <T extends GeoEntity> PlayState predicate(AnimationState<T> tAnimationState) {
        if (tAnimationState.isMoving()) {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("walk", Animation.LoopType.LOOP));
        } else {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    private <T extends GeoEntity> PlayState skillPredicate(AnimationState<T> tAnimationState) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("CustomAge", this.customAge);
        compound.putInt("Lifespan", this.lifespan);
        compound.putInt("AttackDamage", this.attackDamage);
        System.out.println("保存九尾狐数据 - CustomAge: " + this.customAge + ", Lifespan: " + this.lifespan);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.customAge = compound.getInt("CustomAge");
        this.lifespan = compound.getInt("Lifespan");
        this.attackDamage = compound.getInt("AttackDamage");
        System.out.println("加载九尾狐数据 - CustomAge: " + this.customAge + ", Lifespan: " + this.lifespan);

        // 确保攻击力属性也被正确设置
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.attackDamage);
        }
    }
}
