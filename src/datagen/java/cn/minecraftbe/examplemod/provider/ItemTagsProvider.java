package cn.minecraftbe.examplemod.provider;

import cn.minecraftbe.examplemod.ExampleMod;
import cn.minecraftbe.examplemod.item.Items;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ItemTagsProvider extends net.neoforged.neoforge.common.data.ItemTagsProvider {
    public ItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ExampleMod.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(Tags.Items.TOOLS_WRENCH)
                .add(Items.TEST_WRENCH.get());
    }
}
