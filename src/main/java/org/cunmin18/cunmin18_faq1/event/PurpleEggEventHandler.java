package org.cunmin18.cunmin18_faq1.event;


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.cunmin18.cunmin18_faq1.commands.EnchantmentSettingCommand;
import org.cunmin18.cunmin18_faq1.config.ModClientConfig;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;
import org.cunmin18.cunmin18_faq1.utils.EntityEnchantmentTools;
import org.cunmin18.cunmin18_faq1.utils.ILivingEntity;

public class PurpleEggEventHandler implements ServerTickEvents.EndWorldTick{
    @Override
    public void onEndTick(ServerWorld world) {
        //每Tick检测所有生物附近是否有带忠诚附魔的实体，如果有，则这个实体会围着那个实体跳舞
        if(!ModClientConfig.isEnable_loyalty_enchantment) return;
        for(ServerPlayerEntity player : world.getPlayers()){
            var minWorldPos = player.getBlockPos().add(-20, -2, -20);
            var maxWorldPos = player.getBlockPos().add(20, 4, 20);
            Vec3d vec1 = new Vec3d(minWorldPos.getX(), minWorldPos.getY(), minWorldPos.getZ());
            Vec3d vec2 = new Vec3d(maxWorldPos.getX(), maxWorldPos.getY(), maxWorldPos.getZ());
            for(LivingEntity livingEntity : world.getEntitiesByClass(LivingEntity.class,new Box(vec1,vec2),entity -> !(entity instanceof PlayerEntity))){
                var box = new Box(livingEntity.getBlockPos()).expand(10);
                var ll = world.getEntitiesByClass(LivingEntity.class,box,pig -> EntityEnchantmentTools.hasEnchantment(pig, EntityEnchantmentType.LOYALTY) && pig != livingEntity);
                var hasPig = !ll.isEmpty();
                if(hasPig){
                    var pig = ll.get(0);
                    //发现忠诚附魔实体，看向它
                    livingEntity.lookAt(pig.getCommandSource().getEntityAnchor(), pig.getPos());
                    if(livingEntity.isOnGround()){
                        //跳跃
                        ((ILivingEntity)livingEntity).callJumpMethod();
                        livingEntity.swingHand((livingEntity.getActiveHand()));
                        //得到距离
                        var distance = livingEntity.distanceTo(pig);
                        //距离过远，主动靠近带忠诚附魔的实体
                        if(distance > 4){
                            var ev = livingEntity.getVelocity();
                            var rotation = Math.toRadians(livingEntity.getYaw());
                            var moveSpeed = livingEntity.getAttributeBaseValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                            var dx = -Math.sin(rotation) * moveSpeed;
                            var dz = Math.cos(rotation) * moveSpeed;
                            //添加速度，以实现移动
                            livingEntity.setVelocity(dx,ev.y,dz);
                        }
                    }
                    double headX = livingEntity.getX();
                    double headY = livingEntity.getY() + livingEntity.getHeight() * 0.8;
                    double headZ = livingEntity.getZ();
                    //冒爱心
                    if (player.getWorld() instanceof ServerWorld) {
                        ((ServerWorld) player.getWorld()).spawnParticles(
                                ParticleTypes.HEART,
                                headX + (world.random.nextDouble() - 0.5) * 0.5,
                                headY + world.random.nextDouble() * 0.5,
                                headZ + (world.random.nextDouble() - 0.5) * 0.5,
                                5,
                                0, 0, 0,
                                0.0
                        );
                    }
                }
            }
        }
    }

}
