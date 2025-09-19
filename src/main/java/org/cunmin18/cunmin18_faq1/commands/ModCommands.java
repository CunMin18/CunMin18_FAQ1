package org.cunmin18.cunmin18_faq1.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ModCommands {

    public static void registerCommands(){
        //注册指令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            EntityEnchantmentCommand.register(dispatcher, registryAccess, environment);
            EnchantmentSettingCommand.register(dispatcher, registryAccess, environment);
        });
    }
}
