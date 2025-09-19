package org.cunmin18.cunmin18_faq1.entity_enchantments;

public class EntityEnchantment {
    //附魔类型
    private EntityEnchantmentType enchantmentType;
    //附魔等级
    private int enchantmentLevel;
    public EntityEnchantment(EntityEnchantmentType enchantmentType,int enchantmentLevel){
        this.enchantmentLevel = enchantmentLevel;
        this.enchantmentType = enchantmentType;
    }
    //得到附魔等级
    public int getEnchantmentLevel(){
        return enchantmentLevel;
    }
    //设置附魔等级
    public void setEnchantmentLevel(int level){
        enchantmentLevel = level;
    }
    //得到附魔类型
    public EntityEnchantmentType getEnchantmentType() {
        return enchantmentType;
    }
    //设置附魔类型
    public void setEnchantmentType(EntityEnchantmentType type){
        enchantmentType = type;
    }
}
