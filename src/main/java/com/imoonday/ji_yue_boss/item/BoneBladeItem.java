package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.init.ModSounds;
import com.imoonday.ji_yue_boss.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = JiYueBoss.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BoneBladeItem extends Item implements GeoItem {

    private static final List<BoneBladeItem> ITEMS = new ArrayList<>();
    private static final List<RegistryObject<SoundEvent>> SOUNDS = ModSounds.TELEPORT_SOUNDS;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Quality quality;

    public BoneBladeItem(Quality quality, Properties pProperties) {
        super(pProperties);
        this.quality = quality;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        ITEMS.add(this);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack).copy().withStyle(style -> style.withColor(0x02A5B4));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.ji_yue_boss.bone_blade.tooltip", (int) this.quality.getDamage()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(player, entity -> !entity.isSpectator() && entity.isPickable(), 20.0f);
        if (hitResult instanceof EntityHitResult result) {
            if (!level.isClientSide) {
                Entity entity = result.getEntity();
                Vec3 forward = entity.getLookAngle().with(Direction.Axis.Y, 0.0);
                Vec3 position = entity.position().add(forward.reverse());
                if (level.collidesWithSuffocatingBlock(player, entity.getBoundingBox().move(position))) {
                    position = entity.position().add(forward);
                    if (level.collidesWithSuffocatingBlock(player, entity.getBoundingBox().move(position))) {
                        position = entity.position();
                    }
                }
                player.teleportTo(position.x, position.y, position.z);
                player.lookAt(EntityAnchorArgument.Anchor.EYES, entity.getEyePosition());
                if (entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, this.quality.getWeaknessDuration()));
                }
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, this.quality.getInvisibilityDuration()));
                level.playSound(null, player.blockPosition(), ModSounds.TELEPORT_FORCE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                Utils.playRandomSound(SOUNDS, level, player);
                Utils.addParticlesAround(ParticleTypes.SOUL, (ServerLevel) level, player, 30);
                Utils.addCooldown(player, this.quality.getCooldown(), ITEMS);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new GeoItemExtensions<BoneBladeItem>("bone_blade"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public Quality getQuality() {
        return quality;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return;

        if (event.getSource().getEntity() instanceof Player player) {
            if (player.getMainHandItem().getItem() instanceof BoneBladeItem item) {
                boolean noTarget;
                if (entity instanceof Targeting targeting) {
                    noTarget = targeting.getTarget() == null;
                } else {
                    Optional<LivingEntity> optional = entity.getBrain().getMemoryInternal(MemoryModuleType.ATTACK_TARGET);
                    noTarget = optional == null || optional.isEmpty();
                }
                if (noTarget) {
                    entity.level().playSound(null, entity.blockPosition(), ModSounds.ATTACK.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    event.setAmount(item.getQuality().getDamage());
                }
            }
        }
    }

    public enum Quality {
        COMMON(20 * 45, 20, 20 * 5, 20 * 5),
        RARE(20 * 30, 30, 20 * 7, 20 * 7),
        MYTHIC(20 * 10, 40, 20 * 10, 20 * 10);

        private final int cooldown;
        private final float damage;
        private final int weaknessDuration;
        private final int invisibilityDuration;

        Quality(int cooldown, float damage, int weaknessDuration, int invisibilityDuration) {
            this.cooldown = cooldown;
            this.damage = damage;
            this.weaknessDuration = weaknessDuration;
            this.invisibilityDuration = invisibilityDuration;
        }

        public int getCooldown() {
            return cooldown;
        }

        public float getDamage() {
            return damage;
        }

        public int getWeaknessDuration() {
            return weaknessDuration;
        }

        public int getInvisibilityDuration() {
            return invisibilityDuration;
        }
    }
}
