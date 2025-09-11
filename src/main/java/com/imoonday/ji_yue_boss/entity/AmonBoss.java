package com.imoonday.ji_yue_boss.entity;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.client.sound.BossMusicPlayer;
import com.imoonday.ji_yue_boss.entity.goal.HitAndRunMeleeAttackGoal;
import com.imoonday.ji_yue_boss.init.ModEntities;
import com.imoonday.ji_yue_boss.init.ModSounds;
import com.lothrazar.cyclic.registry.ItemRegistry;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collection;

public class AmonBoss extends BaseAmon {

    private static final byte MUSIC_PLAY_ID = 67;
    private static final byte MUSIC_STOP_ID = 68;
    public static final MobEffect[] DEBUFFS = new MobEffect[]{MobEffects.BLINDNESS, MobEffects.CONFUSION, MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.POISON, MobEffects.WITHER, MobEffects.HARM};
    public static final int DEBUFF_COOLDOWN = 20 * 15;
    public static final int DEBUFF_DURATION = 20 * 5;
    public static final MobEffect[] BUFFS = new MobEffect[]{MobEffects.REGENERATION, MobEffects.DAMAGE_RESISTANCE, MobEffects.DAMAGE_BOOST};
    public static final int BUFF_COOLDOWN = 20 * 15;
    public static final int BUFF_DURATION = 20 * 10;
    public static final MobEffect[] PERMANENT_BUFFS = new MobEffect[]{MobEffects.FIRE_RESISTANCE};
    public static final int CLONE_COOLDOWN = 20 * 20;
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
    private final Amon[] clones = new Amon[4];
    private LivingEntity lastTarget;
    private boolean inCombat;
    private boolean wasInCombat;
    private int lastHurtMessageIndex;
    private int debuffCooldown;
    private int buffCooldown;
    private int cloneCooldown;

