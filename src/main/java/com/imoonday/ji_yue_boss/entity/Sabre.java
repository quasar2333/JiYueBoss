package com.imoonday.ji_yue_boss.entity;

import com.imoonday.ji_yue_boss.init.ModEntities;
import com.imoonday.ji_yue_boss.init.ModItems;
import com.imoonday.ji_yue_boss.init.ModSounds;
import com.imoonday.ji_yue_boss.item.SabreItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fluids.FluidType;

import java.util.List;

public class Sabre extends ThrowableItemProjectile {

    private static final double RANGE = 3.0;
    private Item item;

    public Sabre(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public Sabre(double pX, double pY, double pZ, Level pLevel) {
        super(ModEntities.SABRE.get(), pX, pY, pZ, pLevel);
    }

    public Sabre(LivingEntity pShooter, Level pLevel) {
        super(ModEntities.SABRE.get(), pShooter, pLevel);
    }

    @Override
    public void setItem(ItemStack pStack) {
        super.setItem(pStack);
        this.item = pStack.getItem();
    }

    @Override
    protected Item getDefaultItem() {
        return this.item != null ? this.item : ModItems.COMMON_SABRE.get();
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.playSound(ModSounds.SWORD_DOWN.get());
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return false;
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        Level level = this.level();
        if (!level.isClientSide) {
            ItemStack stack = this.getItem();
            if (stack.getItem() instanceof SabreItem sabre) {
                SabreItem.Quality quality = sabre.getQuality();
                Entity owner = this.getOwner();
                DamageSource source = this.damageSources().mobProjectile(this, owner instanceof LivingEntity living ? living : null);
                float damage = quality.getDamage();

                double offset = RANGE / 2.0;
                ((ServerLevel) level).sendParticles(ParticleTypes.GLOW, this.getX(), this.getY(), this.getZ(), 60, offset, offset, offset, 0.0);

                List<Entity> entities = level.getEntities(this, AABB.ofSize(pResult.getLocation(), RANGE, RANGE, RANGE), EntitySelector.LIVING_ENTITY_STILL_ALIVE);
                for (Entity entity : entities) {
                    entity.hurt(source, damage);
                }
            }

            this.discard();
        }
    }
}
