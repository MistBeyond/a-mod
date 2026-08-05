package com.mistbeyond.examplemod.data.tags;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.item.Items;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ExampleModItemTagsProvider extends ItemTagsProvider {
    public ExampleModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Ids.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(Tags.Items.TOOLS_WRENCH)
                .add(Items.TEST_WRENCH.get());
    }
}
