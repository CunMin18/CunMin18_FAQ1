package org.cunmin18.cunmin18_faq1.mixins.entity;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;
import org.cunmin18.cunmin18_faq1.utils.EntityEnchantmentTools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    //附魔书右键实体 可以给实体添加附魔
    @Inject(method = "interactMob",at = @At("HEAD"),cancellable = true)
    private void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
        ItemStack itemStack = player.getStackInHand(hand);
        var mobEntity = (MobEntity)(Object)this;
        if (itemStack.isOf(Items.ENCHANTED_BOOK) && mobEntity.isAlive() && !mobEntity.isSleeping()) {
            var success = enchantMob(itemStack);
            if(success){
                float g = 1.0F + (mobEntity.getRandom().nextFloat() - mobEntity.getRandom().nextFloat()) * 0.2F;
                mobEntity.playSound(SoundEvents.BLOCK_ANVIL_USE, 1.0F, g);
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }
                System.out.println("successfully enchanted the mob with an enchanted book!");
            }
            else{
                System.out.println("Unsupported enchantment!");
            }
            ci.setReturnValue(ActionResult.success(mobEntity.getWorld().isClient));
            ci.cancel();
        }
    }
    //判断附魔书是否可以用于附魔（取有效附魔）
    @Unique
    private boolean hasAvailableEnchantments(ItemStack itemStack){
        return     hasEnchantment(itemStack, Enchantments.EFFICIENCY)
                || hasEnchantment(itemStack,Enchantments.LOYALTY)
                || hasEnchantment(itemStack,Enchantments.FORTUNE)
                || hasEnchantment(itemStack,Enchantments.UNBREAKING)
                || hasEnchantment(itemStack,Enchantments.FIRE_ASPECT);
    }

    //物品拥有某个附魔
    @Unique
    private boolean hasEnchantment(ItemStack itemStack, Enchantment enchantment){

        //注意：附魔书和普通物品的附魔nbt是不同的，所以判断方法有区别！

        //附魔书
        if (itemStack.getItem() instanceof EnchantedBookItem) {
            NbtList storedEnchantments = EnchantedBookItem.getEnchantmentNbt(itemStack);
            for (int i = 0; i < storedEnchantments.size(); i++) {
                var nc = storedEnchantments.getCompound(i);
                var id1 = EnchantmentHelper.getIdFromNbt(nc);
                var id2 = EnchantmentHelper.getEnchantmentId(enchantment);
                if (id1 != null && id1.equals(id2)) {
                    return true;
                }
            }
            return false;
        }
        //普通物品
        else {
            return EnchantmentHelper.getLevel(enchantment, itemStack) > 0;
        }
    }

    //将实体附魔
    @Unique
    private boolean enchantMob(ItemStack itemStack){
        var mobEntity = (MobEntity)(Object)this;
        var enchantedCount = 0;
        if(hasAvailableEnchantments(itemStack)){
            EntityEnchantmentType[] types = EntityEnchantmentType.values();
            for(var ee : types){
                if(ee.equals(EntityEnchantmentType.NONE)) continue;
                final var e = EntityEnchantmentTools.EnumObjectMapper.convert(ee);
                if(hasEnchantment(itemStack,e)){
                    var h = EntityEnchantmentTools.hasEnchantment(mobEntity,ee);
                    if(!h){
                        var level = getEnchantmentLevel(itemStack,e);
                        EntityEnchantmentTools.addEnchantmentToEntity(mobEntity,ee,level);
                        enchantedCount++;
                    }
                    else{
                        var nbt = EntityEnchantmentTools.getEnchantmentNbtByType(mobEntity,ee);
                        if (nbt != null) {
                            var level = getEnchantmentLevel(itemStack,e);
                            var enchantable = !h || (h && EntityEnchantmentTools.getLevelFromNbt(nbt) < level);
                            if(enchantable) {
                                EntityEnchantmentTools.addEnchantmentToEntity(mobEntity,ee,level);
                                enchantedCount++;
                            }
                        }
                    }
                }
            }
        }
        return enchantedCount > 0;
    }
    //得到物品某个附魔的附魔等级
    @Unique
    private int getEnchantmentLevel(ItemStack itemStack, Enchantment enchantment){
        NbtList storedEnchantments = EnchantedBookItem.getEnchantmentNbt(itemStack);
        for (int i = 0; i < storedEnchantments.size(); i++) {
            var nc = storedEnchantments.getCompound(i);
            var id1 = EnchantmentHelper.getIdFromNbt(nc);
            var id2 = EnchantmentHelper.getEnchantmentId(enchantment);
            if (id1 != null && id1.equals(id2)) {
                return EnchantmentHelper.getLevelFromNbt(nc);
            }
        }
        return 0;
    }
}
