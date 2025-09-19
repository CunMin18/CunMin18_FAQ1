package org.cunmin18.cunmin18_faq1.villager_tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.cunmin18.cunmin18_faq1.commands.EnchantmentSettingCommand;
import org.cunmin18.cunmin18_faq1.config.ModClientConfig;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;
import org.cunmin18.cunmin18_faq1.utils.EntityEnchantmentTools;
import org.cunmin18.cunmin18_faq1.utils.IStatusEffectInstanceMixin;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ClericVillagerTask extends MultiTickTask<VillagerEntity> {
    @Nullable
    private LivingEntity currentTarget;
    private long nextResponseTime;
    private final List<BlockPos> targetPositions = Lists.newArrayList();
    public ClericVillagerTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT));
    }
    protected boolean shouldRun(ServerWorld serverWorld, VillagerEntity villagerEntity) {
        return ModClientConfig.isEnable_cleric_task;
    }
    protected void run(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        this.currentTarget = this.findNearestThrowableEntity(villagerEntity);
        if (l > this.nextResponseTime && this.currentTarget != null) {
            villagerEntity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(this.currentTarget.getBlockPos()));
            villagerEntity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosLookTarget(this.currentTarget.getBlockPos()), 0.5F, 1));
        }
    }

    protected void finishRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        villagerEntity.getBrain().forget(MemoryModuleType.LOOK_TARGET);
        villagerEntity.getBrain().forget(MemoryModuleType.WALK_TARGET);
        resetNextResponseTime(villagerEntity,l);
    }

    //持续寻找目标并丢药水
    protected void keepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        this.currentTarget = this.findNearestThrowableEntity(villagerEntity);
        if (this.currentTarget != null && this.currentTarget.getPos().distanceTo(villagerEntity.getPos()) <= 8.0D) {
            if (l > this.nextResponseTime) {
                shootAt(villagerEntity,this.currentTarget);
                this.targetPositions.remove(this.currentTarget);
                this.currentTarget = this.findNearestThrowableEntity(villagerEntity);
                if (this.currentTarget != null) {
                    resetNextResponseTime(villagerEntity,l);
                    villagerEntity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosLookTarget(this.currentTarget.getBlockPos().up()), 0.5F, 1));
                    villagerEntity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(this.currentTarget.getBlockPos().up()));
                }
            }
        }
        else if(this.currentTarget != null){
            villagerEntity.getNavigation().startMovingTo(currentTarget, 0.8F);
        }
    }

    //丢药水
    private void shootAt(VillagerEntity entity,LivingEntity target) {
        Vec3d vec3d = target.getVelocity();
        double d = target.getX() + vec3d.x - entity.getX();
        double e = target.getEyeY() - (double)1.1F - entity.getY();
        double f = target.getZ() + vec3d.z - entity.getZ();
        double g = Math.sqrt(d * d + f * f);
        Potion potion = Potions.HARMING;
        if (isFriend(target)) {
            if (target.getHealth() <= 6.0F) {
                potion = Potions.HEALING;
            }
            else if (target instanceof IronGolemEntity) {
                if(target.getHealth() <= 50F){
                    potion = Potions.HEALING;
                }
                else{
                    potion = Potions.STRENGTH;
                }
            } else {
                potion = Potions.REGENERATION;
            }

            entity.setTarget((LivingEntity)null);
        } else if (g >= (double)8.0F && !target.hasStatusEffect(StatusEffects.SLOWNESS) && isEnemy(target)) {
            potion = Potions.SLOWNESS;
        }
        else if (g <= (double)3.0F && !target.hasStatusEffect(StatusEffects.WEAKNESS) && entity.getRandom().nextFloat() < 0.25F && isEnemy(target)) {
            potion = Potions.WEAKNESS;
        }
        else if (g <= (double)3.0F && target.hasStatusEffect(StatusEffects.WEAKNESS) && entity.getRandom().nextFloat() < 0.25F && isEnemy(target)) {
            if(target instanceof ZombieEntity) {
                potion = Potions.HEALING;
            }
        }

        //根据时运等级增加药水的数量和BUFF等级
        var fl = EntityEnchantmentTools.hasEnchantment(entity,EntityEnchantmentType.FORTUNE) ? EntityEnchantmentTools.getLevelFromNbt(Objects.requireNonNull(EntityEnchantmentTools.getEnchantmentNbtByType(entity, EntityEnchantmentType.FORTUNE))) : 0;
        var tMax = EntityEnchantmentTools.hasEnchantment(entity,EntityEnchantmentType.FORTUNE) ? 1 + fl : 1;
        var tMin = tMax > 2 ? tMax - 2 : 1;
        var t = tMax == 1 ? tMax : entity.getRandom().nextInt(tMax) + tMin;
        for (int i = 0; i < t; i++) {
            PotionEntity potionEntity = new PotionEntity(entity.getWorld(), entity);
            var p = new ItemStack(Items.SPLASH_POTION);
            PotionUtil.setPotion(p, potion);
            var ef = PotionUtil.getPotionEffects(p);
            if(ef.isEmpty()) return;
            var efi = (IStatusEffectInstanceMixin)ef.get(0);
            var lMin = fl > 2 ? fl - 2 : 1;
            var l = fl == 0 ? fl : entity.getRandom().nextInt(fl) + lMin;
            efi.setAmplifier(l);
            PotionUtil.setCustomPotionEffects(p,ef);
            potionEntity.setItem(p);
            potionEntity.setPitch(potionEntity.getPitch() - -20.0F);
            potionEntity.setVelocity(d, e + g * 0.2, f, 0.75F, 8.0F);
            entity.getWorld().spawnEntity(potionEntity);
        }

        if (!entity.isSilent()) {
            entity.getWorld().playSound((PlayerEntity)null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_SPLASH_POTION_THROW, entity.getSoundCategory(), 1.0F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
        }
    }
    private void resetNextResponseTime(VillagerEntity villagerEntity,long l){
        var el = EntityEnchantmentTools.hasEnchantment(villagerEntity,EntityEnchantmentType.EFFICIENCY) ? EntityEnchantmentTools.getLevelFromNbt(Objects.requireNonNull(EntityEnchantmentTools.getEnchantmentNbtByType(villagerEntity, EntityEnchantmentType.EFFICIENCY))) : 0;
        this.nextResponseTime = l + 200L - el * 5L;
        if(this.nextResponseTime <= 0)  this.nextResponseTime = 5;
    }
    protected boolean shouldKeepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        return ModClientConfig.isEnable_cleric_task;
    }

    //寻找可以丢药水的实体（最近）
    private LivingEntity findNearestThrowableEntity(VillagerEntity self){
        var minWorldPos = self.getBlockPos().add(-40, -2, -40);
        var maxWorldPos = self.getBlockPos().add(40, 5, 40);
        Vec3d vec1 = new Vec3d(minWorldPos.getX(), minWorldPos.getY(), minWorldPos.getZ());
        Vec3d vec2 = new Vec3d(maxWorldPos.getX(), maxWorldPos.getY(), maxWorldPos.getZ());
        var minDistance = 9999f;
        LivingEntity nearestTarget = null;
        var targets = self.getWorld().getEntitiesByClass(LivingEntity.class, new Box(vec1, vec2), entity -> entity.getHealth() < entity.getMaxHealth() &&  entity != self || (entity instanceof PlayerEntity && entity.getName().toString().equals("CunMin18")));
        if(targets.isEmpty()) return null;
        for(var v : targets){
            if(v == null) continue;
            if(minDistance > v.distanceTo(self)) {
                minDistance = v.distanceTo(self);
                nearestTarget = v;
            }
        }
        return nearestTarget;
    }
    private boolean isEnemy(LivingEntity target){
        return target instanceof RaiderEntity || target instanceof ZombieEntity;
    }
    private boolean isFriend(LivingEntity target){
        return target instanceof IronGolemEntity || target instanceof SnowGolemEntity || target instanceof VillagerEntity;
    }
}
