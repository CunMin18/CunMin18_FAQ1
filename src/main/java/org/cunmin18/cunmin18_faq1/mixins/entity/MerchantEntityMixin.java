package org.cunmin18.cunmin18_faq1.mixins.entity;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.village.TradeOfferList;
import org.cunmin18.cunmin18_faq1.utils.IMerchantEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MerchantEntity.class)
public abstract class MerchantEntityMixin implements IMerchantEntity {
    @Shadow
    public abstract TradeOfferList getOffers();
}
