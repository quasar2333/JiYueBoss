package com.imoonday.ji_yue_boss.entity;

import com.imoonday.ji_yue_boss.JiYueBoss;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.sonmok14.fromtheshadows.server.utils.registry.ItemRegistry;
import org.jetbrains.annotations.Nullable;

public abstract class BaseAmon extends Monster {

    protected BaseAmon(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected boolean canRide(Entity pEntity) {
        return false;
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
        } else {
            this.noActionTime = 0;
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        SpawnGroupData result = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
        RandomSource randomsource = pLevel.getRandom();
        this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
        return result;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
        if (JiYueBoss.fromtheshadows) {
            this.setItemSlot(EquipmentSlot.HEAD, ItemRegistry.CRUST_HEAD.get().getDefaultInstance());
            this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        }
    }

    public static AttributeSupplier.Builder createBaseAttributes() {
        return Monster.createMonsterAttributes()
                      .add(Attributes.MOVEMENT_SPEED, 0.3)
                      .add(Attributes.FOLLOW_RANGE, 40);
    }
}
