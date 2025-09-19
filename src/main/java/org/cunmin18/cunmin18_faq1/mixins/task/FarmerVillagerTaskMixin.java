package org.cunmin18.cunmin18_faq1.mixins.task;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.FarmerVillagerTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;
import org.cunmin18.cunmin18_faq1.utils.EntityEnchantmentTools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(FarmerVillagerTask.class)
public class FarmerVillagerTaskMixin {
    @Shadow
    private BlockPos currentTarget;
    @Shadow
    private long nextResponseTime;
    @Shadow
    private int ticksRan;
    @Shadow
    private final List<BlockPos> targetPositions = Lists.newArrayList();

    @Shadow
    private BlockPos chooseRandomTarget(ServerWorld world) {
        return this.targetPositions.isEmpty() ? null : (BlockPos)this.targetPositions.get(world.getRandom().nextInt(this.targetPositions.size()));
    }

    @Inject(method = "finishRunning*",at = @At("HEAD"),cancellable = true)
    protected void finishRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l,CallbackInfo ci) {
        villagerEntity.getBrain().forget(MemoryModuleType.LOOK_TARGET);
        villagerEntity.getBrain().forget(MemoryModuleType.WALK_TARGET);
        this.ticksRan = 0;
        resetNextResponseTime(villagerEntity,l);
        ci.cancel();
    }

    //重写了农民种地的逻辑，这里会根据效率附魔等级加快农民思考速度
    @Inject(method = "keepRunning*",at = @At("HEAD"),cancellable = true)
    protected void keepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l, CallbackInfo ci) {
        if (this.currentTarget == null || this.currentTarget.isWithinDistance(villagerEntity.getPos(), (double)1.0F)) {
            if (this.currentTarget != null && l > this.nextResponseTime) {
                BlockState blockState = serverWorld.getBlockState(this.currentTarget);
                Block block = blockState.getBlock();
                Block block2 = serverWorld.getBlockState(this.currentTarget.down()).getBlock();
                if (block instanceof CropBlock && ((CropBlock)block).isMature(blockState)) {
                    serverWorld.breakBlock(this.currentTarget, true, villagerEntity);
                }

                if (blockState.isAir() && block2 instanceof FarmlandBlock && villagerEntity.hasSeedToPlant()) {
                    SimpleInventory simpleInventory = villagerEntity.getInventory();

                    for(int i = 0; i < simpleInventory.size(); ++i) {
                        ItemStack itemStack = simpleInventory.getStack(i);
                        boolean bl = false;
                        if (!itemStack.isEmpty() && itemStack.isIn(ItemTags.VILLAGER_PLANTABLE_SEEDS)) {
                            Item var13 = itemStack.getItem();
                            if (var13 instanceof BlockItem) {
                                BlockItem blockItem = (BlockItem)var13;
                                BlockState blockState2 = blockItem.getBlock().getDefaultState();
                                serverWorld.setBlockState(this.currentTarget, blockState2);
                                serverWorld.emitGameEvent(GameEvent.BLOCK_PLACE, this.currentTarget, GameEvent.Emitter.of(villagerEntity, blockState2));
                                bl = true;
                            }
                        }

                        if (bl) {
                            serverWorld.playSound((PlayerEntity)null, (double)this.currentTarget.getX(), (double)this.currentTarget.getY(), (double)this.currentTarget.getZ(), SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                            itemStack.decrement(1);
                            if (itemStack.isEmpty()) {
                                simpleInventory.setStack(i, ItemStack.EMPTY);
                            }
                            break;
                        }
                    }
                }

                if (block instanceof CropBlock && !((CropBlock)block).isMature(blockState)) {
                    this.targetPositions.remove(this.currentTarget);
                    this.currentTarget = this.chooseRandomTarget(serverWorld);
                    if (this.currentTarget != null) {
                        resetNextResponseTime(villagerEntity,l);
                        villagerEntity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosLookTarget(this.currentTarget), 0.5F, 1));
                        villagerEntity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(this.currentTarget));
                    }
                }
            }

            ++this.ticksRan;
            ci.cancel();
        }
    }

    @Unique
    private void resetNextResponseTime(VillagerEntity villagerEntity,long l){
        var el = getEfficiencyEnchantmentLevel(villagerEntity);
        this.nextResponseTime = (long) (l + 20L - el * 0.5);
        if(this.nextResponseTime <= 0)  this.nextResponseTime = 5;
    }
    @Unique
    private int getEfficiencyEnchantmentLevel(VillagerEntity entity){
        var hasEfficiency = EntityEnchantmentTools.hasEnchantment(entity, EntityEnchantmentType.EFFICIENCY);
        var fortuneNBT = EntityEnchantmentTools.getEnchantmentNbtByType(entity, EntityEnchantmentType.EFFICIENCY);
        return hasEfficiency && fortuneNBT != null ? EntityEnchantmentTools.getLevelFromNbt(fortuneNBT) : 0;
    }
}
