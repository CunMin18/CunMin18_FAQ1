package org.cunmin18.cunmin18_faq1.villager_tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.cunmin18.cunmin18_faq1.commands.EnchantmentSettingCommand;
import org.cunmin18.cunmin18_faq1.config.ModClientConfig;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;
import org.cunmin18.cunmin18_faq1.utils.EntityEnchantmentTools;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class LibrarianVillagerTask extends MultiTickTask<VillagerEntity> {
    @Nullable
    private LivingEntity currentTarget;
    private long nextResponseTime;
    private final List<BlockPos> targetPositions = Lists.newArrayList();
    public LibrarianVillagerTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT));
    }
    protected boolean shouldRun(ServerWorld serverWorld, VillagerEntity villagerEntity) {
        return ModClientConfig.isEnable_librarian_task;
    }
    protected void run(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        this.currentTarget = this.findEnchantableTarget(villagerEntity);
        if (l > this.nextResponseTime && this.currentTarget != null) {
            villagerEntity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(this.currentTarget.getBlockPos()));
            villagerEntity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosLookTarget(this.currentTarget.getBlockPos()), 0.5F, 1));
        }
    }

    protected void finishRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        villagerEntity.getBrain().forget(MemoryModuleType.LOOK_TARGET);
        villagerEntity.getBrain().forget(MemoryModuleType.WALK_TARGET);
        this.nextResponseTime = l + 40L;
    }

    //寻找并附魔可附魔对象
    protected void keepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        this.currentTarget = this.findEnchantableTarget(villagerEntity);
        if (this.currentTarget != null && this.currentTarget.getPos().distanceTo(villagerEntity.getPos()) <= 3.0D) {
            if (l > this.nextResponseTime) {
                villagerEntity.setStackInHand(Hand.MAIN_HAND, Items.ANVIL.getDefaultStack());
                EntityEnchantmentType[] types = EntityEnchantmentType.values();
                Random random = new Random();
                var temp = random.nextInt(types.length - 1) + 1;
                //随机附魔类型（铁傀儡只附魔耐久）
                var randomEnchantmentType = this.currentTarget instanceof IronGolemEntity ? EntityEnchantmentType.UNBREAKING : types[temp];
                var efficiencyEnchantmentNBT = EntityEnchantmentTools.getEnchantmentNbtByType(villagerEntity,EntityEnchantmentType.EFFICIENCY);
                var efficiencyEnchantmentLevel = efficiencyEnchantmentNBT == null ? 0 : EntityEnchantmentTools.getLevelFromNbt(efficiencyEnchantmentNBT);
                //效率附魔可以增加图书管理员附魔村民的附魔等级
                var levelMax = 3 + efficiencyEnchantmentLevel;
                var levelMin = 1 + efficiencyEnchantmentLevel;
                var randomLevel = random.nextInt(levelMax) + levelMin;

                float g = 1.0F + (villagerEntity.getRandom().nextFloat() - villagerEntity.getRandom().nextFloat()) * 0.2F;
                villagerEntity.playSound(SoundEvents.BLOCK_ANVIL_USE, 1.0F, g);

                enchantVillager(randomEnchantmentType,randomLevel);

                this.targetPositions.remove(this.currentTarget);
                this.currentTarget = this.findEnchantableTarget(villagerEntity);
                if (this.currentTarget != null) {
                    this.nextResponseTime = l + 20L;
                    villagerEntity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosLookTarget(this.currentTarget.getBlockPos().up()), 0.5F, 1));
                    villagerEntity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(this.currentTarget.getBlockPos().up()));
                }
            }
        }
        else if(this.currentTarget != null){
            villagerEntity.setStackInHand(Hand.MAIN_HAND, Items.ANVIL.getDefaultStack());
            villagerEntity.getNavigation().startMovingTo(currentTarget, 0.8F);
        }
    }

    protected boolean shouldKeepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        return ModClientConfig.isEnable_librarian_task;
    }
    private VillagerEntity findNearestEnchantmentableVillagerEntity(VillagerEntity self){
        var minWorldPos = self.getBlockPos().add(-40, -2, -40);
        var maxWorldPos = self.getBlockPos().add(40, 5, 40);
        Vec3d vec1 = new Vec3d(minWorldPos.getX(), minWorldPos.getY(), minWorldPos.getZ());
        Vec3d vec2 = new Vec3d(maxWorldPos.getX(), maxWorldPos.getY(), maxWorldPos.getZ());
        var minDistance = 9999f;
        VillagerEntity nearestVillager = null;
        var targets = self.getWorld().getEntitiesByClass(VillagerEntity.class, new Box(vec1, vec2), entity -> (!EntityEnchantmentTools.hasEnchantments(entity)) && !entity.isBaby() && entity != self);
        if(targets.isEmpty()) return null;
        for(var v : targets){
            if(v == null) continue;
            if(minDistance > v.distanceTo(self)) {
                minDistance = v.distanceTo(self);
                nearestVillager = v;
            }
        }
        return nearestVillager;
    }
    private IronGolemEntity findNearestEnchantmentableIronGolemEntity(VillagerEntity self){
        var minWorldPos = self.getBlockPos().add(-40, -2, -40);
        var maxWorldPos = self.getBlockPos().add(40, 5, 40);
        Vec3d vec1 = new Vec3d(minWorldPos.getX(), minWorldPos.getY(), minWorldPos.getZ());
        Vec3d vec2 = new Vec3d(maxWorldPos.getX(), maxWorldPos.getY(), maxWorldPos.getZ());
        var minDistance = 9999f;
        IronGolemEntity nearestTarget = null;
        var targets = self.getWorld().getEntitiesByClass(IronGolemEntity.class, new Box(vec1, vec2), entity -> (!EntityEnchantmentTools.hasEnchantments(entity)));
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
    //awa
    private PlayerEntity findCunMin18(VillagerEntity self){
        var minWorldPos = self.getBlockPos().add(-40, -2, -40);
        var maxWorldPos = self.getBlockPos().add(40, 5, 40);
        Vec3d vec1 = new Vec3d(minWorldPos.getX(), minWorldPos.getY(), minWorldPos.getZ());
        Vec3d vec2 = new Vec3d(maxWorldPos.getX(), maxWorldPos.getY(), maxWorldPos.getZ());
        var minDistance = 9999f;
        PlayerEntity nearestTarget = null;
        var targets = self.getWorld().getEntitiesByClass(PlayerEntity.class, new Box(vec1, vec2), entity -> (!EntityEnchantmentTools.hasEnchantments(entity)) && entity.getName().toString().equals("CunMin18"));
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
    private LivingEntity findEnchantableTarget(VillagerEntity self){
        var i = findNearestEnchantmentableIronGolemEntity(self);
        if(i != null) return i;
        else{
            var v = findNearestEnchantmentableVillagerEntity(self);
            if(v != null) return v;
            return findCunMin18(self);
        }
    }
    private void enchantVillager(EntityEnchantmentType type,int level){
        if(currentTarget == null) return;
        EntityEnchantmentTools.addEnchantmentToEntity(currentTarget,type,level);
    }
}
