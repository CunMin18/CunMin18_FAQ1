package org.cunmin18.cunmin18_faq1;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import org.cunmin18.cunmin18_faq1.commands.ModCommands;
import org.cunmin18.cunmin18_faq1.config.ModClientConfig;
import org.cunmin18.cunmin18_faq1.event.ModEvents;
import org.cunmin18.cunmin18_faq1.tracked_datas.EntityEnchantmentListHandler;

public class Cunmin18_faq1 implements ModInitializer {
    public static String modName = "cunmin18_faq1";
    @Override
    public void onInitialize() {
        //注册事件
        ModEvents.registerEvents();
        //注册自定义指令
        ModCommands.registerCommands();
        //注册配置文件
        ModClientConfig.INSTANCE.load();
        TrackedDataHandlerRegistry.register(EntityEnchantmentListHandler.INSTANCE);
    }
}
