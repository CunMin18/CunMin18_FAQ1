package org.cunmin18.cunmin18_faq1.utils;

import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantment;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;

import java.util.List;

public interface IEnchantableEntity {
    List<EntityEnchantment> getEnchantments();
    void setEnchantments(List<EntityEnchantment> list);
    boolean isEnchanted();
    void setMaxHealth(int maxHealth);
    void onEnchanted(EntityEnchantmentType type,int level);
    void onRemove(EntityEnchantmentType type,int level);
}
