package cn.minecraftbe.examplemod.provider;

import cn.minecraftbe.examplemod.ExampleMod;
import cn.minecraftbe.examplemod.block.Blocks;
import cn.minecraftbe.examplemod.integration.jade.JadePlugin;
import cn.minecraftbe.examplemod.item.Items;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.BlockItem;
import org.apache.commons.lang3.text.WordUtils;

import java.util.function.Function;


public class LanguageProvider extends net.neoforged.neoforge.common.data.LanguageProvider {

    public LanguageProvider(PackOutput output) {
        super(output, ExampleMod.MODID, "en_us");
    }


    @Override
    protected void addTranslations() {
        this.add("itemGroup.examplemod", "Example Tab");
        this.add("examplemod.configuration.title", "Example Mod Configs");
        this.add("examplemod.configuration.section.examplemod.common.toml", "Example Mod Configs");
        this.add("examplemod.configuration.section.examplemod.common.toml.title", "Example Mod Configs");
        this.add("examplemod.configuration.items", "Item List");
        this.add("examplemod.configuration.logDirtBlock", "Log Dirt Block");
        this.add("examplemod.configuration.magicNumber", "Magic Number");
        this.add("examplemod.configuration.magicNumberIntroduction", "Magic Number Text");
        this.add("examplemod.overlaymessage.wrench.rotation_failed", "Rotation failed on %s axis");

        // may crash
        English.addBlocksAndItems(this);
        English.addJadeIds(this);
    }

    public static class English {
        static void addBlocksAndItems(net.neoforged.neoforge.common.data.LanguageProvider provider) {
            for (var holder : Blocks.BLOCKS.getEntries()) {
                provider.addBlock(holder, byId(holder.getId(), English::capitalize));
            }

            for (var holder : Items.ITEMS.getEntries()) {
                if (!(holder.get() instanceof BlockItem))
                    provider.addItem(holder, byId(holder.getId(), English::capitalize));
            }
        }

        static void addJadeIds(net.neoforged.neoforge.common.data.LanguageProvider provider) {
            for (var id : JadePlugin.Ids.IDENTIFIERS) {
                provider.add("config.jade.plugin_" + id.toLanguageKey(), byId(id, English::capitalize));
            }
        }

        static void addFromKeys(
                net.neoforged.neoforge.common.data.LanguageProvider provider,
                Function<String, String> formatter,
                String... keys
        ) {
            for (String key : keys) {
                if (key.isBlank()) continue;
                provider.add(key, byKey(key, formatter));
            }
        }

        public static String byId(Identifier id, Function<String, String> formatter) {
            return byKey(Util.makeDescriptionId(null, id), formatter);
        }

        public static String byKey(String key, Function<String, String> formatter) {
            int delimiter = key.lastIndexOf(".");
            return formatter.apply(key.substring(delimiter + 1).replace("_", " "));
        }


        /**
         * The wrapper of {@link org.apache.commons.lang3.text.WordUtils#capitalizeFully(String)}}
         *
         */
        @SuppressWarnings("deprecation")
        static String capitalize(String string) {
            return WordUtils.capitalizeFully(string);
        }
    }
}
