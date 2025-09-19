package org.cunmin18.cunmin18_faq1.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.cunmin18.cunmin18_faq1.config.ModClientConfig;

public class EnchantmentSettingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("cmfaq1")

                .then(CommandManager.literal("rule")
                    .then(CommandManager.literal("enable_loyalty_enchantment")
                        .then(CommandManager.argument("bool", BoolArgumentType.bool())
                            .executes(EnchantmentSettingCommand::setLoyaltyEnchantment))
                    )
                )

                .then(CommandManager.literal("rule")
                    .then(CommandManager.literal("isEnable_librarian_task")
                        .then(CommandManager.argument("bool", BoolArgumentType.bool())
                            .executes(EnchantmentSettingCommand::setLibrarianTask))
                    )
                )

                .then(CommandManager.literal("rule")
                    .then(CommandManager.literal("isEnable_cleric_task")
                        .then(CommandManager.argument("bool", BoolArgumentType.bool())
                            .executes(EnchantmentSettingCommand::setClericTask))
                    )
                )
        );
    }
    private static int setLibrarianTask(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var b = BoolArgumentType.getBool(context,"bool");
        ModClientConfig.isEnable_librarian_task = b;
        context.getSource().sendFeedback(() -> Text.literal("设置isEnable_librarian_task为" + b), false);
        ModClientConfig.INSTANCE.save();
        return 1;
    }
    private static int setLoyaltyEnchantment(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var b = BoolArgumentType.getBool(context,"bool");
        ModClientConfig.isEnable_loyalty_enchantment = b;
        context.getSource().sendFeedback(() -> Text.literal("设置isEnable_loyalty_enchantment为" + b), false);
        ModClientConfig.INSTANCE.save();
        return 1;
    }
    private static int setClericTask(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var b = BoolArgumentType.getBool(context,"bool");
        ModClientConfig.isEnable_cleric_task = b;
        context.getSource().sendFeedback(() -> Text.literal("设置isEnable_cleric_task为" + b), false);
        ModClientConfig.INSTANCE.save();
        return 1;
    }
}
