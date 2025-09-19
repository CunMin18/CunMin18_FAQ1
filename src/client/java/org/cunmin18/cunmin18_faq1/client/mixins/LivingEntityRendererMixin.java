package org.cunmin18.cunmin18_faq1.client.mixins;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.cunmin18.cunmin18_faq1.client.renderer.EnchantGlintFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {
    protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }
    @Shadow
    protected abstract boolean addFeature(FeatureRenderer<T, M> feature);

    @Inject(method = "<init>",at = @At("TAIL"))
    private void initRenderer(EntityRendererFactory.Context context, EntityModel entityModel, float f, CallbackInfo ci){
        var renderer = (LivingEntityRenderer)(Object)this;
        if(renderer == null) return;
        //添加附魔层
        addFeature(new EnchantGlintFeatureRenderer<>(renderer));
    }
}