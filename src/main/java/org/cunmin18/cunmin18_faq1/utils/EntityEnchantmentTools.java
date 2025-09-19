package org.cunmin18.cunmin18_faq1.utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.MathHelper;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantment;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;

import java.util.*;
import java.util.function.Supplier;

public class EntityEnchantmentTools {

    //添加附魔
    public static void addEnchantmentToEntity(LivingEntity entity, EntityEnchantmentType type, int level){
        var entityMixin = (IEnchantableEntity)entity;
        //删除原有的附魔 直接覆盖
        if(hasEnchantment(entity,type)){
            if (getLevelFromNbt(Objects.requireNonNull(getEnchantmentNbtByType(entity, type))) >= level) return;
            removeEnchantmentFromEntity(entity,type);
        }
        var newEnchantmentList = new ArrayList<>(entityMixin.getEnchantments());
        newEnchantmentList.add(new EntityEnchantment(type,level));
        ((IEnchantableEntity)entity).setEnchantments(newEnchantmentList);
        ((IEnchantableEntity)entity).onEnchanted(type,level);

        System.out.println("[EntityEnchantmentTools] 添加附魔: " + type.name() + " 等级: " + level + " 到实体: " + entity.getName().getString());
    }

    //删除附魔
    public static void removeEnchantmentFromEntity(LivingEntity entity,EntityEnchantmentType type){
        var entityMixin = (IEnchantableEntity)entity;
        var toRemove = findEnchantmentByType(entityMixin.getEnchantments(),type);
        if(toRemove != null) {
            var newEnchantmentList = new ArrayList<>(entityMixin.getEnchantments());
            newEnchantmentList.remove(toRemove);
            ((IEnchantableEntity)entity).setEnchantments(newEnchantmentList);
            var level = toRemove.getEnchantmentLevel();
            ((IEnchantableEntity)entity).onRemove(type,level);
            System.out.println("[EntityEnchantmentTools] 移除附魔: " + type.name() + " 从实体: " + entity.getName().getString());
        }
    }

    //删除全部附魔
    public static void removeAllEnchantmentsFromEntity(LivingEntity entity){
        var es = ((IEnchantableEntity)entity).getEnchantments();
        for (int i = 0; i < es.size(); i++) {
            var enchantment = es.get(i);
            removeEnchantmentFromEntity(entity,enchantment.getEnchantmentType());
        }
        System.out.println("[EntityEnchantmentTools] 删除了实体全部附魔!");
    }

    //是否有附魔
    public static boolean hasEnchantments(LivingEntity entity){
        var nbt = getEnchantmentNbtList(entity);
        return !nbt.isEmpty();
    }

    //是否有某种附魔
    public static boolean hasEnchantment(LivingEntity entity,EntityEnchantmentType type){
        var nbtList = getEnchantmentNbtList(entity);
        for (int i = 0; i < nbtList.size(); i++) {
            var nc = nbtList.getCompound(i);
            if(getTypeFromNbt(nc).equals(type)) return true;
        }
        return false;
    }

    //通过Type寻找附魔
    public static EntityEnchantment findEnchantmentByType(List<EntityEnchantment> enchantmentList, EntityEnchantmentType type){
        for(var enchantment : enchantmentList){
            if(enchantment == null) {
                continue;
            }
            if(enchantment.getEnchantmentType().equals(EntityEnchantmentType.NONE)) {
                enchantmentList.remove(enchantment);
                continue;
            }
            if(enchantment.getEnchantmentType().equals(type)) return enchantment;
        }
        return null;
    }

    //得到附魔的Nbt列表
    public static NbtList getEnchantmentNbtList(LivingEntity entity) {
        var entityNBT = new NbtCompound();
        entity.writeCustomDataToNbt(entityNBT);
        return entityNBT.getList("entity_enchantments", 10);
    }

    //从Nbt得到附魔等级
    public static int getLevelFromNbt(NbtCompound nbt) {
        return MathHelper.clamp(nbt.getInt("level"), 0, 255);
    }

    //从Nbt得到附魔类型
    public static EntityEnchantmentType getTypeFromNbt(NbtCompound nbt) {
        return EntityEnchantmentType.valueOf(nbt.getString("type"));
    }

    //通过类型得到附魔Nbt
    public static NbtCompound getEnchantmentNbtByType(LivingEntity entity,EntityEnchantmentType type){
        var nbtList = getEnchantmentNbtList(entity);
        for (int i = 0; i < nbtList.size(); i++) {
            var nc = nbtList.getCompound(i);
            if(getTypeFromNbt(nc).equals(type)) return nc;
        }
        return null;
    }

    //映射工具
    public static class EnumObjectMapper {
        private static final Map<EntityEnchantmentType, Enchantment> ENCHANTMENT_MAP = new HashMap<>();
        static {
            register(EntityEnchantmentType.FORTUNE, () -> Enchantments.FORTUNE);
            register(EntityEnchantmentType.EFFICIENCY, () -> Enchantments.EFFICIENCY);
            register(EntityEnchantmentType.LOYALTY, () -> Enchantments.LOYALTY);
            register(EntityEnchantmentType.UNBREAKING, () -> Enchantments.UNBREAKING);
            register(EntityEnchantmentType.FIRE_ASPECT, () -> Enchantments.FIRE_ASPECT);
        }
        private static void register(EntityEnchantmentType enumValue, Supplier<Enchantment> objectSupplier) {
            ENCHANTMENT_MAP.put(enumValue, objectSupplier.get());
        }

        public static Enchantment convert(EntityEnchantmentType enumValue) {
            return ENCHANTMENT_MAP.getOrDefault(enumValue, null);
        }
    }
}
