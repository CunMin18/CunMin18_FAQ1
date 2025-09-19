package org.cunmin18.cunmin18_faq1.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;
import org.cunmin18.cunmin18_faq1.utils.EntityEnchantmentTools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "isFireImmune",at = @At("HEAD"),cancellable = true)
    private void isFireImmune(CallbackInfoReturnable<Boolean> ci){
        //重写防火方法，会判断实体是否包含火焰附加附魔，有则防火
        var entity = (Entity)(Object)this;
        if(entity instanceof LivingEntity livingEntity){
            ci.setReturnValue(EntityEnchantmentTools.hasEnchantment(livingEntity, EntityEnchantmentType.FIRE_ASPECT) || entity.getType().isFireImmune());
        }
        else{
            ci.setReturnValue(entity.getType().isFireImmune());
        }
        ci.cancel();
    }
}
