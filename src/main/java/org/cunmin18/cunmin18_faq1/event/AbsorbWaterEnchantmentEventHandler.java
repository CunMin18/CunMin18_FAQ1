package org.cunmin18.cunmin18_faq1.event;


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.cunmin18.cunmin18_faq1.commands.EnchantmentSettingCommand;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;
import org.cunmin18.cunmin18_faq1.utils.EntityEnchantmentTools;
import org.cunmin18.cunmin18_faq1.utils.ILivingEntity;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

public class AbsorbWaterEnchantmentEventHandler implements ServerTickEvents.EndWorldTick{
    @Override
    public void onEndTick(ServerWorld world) {
        //每Tick检测带吸水附魔实体附近有无水或岩浆，如果有，则根据情况吸水
        for(ServerPlayerEntity player : world.getPlayers()){
            var minWorldPos = player.getBlockPos().add(-40, -20, -40);
            var maxWorldPos = player.getBlockPos().add(40, 20, 40);
            Vec3d vec1 = new Vec3d(minWorldPos.getX(), minWorldPos.getY(), minWorldPos.getZ());
            Vec3d vec2 = new Vec3d(maxWorldPos.getX(), maxWorldPos.getY(), maxWorldPos.getZ());
            for(LivingEntity livingEntity : world.getEntitiesByClass(LivingEntity.class,new Box(vec1,vec2),entity -> EntityEnchantmentTools.hasEnchantments(entity) && (isInWater(entity) || isInLava(entity)))){
                if(EntityEnchantmentTools.hasEnchantment(livingEntity,EntityEnchantmentType.ABSORBENT)){
                    //是否有火焰附加附魔，如果有，就可以吸收岩浆
                    var containFireAspect = EntityEnchantmentTools.hasEnchantment(livingEntity,EntityEnchantmentType.FIRE_ASPECT);
                    var al = getAbsorbentEnchantmentLevel(livingEntity);
                    var poses = getBlocksPosNear(livingEntity.getBlockPos(),2 + al);
                    for(var pos : poses){
                        FluidState fluidState = world.getFluidState(pos);
                        if(fluidState.isIn(FluidTags.WATER) || (fluidState.isIn(FluidTags.LAVA) && containFireAspect)){
                            world.setBlockState(pos, Blocks.AIR.getDefaultState(),3);
                            livingEntity.setHealth(livingEntity.getHealth() + 1);
                            if(livingEntity.getHealth() > livingEntity.getMaxHealth()){
                                livingEntity.setHealth(livingEntity.getMaxHealth());
                            }
                        }
                    }
                }
            }
        }
    }
    //实体在水中
    private boolean isInWater(LivingEntity entity){
        return entity.getWorld().getFluidState(entity.getBlockPos()).isIn(FluidTags.WATER);
    }
    //实体在岩浆中
    private boolean isInLava(LivingEntity entity){
        return entity.getWorld().getFluidState(entity.getBlockPos()).isIn(FluidTags.LAVA);
    }
    //得到吸水附魔的等级
    private int getAbsorbentEnchantmentLevel(LivingEntity entity){
        var hasEfficiency = EntityEnchantmentTools.hasEnchantment(entity, EntityEnchantmentType.ABSORBENT);
        var fortuneNBT = EntityEnchantmentTools.getEnchantmentNbtByType(entity, EntityEnchantmentType.ABSORBENT);
        return hasEfficiency && fortuneNBT != null ? EntityEnchantmentTools.getLevelFromNbt(fortuneNBT) : 0;
    }
    //得到一定范围内的所有方块位置
    public List<BlockPos> getBlocksPosNear(BlockPos entityPos, int radius) {
        List<BlockPos> blocksPos = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos targetPos = entityPos.add(x, y, z);
                    blocksPos.add(targetPos);
                }
            }
        }
        return blocksPos;
    }
}
