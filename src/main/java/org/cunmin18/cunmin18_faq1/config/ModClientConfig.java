package org.cunmin18.cunmin18_faq1.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.cunmin18.cunmin18_faq1.Cunmin18_faq1;
import org.cunmin18.cunmin18_faq1.commands.EnchantmentSettingCommand;

public class ModClientConfig {
    public static final ConfigClassHandler<ModClientConfig> INSTANCE = ConfigClassHandler.createBuilder(ModClientConfig.class)
            .id(new Identifier(Cunmin18_faq1.modName, "client_config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve(Cunmin18_faq1.modName + "-client.json")).build())
            .build();
    @SerialEntry
    public static boolean isEnable_loyalty_enchantment = true;
    @SerialEntry
    public static boolean isEnable_librarian_task = true;
    @SerialEntry
    public static boolean isEnable_cleric_task = true;
}
