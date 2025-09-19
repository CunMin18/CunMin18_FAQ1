package org.cunmin18.cunmin18_faq1.mixins.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;
import org.cunmin18.cunmin18_faq1.utils.EntityEnchantmentTools;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(Block.class)
public class BlockMixin {
    @Shadow
    public static List<ItemStack> getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity) {
        LootContextParameterSet.Builder builder = (new LootContextParameterSet.Builder(world)).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).add(LootContextParameters.TOOL, ItemStack.EMPTY).addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity);
        return state.getDroppedStacks(builder);
    }
    @Shadow
    public static void dropStack(World world, BlockPos pos, ItemStack stack) {
        double d = (double) EntityType.ITEM.getHeight() / (double)2.0F;
        double e = (double)pos.getX() + (double)0.5F + MathHelper.nextDouble(world.random, (double)-0.25F, (double)0.25F);
        double f = (double)pos.getY() + (double)0.5F + MathHelper.nextDouble(world.random, (double)-0.25F, (double)0.25F) - d;
        double g = (double)pos.getZ() + (double)0.5F + MathHelper.nextDouble(world.random, (double)-0.25F, (double)0.25F);
        dropStack(world, (Supplier)(() -> new ItemEntity(world, e, f, g, stack)), stack);
    }
    @Shadow
    private static void dropStack(World world, Supplier<ItemEntity> itemEntitySupplier, ItemStack stack) {
        if (!world.isClient && !stack.isEmpty() && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            ItemEntity itemEntity = (ItemEntity)itemEntitySupplier.get();
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);
        }
    }

    @Inject(
            method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void dropStacks(BlockState state, World world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack tool,CallbackInfo ci) {
        //我重写了物品掉落的方法，这里会根据实体的时运附魔等级来给掉落物翻倍
        if (world instanceof ServerWorld && entity instanceof LivingEntity livingEntity) {
            getDroppedStacks(state, (ServerWorld)world, pos, blockEntity).forEach((stack) -> {
                var hasFortune = EntityEnchantmentTools.hasEnchantment(livingEntity, EntityEnchantmentType.FORTUNE);
                var fortuneNBT = EntityEnchantmentTools.getEnchantmentNbtByType(livingEntity, EntityEnchantmentType.FORTUNE);
                var c = hasFortune && fortuneNBT != null ? EntityEnchantmentTools.getLevelFromNbt(fortuneNBT) : 0;
                stack.setCount(stack.getCount() + c);
                dropStack((ServerWorld) world, pos, stack);
            });
            state.onStacksDropped((ServerWorld)world, pos, tool, true);
        }
        ci.cancel();
    }
}
