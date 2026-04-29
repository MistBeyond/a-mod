package com.mistbeyond.examplemod.core;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.recipe.RecipeTypes;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

@EventBusSubscriber(modid = Ids.MODID)
public class SideSynchronizer {
    private SideSynchronizer() {
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        @SuppressWarnings("unchecked")
        var modRecipeTypes = (List<RecipeType<?>>) (List<?>) RecipeTypes.getAll().stream().map(DeferredHolder::get).toList();
        event.sendRecipes(modRecipeTypes);
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = Ids.MODID)
    public static class Client {
        @SubscribeEvent
        public static void onRecipesReceived(RecipesReceivedEvent event) {
            ModClientRecipes.syncRecipe(event.getRecipeMap());
        }

        @SubscribeEvent
        public static void onClientPlayerNetworkLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ModClientRecipes.clearRecipe();
        }

    }
}
