package cn.minecraftbe.examplemod;

import cn.minecraftbe.examplemod.models.ModelProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class ExampleModDataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        event.createProvider(LanguageProvider::new);
        event.createProvider(ModelProvider::new);
    }
}
