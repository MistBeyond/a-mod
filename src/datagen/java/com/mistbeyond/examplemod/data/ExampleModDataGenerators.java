package com.mistbeyond.examplemod.data;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.data.provider.ModItemTagsProvider;
import com.mistbeyond.examplemod.data.provider.ModLanguageProvider;
import com.mistbeyond.examplemod.data.provider.model.ModModelProvider;
import com.mistbeyond.examplemod.data.provider.recipe.ExampleModRecipeProvider;
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
        event.createProvider(ModLanguageProvider::new);
        event.createProvider(ModModelProvider::new);

        // server
        event.createDatapackRegistryObjects(new RegistrySetBuilder());
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ExampleModRecipeProvider.Runner::new);
    }
}
