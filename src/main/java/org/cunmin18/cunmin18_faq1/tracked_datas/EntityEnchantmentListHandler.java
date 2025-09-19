package org.cunmin18.cunmin18_faq1.tracked_datas;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantment;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;

import java.util.ArrayList;
import java.util.List;

public class EntityEnchantmentListHandler implements TrackedDataHandler<List<EntityEnchantment>> {
    public static final EntityEnchantmentListHandler INSTANCE = new EntityEnchantmentListHandler();
    @Override
    public void write(PacketByteBuf buf, List<EntityEnchantment> value) {
        buf.writeInt(value.size());
        for (var enchantment : value) {
            var enchantmentNbt = new NbtCompound();
            enchantmentNbt.putString("type", enchantment.getEnchantmentType().name());
            enchantmentNbt.putInt("level", enchantment.getEnchantmentLevel());
            buf.writeNbt(enchantmentNbt);
        }
    }
    @Override
    public List<EntityEnchantment> read(PacketByteBuf packetByteBuf) {
        List<EntityEnchantment> list = new ArrayList<>();
        int size = packetByteBuf.readInt();
        for (int i = 0; i < size; i++) {
            NbtCompound nbt = packetByteBuf.readNbt();
            if (nbt != null) {
                String typeName = nbt.getString("type");
                int level = nbt.getInt("level");
                EntityEnchantmentType type = EntityEnchantmentType.valueOf(typeName);
                var e = new EntityEnchantment(type,level);
                list.add(e);
            }
        }
        return list;
    }
    @Override
    public List<EntityEnchantment> copy(List<EntityEnchantment> list) {
        return new ArrayList<>(list);
    }
}
