package org.cunmin18.cunmin18_faq1.mixins.status_effect;

import net.minecraft.entity.effect.StatusEffectInstance;
import org.cunmin18.cunmin18_faq1.utils.IStatusEffectInstanceMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(StatusEffectInstance.class)
public class StatusEffectInstanceMixin implements IStatusEffectInstanceMixin {
    @Shadow
    private int amplifier;

    //新增功能：设置效果等级
    @Unique
    public void setAmplifier(int level){
        amplifier = level;
    }
}
