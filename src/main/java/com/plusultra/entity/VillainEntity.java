package com.plusultra.entity;

import org.jetbrains.annotations.Nullable;
import com.plusultra.quirk.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** A mission boss: tough melee fighter with a periodic "Decay" shockwave and a boss bar. */
public class VillainEntity extends Monster {
    private boolean wasEnraged = false;
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    public VillainEntity(EntityType<? extends VillainEntity> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossEvent.setProgress(getHealth() / getMaxHealth());

        boolean enraged = getHealth() < getMaxHealth() * 0.5f;
        if (enraged && !wasEnraged) {
            wasEnraged = true;
            bossEvent.setColor(BossEvent.BossBarColor.RED);
            addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 0, false, false));
            addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 0, false, false));
            if (level() instanceof ServerLevel sl) {
                playSound(SoundEvents.ENDER_DRAGON_GROWL, 2.0f, 0.7f);
                Fx.sphere(sl, Fx.dust(0xB22222, 2.0f), position().add(0, 1, 0), 1.8, 60);
            }
        }

        // Menacing aura: dark smoke, red embers when enraged.
        if (tickCount % 4 == 0 && level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.0, getZ(), 3, 0.4, 0.8, 0.4, 0.01);
            if (enraged) sl.sendParticles(Fx.dust(0xB22222, 1.4f), getX(), getY() + 1.0, getZ(), 3, 0.4, 0.8, 0.4, 0);
        }

        // "Decay" shockwave every 6 seconds (4 when enraged) while a target is close.
        int interval = enraged ? 80 : 120;
        if (tickCount % interval == 0 && getTarget() != null && distanceToSqr(getTarget()) < 12 * 12
                && level() instanceof ServerLevel sl) {
            Vec3 c = position();
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y + 0.5, c.z, 40, 2.0, 0.3, 2.0, 0.02);
            for (int r = 1; r <= 7; r += 2) {
                Fx.ring(sl, Fx.dust(0x6A0DAD, 1.6f), c.add(0, 0.2, 0), r, 12 + r * 5);
                Fx.ring(sl, ParticleTypes.SOUL_FIRE_FLAME, c.add(0, 0.4, 0), r, 8 + r * 3);
            }
            Fx.spiral(sl, ParticleTypes.SQUID_INK, c, 3, 1.0, 3, 30);
            sl.playSound(null, c.x, c.y, c.z, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.0f, 0.6f);
            for (Player p : sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(7))) {
                p.hurt(damageSources().mobAttack(this), enraged ? 8.0f : 6.0f);
                p.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                Vec3 away = p.position().subtract(c).normalize();
                p.push(away.x * 1.2, 0.4, away.z * 1.2);
                p.hurtMarked = true;
            }
        }
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (hasCustomName()) bossEvent.setName(getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}
