package org.cunmin18.cunmin18_faq1.mixins.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantment;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;
import org.cunmin18.cunmin18_faq1.utils.IEnchantableEntity;
import org.cunmin18.cunmin18_faq1.tracked_datas.EntityEnchantmentListHandler;
import org.cunmin18.cunmin18_faq1.utils.ILivingEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements IEnchantableEntity,ILivingEntity {
    @Unique
    private final static TrackedData<List<EntityEnchantment>> ENCHANTMENT_LIST = DataTracker.registerData(LivingEntity.class, EntityEnchantmentListHandler.INSTANCE);

    @Shadow
    protected void jump(){};

    //实体的跳跃方法是Protected的 所以用这个方法使实体跳跃
    @Unique
    public void callJumpMethod(){
        jump();
    }

    //得到实体附魔列表
    @Override
    @Unique
    public List<EntityEnchantment> getEnchantments(){
        var l = (LivingEntity)(Object)this;
        return l.getDataTracker().get(ENCHANTMENT_LIST);
    }

    //设置实体的附魔列表
    @Override
    @Unique
    public void setEnchantments(List<EntityEnchantment> list){
        var l = (LivingEntity)(Object)this;
        l.getDataTracker().set(ENCHANTMENT_LIST,list);
    }

    //是否被附魔
    @Override
    @Unique
    public boolean isEnchanted() {
        var l = (LivingEntity)(Object)this;
        return !l.getDataTracker().get(ENCHANTMENT_LIST).isEmpty();
    }

    //设置生命上限
    @Override
    @Unique
    public void setMaxHealth(int maxHealth) {
        var l = (LivingEntity)(Object)this;
        EntityAttributeInstance healthAttribute = l.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(maxHealth);
            l.setHealth(maxHealth);
        }
    }

    //当附魔被添加
    @Override
    @Unique
    public void onEnchanted(EntityEnchantmentType type,int level) {
        var l = (LivingEntity)(Object)this;
        switch (type){
            case UNBREAKING:
                setMaxHealth(Math.round(l.getMaxHealth() * (1 + level * 0.1f)));
                break;
        }
    }

    //当附魔被移除
    @Override
    @Unique
    public void onRemove(EntityEnchantmentType type,int level) {
        var l = (LivingEntity)(Object)this;
        switch (type){
            case UNBREAKING:
                setMaxHealth(Math.round(l.getMaxHealth() / (1 + level * 0.1f)));
                break;
        }
    }

    //添加了附魔列表DataTracker，以实现附魔数据客户端服务端互通
    @Inject(method = "initDataTracker", at = @At("HEAD"))
    protected void initDataTracker(CallbackInfo ci) {
        var l = (LivingEntity)(Object)this;
        l.getDataTracker().startTracking(ENCHANTMENT_LIST,new ArrayList<>());
    }

    //将附魔列表的信息写入实体NBT（随时在调用，防止实体附魔随存档重载丢失）
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    protected void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        var l = (LivingEntity) (Object) this;
        NbtList enchantmentsNbt = new NbtList();
        for (EntityEnchantment enchantment : this.getEnchantments()) {
            if (enchantment != null && enchantment.getEnchantmentType() != EntityEnchantmentType.NONE) {
                NbtCompound enchantmentNbt = new NbtCompound();
                enchantmentNbt.putString("type", enchantment.getEnchantmentType().name());
                enchantmentNbt.putInt("level", enchantment.getEnchantmentLevel());
                enchantmentsNbt.add(enchantmentNbt);
            }
        }
        nbt.put("entity_enchantments", enchantmentsNbt);
    }

    //根据实体的nbt，将附魔写入附魔列表（加载存档时会调用，便于后面获取实体附魔）
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    protected void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        setEnchantments(new ArrayList<>());
        var l = (LivingEntity)(Object)this;
        if (nbt.contains("entity_enchantments", 9)) {
            NbtList enchantmentsNbt = nbt.getList("entity_enchantments", 10);
            var newEnchantmentList = new ArrayList<>(getEnchantments());
            for (int i = 0; i < enchantmentsNbt.size(); i++) {
                NbtCompound enchantmentNbt = enchantmentsNbt.getCompound(i);
                String typeName = enchantmentNbt.getString("type");
                int level = enchantmentNbt.getInt("level");
                EntityEnchantmentType type = EntityEnchantmentType.valueOf(typeName);
                newEnchantmentList.add(new EntityEnchantment(type, level));
            }
            setEnchantments(newEnchantmentList);
        }
    }
}
