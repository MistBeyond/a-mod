package cn.minecraftbe.examplemod.datagen;

import cn.minecraftbe.examplemod.block.Blocks;
import cn.minecraftbe.examplemod.item.Items;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Arrays;
import java.util.StringJoiner;

import static cn.minecraftbe.examplemod.ExampleMod.EXAMPLE_TAB;
import static cn.minecraftbe.examplemod.ExampleMod.MODID;

@EventBusSubscriber(modid = MODID)
public class MyLanguageProvider extends LanguageProvider {

    public MyLanguageProvider(PackOutput output) {
        super(output, MODID, "en_us");
    }

    private static String getNameFromRegistered(String name) {
        StringJoiner joiner = new StringJoiner(" ");
        Arrays.stream(name.strip().split(":")[1].split("_"))
                .map(str -> str.substring(0, 1).toUpperCase() + str.substring(1))
                .forEach(joiner::add);
        return joiner.toString();
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        event.createProvider(MyLanguageProvider::new);
    }

    @Override
    protected void addTranslations() {
        Blocks.BLOCKS.getEntries().forEach(holder ->
                this.addBlock(holder, getNameFromRegistered(holder.getRegisteredName()))
        );
        Items.ITEMS.getEntries().forEach(holder -> {
            if (!(holder.get() instanceof BlockItem))
                this.addItem(holder, getNameFromRegistered(holder.getRegisteredName()));
        });

        this.add("itemGroup.examplemod", getNameFromRegistered(EXAMPLE_TAB.getRegisteredName()));
        this.add("examplemod.configuration.title", "Example Mod Configs");
        this.add("examplemod.configuration.section.examplemod.common.toml", "Example Mod Configs");
        this.add("examplemod.configuration.section.examplemod.common.toml.title", "Example Mod Configs");
        this.add("examplemod.configuration.items", "Item List");
        this.add("examplemod.configuration.logDirtBlock", "Log Dirt Block");
        this.add("examplemod.configuration.magicNumber", "Magic Number");
        this.add("examplemod.configuration.magicNumberIntroduction", "Magic Number Text");
    }
}
