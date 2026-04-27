package cn.minecraftbe.examplemod;

import cn.minecraftbe.examplemod.provider.ItemTagsProvider;
import cn.minecraftbe.examplemod.provider.LanguageProvider;
import cn.minecraftbe.examplemod.provider.model.ModelProvider;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.RegistrySetBuilder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Slf4j
@EventBusSubscriber(modid = ExampleMod.MODID)
public class ExampleModDataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        log.debug("Starting data generation");
        // client
        event.createProvider(LanguageProvider::new);
        event.createProvider(ModelProvider::new);

        // server
        event.createDatapackRegistryObjects(new RegistrySetBuilder());
        event.createProvider(ItemTagsProvider::new);
    }
}
