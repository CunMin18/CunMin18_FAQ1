package org.cunmin18.cunmin18_faq1.mixins.task;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;
import org.cunmin18.cunmin18_faq1.villager_tasks.ClericVillagerTask;
import org.cunmin18.cunmin18_faq1.villager_tasks.LibrarianVillagerTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerTaskListProvider.class)
public class VillagerTaskListProviderMixin {
    @Shadow
    private static Pair<Integer, Task<LivingEntity>> createBusyFollowTask() {
        return Pair.of(5, new RandomTask<>(
                ImmutableList.of(
                        Pair.of(LookAtMobTask.create(EntityType.VILLAGER, 8.0F), 2),
                        Pair.of(LookAtMobTask.create(EntityType.PLAYER, 8.0F), 2),
                        Pair.of(new WaitTask(30, 60), 8)
                )
        ));
    }
    //给指定职业村民添加自定义任务
    @Inject(method = "createWorkTasks", at = @At("RETURN"), cancellable = true)
    private static void createWorkTasks(VillagerProfession profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, Task<VillagerEntity>>>> ci) {
        var originalList = ci.getReturnValue();
        if(profession.equals(VillagerProfession.LIBRARIAN)){
            var newList = ImmutableList.<Pair<Integer, Task<VillagerEntity>>>builder()
                    .addAll(originalList)
                        .add(Pair.of(10, new LibrarianVillagerTask()))
                    .build();
            ci.setReturnValue(newList);
        }
        else if (profession.equals(VillagerProfession.CLERIC)) {
            var newList = ImmutableList.<Pair<Integer, Task<VillagerEntity>>>builder()
                    .addAll(originalList)
                    .add(Pair.of(10, new ClericVillagerTask()))
                    .build();
            ci.setReturnValue(newList);
        }
        else{
            var newList = ImmutableList.<Pair<Integer, Task<VillagerEntity>>>builder()
                    .addAll(originalList)
                    .build();
            ci.setReturnValue(newList);
        }
    }
}
