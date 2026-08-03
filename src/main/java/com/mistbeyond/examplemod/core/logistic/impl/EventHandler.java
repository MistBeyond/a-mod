package com.mistbeyond.examplemod.core.logistic.impl;

import com.mistbeyond.examplemod.Ids;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = Ids.MODID)
class EventHandler {

    @SubscribeEvent
    static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            EnergyNetworkManager.INSTANCE.processPendingLoads(serverLevel);
        }
    }

    @SubscribeEvent
    static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            EnergyNetworkManager.INSTANCE.onLevelUnload(serverLevel);
        }
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        EnergyNetworkManager.INSTANCE.clearAll();
    }
}
