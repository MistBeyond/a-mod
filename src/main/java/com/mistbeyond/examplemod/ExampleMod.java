package com.mistbeyond.examplemod;

import com.mistbeyond.examplemod.config.CommonConfig;
import com.mistbeyond.examplemod.config.ServerConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(Ids.MODID)
public class ExampleMod {
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG_SPEC, String.format("%s/common.toml", Ids.MODID));
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG_SPEC, String.format("%s/server.toml", Ids.MODID));

        Init.registerCommon(modEventBus, modContainer);

        modEventBus.addListener(this::commonSetup);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (CommonConfig.instance.logDirtBlock.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", CommonConfig.instance.magicNumberIntroduction.get(), CommonConfig.instance.magicNumber.getAsInt());

        CommonConfig.instance.itemStrings.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

}
