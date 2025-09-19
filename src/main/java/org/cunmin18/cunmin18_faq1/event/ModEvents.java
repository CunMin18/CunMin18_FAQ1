package org.cunmin18.cunmin18_faq1.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ModEvents {
    public static void registerEvents(){
        ServerTickEvents.END_WORLD_TICK.register(new PurpleEggEventHandler());
        ServerTickEvents.END_WORLD_TICK.register(new AbsorbWaterEnchantmentEventHandler());
    }
}