    public AmonBoss(EntityType<? extends BaseAmon> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public AmonBoss(Level level, Vec3 pos) {
        this(ModEntities.AMON_BOSS.get(), level);
        this.moveTo(pos);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HitAndRunMeleeAttackGoal(this, 1.2, false, 5.0f));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("DebuffCooldown", this.debuffCooldown);
        pCompound.putInt("BuffCooldown", this.buffCooldown);
        pCompound.putInt("CloneCooldown", this.cloneCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.debuffCooldown = pCompound.contains("EffectCooldown") ? pCompound.getInt("EffectCooldown") : pCompound.getInt("DebuffCooldown");
        this.buffCooldown = pCompound.contains("OwnEffectCooldown") ? pCompound.getInt("OwnEffectCooldown") : pCompound.getInt("BuffCooldown");
        this.cloneCooldown = pCompound.getInt("CloneCooldown");
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomName(@Nullable Component pName) {
        super.setCustomName(pName);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void tick() {
        Level level = this.level();
        if (!level.isClientSide) {
            level.broadcastEntityEvent(this, !isSilent() ? MUSIC_PLAY_ID : MUSIC_STOP_ID);
        }
        super.tick();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        debuffTick();
        buffTick();
        cloneTick();
    }

    public void debuffTick() {
        if (this.debuffCooldown > 0) {
            --this.debuffCooldown;
        }
        if (this.debuffCooldown <= 0) {
            Collection<ServerPlayer> players = this.bossEvent.getPlayers();
            boolean added = false;
            for (ServerPlayer player : players) {
                if (player.getAbilities().instabuild || player.isSpectator()) continue;
                if (addDebuff(player)) {
                    added = true;
                }
            }
            if (added) {
                resetDebuffCooldown();
            }
        }
    }

    public int getDebuffCooldown() {
        return debuffCooldown;
    }

    public void resetDebuffCooldown() {
        this.debuffCooldown = DEBUFF_COOLDOWN;
    }

    public boolean addDebuff(Player player) {
        MobEffect debuff = Util.getRandom(DEBUFFS, this.random);
        return player.addEffect(new MobEffectInstance(debuff, DEBUFF_DURATION));
    }

    public void buffTick() {
        if (this.buffCooldown > 0) {
            --this.buffCooldown;
        }
        if (this.buffCooldown <= 0) {
            resetBuffCooldown();
            addBuff();
        }
        addPermanentBuffs();
    }

    private void addPermanentBuffs() {
        for (MobEffect buff : PERMANENT_BUFFS) {
            if (!this.hasEffect(buff)) {
                this.addEffect(new MobEffectInstance(buff, -1));
            }
        }
    }

    public int getBuffCooldown() {
        return buffCooldown;
    }

    public void resetBuffCooldown() {
        this.buffCooldown = BUFF_COOLDOWN;
    }

    public void addBuff() {
        MobEffect buff = Util.getRandom(BUFFS, this.random);
        this.addEffect(new MobEffectInstance(buff, BUFF_DURATION));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.bossEvent.addPlayer(pPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.bossEvent.removePlayer(pPlayer);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
        if (JiYueBoss.cyclic) {
            ItemStack helmet = ItemRegistry.GLOWING_HELMET.get().getDefaultInstance();
            helmet.getOrCreateTag().putBoolean("Unbreakable", true);
            this.spawnAtLocation(helmet);
        }
        ItemStack book = Items.BOOK.getDefaultInstance();
        book.setHoverName(Component.translatable("item.ji_yue_boss.diary"));
        this.spawnAtLocation(book);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BaseAmon.createBaseAttributes()
                       .add(Attributes.MAX_HEALTH, 500)
                       .add(Attributes.ARMOR, 20)
                       .add(Attributes.ATTACK_DAMAGE, 15)
                       .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        boolean result = super.hurt(pSource, pAmount);
        if (result) {
            Entity entity = pSource.getEntity();
            if (entity instanceof Player player) {
                if (this.random.nextFloat() < 0.2f) {
                    int index;
                    do {
                        index = this.random.nextInt(4) + 1;
                    } while (index == this.lastHurtMessageIndex);
                    player.sendSystemMessage(Component.translatable("msg.ji_yue_boss.amon_boss.hurt." + index));
                    this.lastHurtMessageIndex = index;
                }
            }
        }
        return result;
    }

    @Override
    protected void tickDeath() {
        if (this.deathTime == 0 && !this.level().isClientSide()) {
            for (ServerPlayer player : this.bossEvent.getPlayers()) {
                player.sendSystemMessage(Component.translatable("msg.ji_yue_boss.amon_boss.death"));
            }
        }
        super.tickDeath();
    }

    public void cloneTick() {
        LivingEntity target = this.getTarget();
        if (target == null && !this.inCombat) {
            if (this.lastTarget != null || this.wasInCombat) {
                this.cloneCooldown = 0;
                for (int i = 0; i < 4; i++) {
                    Amon clone = this.clones[i];
                    if (clone != null) {
                        clone.discard();
                    }
                    this.clones[i] = null;
                }
                this.setHealth(this.getMaxHealth());
            }
        } else {
            if (!hasClone()) {
                if (this.cloneCooldown > 0) {
                    --this.cloneCooldown;
                }
                if (this.cloneCooldown <= 0) {
                    this.cloneCooldown = CLONE_COOLDOWN;
                    refreshClones();
                }
            } else {
                this.cloneCooldown = CLONE_COOLDOWN;
            }
        }

        if (hasClone()) {
            if (!this.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 3, false, false));
            }
        } else if (this.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            this.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        }

        this.lastTarget = target;
        this.wasInCombat = this.inCombat;
    }

    public void refreshClones() {
        Level level = this.level();
        for (int i = 0; i < 4; ++i) {
            Amon clone = this.clones[i];
            if (clone != null) {
                clone.discard();
            }

            Amon amon = new Amon(level, this.position().relative(Direction.values()[i + 2], 1), this);
            LivingEntity target = this.getTarget();
            if (target == null) {
                target = level.getNearestPlayer(this, 8);
            }
            if (target != null) {
                amon.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
                amon.setTarget(target);
            }
            if (!level.noCollision(amon)) {
                amon.teleportRelative(0, 1, 0);
            }
            if (!level.noCollision(amon)) {
                amon.teleportTo(this.getX(), this.getY(), this.getZ());
            }
            amon.randomTeleport(amon.getX(), amon.getY(), amon.getZ(), false);
            amon.teleportRelative(0, 0.5, 0);
            amon.setDeltaMovement(0, 0.25, 0);

            amon.populateDefaultEquipmentSlots(level.getRandom(), level.getCurrentDifficultyAt(amon.blockPosition()));

            level.addFreshEntity(amon);
            this.clones[i] = amon;
        }
    }

    public boolean hasClone() {
        for (Amon clone : this.clones) {
            if (clone != null && clone.isAlive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == MUSIC_PLAY_ID) {
            BossMusicPlayer.playBossMusic(this, ModSounds.AMON_BGM.get(), 40);
        } else if (id == MUSIC_STOP_ID) {
            BossMusicPlayer.stopBossMusic(this);
        } else {
            super.handleEntityEvent(id);
        }
    }

    public boolean addClone(Amon clone) {
        for (int i = 0; i < 4; i++) {
            if (this.clones[i] == null) {
                this.clones[i] = clone;
                return true;
            }
        }
        return false;
    }

    public Amon[] getClones() {
        return clones;
    }

    public boolean isClone(Amon amon) {
        for (Amon clone : this.clones) {
            if (clone == amon) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
        BossMusicPlayer.stopBossMusic(this);
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        this.bossEvent.removeAllPlayers();
    }

    @Override
    public void onEnterCombat() {
        super.onEnterCombat();
        this.inCombat = true;
    }

    @Override
    public void onLeaveCombat() {
        super.onLeaveCombat();
        this.inCombat = false;
    }
}
