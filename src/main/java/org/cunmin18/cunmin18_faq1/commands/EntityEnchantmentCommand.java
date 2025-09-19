package org.cunmin18.cunmin18_faq1.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.cunmin18.cunmin18_faq1.entity_enchantments.EntityEnchantmentType;
import org.cunmin18.cunmin18_faq1.utils.EntityEnchantmentTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityEnchantmentCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        ArgumentType<EntityEnchantmentType> EntityEnchantmentType = new ArgumentType<EntityEnchantmentType>() {
            @Override
            public EntityEnchantmentType parse(StringReader reader) throws CommandSyntaxException {
                return null;
            }
        };
        dispatcher.register(CommandManager.literal("cmfaq1")

                .then(CommandManager.literal("enchant")
                    .then(CommandManager.literal("clear")
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())
                            .executes(EntityEnchantmentCommand::executeClearEnchantment)
                        )
                    )
                ).

                then(CommandManager.literal("enchant")
                    .then(CommandManager.literal("add")
                        .then(CommandManager.argument("type", StringArgumentType.string())
                            .then(CommandManager.argument("level", IntegerArgumentType.integer())
                                .then(CommandManager.argument("targets", EntityArgumentType.entities())
                                    .executes(EntityEnchantmentCommand::executeAddEnchantment))
                            )
                        )
                    )
                ).

                then(CommandManager.literal("enchant").
                    then(CommandManager.literal("remove")
                        .then(CommandManager.argument("type", StringArgumentType.string())
                            .then(CommandManager.argument("targets", EntityArgumentType.entities()))
                                .executes(EntityEnchantmentCommand::executeRemoveEnchantment))
                        )
                    )
                );
    }
    private static int executeClearEnchantment(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Collection<? extends Entity> entities = EntityArgumentType.getEntities(context, "targets");
        List<Entity> mutableEntities = new ArrayList<>(entities);

        if (entities.isEmpty()) {
            mutableEntities.add(source.getEntity());
        }
        int processedCount = 0;
        for (Entity entity : mutableEntities) {
            if(entity instanceof LivingEntity livingEntity){
                EntityEnchantmentTools.removeAllEnchantmentsFromEntity(livingEntity);
                processedCount++;
            }
        }

        context.getSource().sendFeedback(() -> Text.literal("删除了全部附魔."), false);

        return processedCount;
    }
    private static int executeAddEnchantment(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Collection<? extends Entity> entities = EntityArgumentType.getEntities(context, "targets");
        var typeS = StringArgumentType.getString(context,"type");
        var type = EntityEnchantmentType.valueOf(typeS);
        var level = IntegerArgumentType.getInteger(context,"level");
        List<Entity> mutableEntities = new ArrayList<>(entities);

        if (entities.isEmpty()) {
            mutableEntities.add(source.getEntity());
        }
        int processedCount = 0;
        for (Entity entity : mutableEntities) {
            if(entity instanceof LivingEntity livingEntity){
                EntityEnchantmentTools.addEnchantmentToEntity(livingEntity,type,level);
                processedCount++;
            }
        }

        context.getSource().sendFeedback(() -> Text.literal("添加了附魔."), false);

        return processedCount;
    }
    private static int executeRemoveEnchantment(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Collection<? extends Entity> entities = EntityArgumentType.getEntities(context, "targets");
        var typeS = StringArgumentType.getString(context,"type");
        var type = EntityEnchantmentType.valueOf(typeS);
        List<Entity> mutableEntities = new ArrayList<>(entities);

        if (entities.isEmpty()) {
            mutableEntities.add(source.getEntity());
        }
        int processedCount = 0;
        for (Entity entity : mutableEntities) {
            if(entity instanceof LivingEntity livingEntity){
                EntityEnchantmentTools.removeEnchantmentFromEntity(livingEntity,type);
                processedCount++;
            }
        }

        context.getSource().sendFeedback(() -> Text.literal("删除了附魔."), false);

        return processedCount;
    }
}
