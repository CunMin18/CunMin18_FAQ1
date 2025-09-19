package org.cunmin18.cunmin18_faq1.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.cunmin18.cunmin18_faq1.utils.IEnchantableEntity;

@Environment(EnvType.CLIENT)
public class EnchantGlintFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
        extends FeatureRenderer<T, M> {

    public EnchantGlintFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        //如果有附魔 继续渲染
        boolean hasEnchantments = ((IEnchantableEntity)entity).isEnchanted();
        if(hasEnchantments) {
            this.getContextModel().render(matrices, vertexConsumers.getBuffer(RenderLayer.getGlintTranslucent()), light, OverlayTexture.DEFAULT_UV, 0.5F, 0.5F, 0.5F, 1.0F);
        }
    }
}