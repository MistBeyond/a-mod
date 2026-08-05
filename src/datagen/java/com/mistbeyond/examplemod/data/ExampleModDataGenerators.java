package com.mistbeyond.examplemod.data;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.data.lang.ExampleModLanguageProvider;
import com.mistbeyond.examplemod.data.model.ExampleModModelProvider;
import com.mistbeyond.examplemod.data.recipe.ExampleModRecipeProvider;
import com.mistbeyond.examplemod.data.tags.ExampleModItemTagsProvider;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.RegistrySetBuilder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Slf4j
@EventBusSubscriber(modid = Ids.MODID)
public class ExampleModDataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        log.debug("Starting data generation");
        // client
        event.createProvider(ExampleModLanguageProvider::new);
        event.createProvider(ExampleModModelProvider::new);

        // server
        event.createDatapackRegistryObjects(new RegistrySetBuilder());
        event.createProvider(ExampleModItemTagsProvider::new);
        event.createProvider(ExampleModRecipeProvider.Runner::new);
    }
}
