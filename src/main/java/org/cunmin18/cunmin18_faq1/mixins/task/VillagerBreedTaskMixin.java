package org.cunmin18.cunmin18_faq1.mixins.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.VillagerBreedTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestType;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;
import org.cunmin18.cunmin18_faq1.utils.EntityEnchantmentTools;
import org.cunmin18.cunmin18_faq1.utils.IEnchantableEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(VillagerBreedTask.class)
public class VillagerBreedTaskMixin {
    @Inject(method = "canReachHome",at = @At("HEAD"),cancellable = true)
    private void canReachHome(VillagerEntity villager, BlockPos pos, RegistryEntry<PointOfInterestType> poiType, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        cir.cancel();
    }
    @Shadow
    private Optional<BlockPos> getReachableHome(ServerWorld world, VillagerEntity villager){
        return null;
    };
    @Shadow
    private void setChildHome(ServerWorld world, VillagerEntity child, BlockPos pos){
    };
    @Shadow
    private Optional<VillagerEntity> createChild(ServerWorld world, VillagerEntity parent, VillagerEntity partner){
        return null;
    };

    @Inject(method = "goHome",at = @At("HEAD"),cancellable = true)
    private void goHome(ServerWorld world, VillagerEntity first, VillagerEntity second, CallbackInfo ci) {
        Optional<BlockPos> optional = getReachableHome(world, first);
        if (!optional.isPresent()) {
            world.sendEntityStatus(second, (byte)13);
            world.sendEntityStatus(first, (byte)13);
            Optional<VillagerEntity> optional2 = createChild(world, first, second);
        } else {
            Optional<VillagerEntity> optional2 = createChild(world, first, second);
            if (optional2.isPresent()) {
                setChildHome(world, (VillagerEntity)optional2.get(), (BlockPos)optional.get());
            }
            else {
                world.getPointOfInterestStorage().releaseTicket((BlockPos)optional.get());
                DebugInfoSender.sendPointOfInterest(world, (BlockPos)optional.get());
            }
        }
        ci.cancel();
    }

    //重写了繁殖的方法，新增附魔识别，根据时运等级增加生成村民数量，根据效率等级减少繁殖冷却
    @Inject(method = "createChild",at = @At("HEAD"),cancellable = true)
    private void createChildMixin(ServerWorld world, VillagerEntity parent, VillagerEntity partner, CallbackInfoReturnable<Optional<VillagerEntity>> ci) {

        var parentFL = getEnchantmentLevel(parent,EntityEnchantmentType.FORTUNE);
        var partnerFL = getEnchantmentLevel(partner,EntityEnchantmentType.FORTUNE);
        var mFl = Math.max(parentFL,partnerFL);

        var parentEL = getEnchantmentLevel(parent,EntityEnchantmentType.EFFICIENCY);
        var partnerEL = getEnchantmentLevel(partner,EntityEnchantmentType.EFFICIENCY);
        var mEl = Math.max(parentEL,partnerEL);

        var pA = 6000 - mEl * 200;
        if(pA < 0) pA = 0;
        parent.setBreedingAge(pA);
        partner.setBreedingAge(pA);

        var cA = -24000 + mEl * 200;
        if(cA > 0) cA = 0;
        for (int i = 0; i < 1 + mFl; i++) {
            VillagerEntity villagerEntity = parent.createChild(world, partner);

            if (villagerEntity == null) {
                ci.setReturnValue(Optional.empty());
                ci.cancel();
            }

            if (villagerEntity != null) {
                villagerEntity.setBreedingAge(cA);
                villagerEntity.refreshPositionAndAngles(parent.getX(), parent.getY(), parent.getZ(), 0.0F, 0.0F);
                extendParentEnchantments(parent,partner,villagerEntity);
                world.spawnEntityAndPassengers(villagerEntity);
                world.sendEntityStatus(villagerEntity, (byte)12);
                if(i == mFl){
                    ci.setReturnValue(Optional.of(villagerEntity));
                    ci.cancel();
                }
            }
        }
    }

    //让小村民继承父母的附魔，所有附魔等级取父母本的最大值
    @Unique
    private void extendParentEnchantments(VillagerEntity father, VillagerEntity mother, VillagerEntity baby){
        List<EntityEnchantmentType> parentEnchantmentType = new ArrayList<>();
        parentEnchantmentType.addAll(getEntityEnchantmentTypeList(father));
        parentEnchantmentType.addAll(getEntityEnchantmentTypeList(mother));
        var extendEnchantmentType = parentEnchantmentType.stream().distinct().toList();
        for (var type : extendEnchantmentType) {
            var fatherEL = getEnchantmentLevel(father, type);
            var motherEL = getEnchantmentLevel(mother, type);
            var ml = Math.max(fatherEL, motherEL);
            if(ml > 0) EntityEnchantmentTools.addEnchantmentToEntity(baby,type,ml);
        }
    }

    @Unique
    private List<EntityEnchantmentType> getEntityEnchantmentTypeList(LivingEntity livingEntity){
        List<EntityEnchantmentType> result = new ArrayList<>();
        for (int i = 0; i < ((IEnchantableEntity)livingEntity).getEnchantments().size(); i++) {
            var type = ((IEnchantableEntity)livingEntity).getEnchantments().get(i).getEnchantmentType();
            result.add(type);
        }
        return result;
    }
    @Unique
    private int getEnchantmentLevel(LivingEntity entity,EntityEnchantmentType type){
        var hasEnchantment = EntityEnchantmentTools.hasEnchantment(entity, type);
        var nbt = EntityEnchantmentTools.getEnchantmentNbtByType(entity, type);
        return hasEnchantment && nbt != null ? EntityEnchantmentTools.getLevelFromNbt(nbt) : 0;
    }
}
