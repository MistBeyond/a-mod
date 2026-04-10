package cn.minecraftbe.examplemod.datagen;

import cn.minecraftbe.examplemod.block.Blocks;
import cn.minecraftbe.examplemod.item.Items;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.apache.commons.lang3.text.WordUtils;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;

public class SimpleEnglishProvider {
    static void addSimpleTranslations(LanguageProvider provider) {
        Blocks.BLOCKS.getEntries().forEach(holder ->
                provider.addBlock(holder, getTranslationFromIdentifier(holder.getId(), SimpleEnglishProvider::capitalizeFully))
        );
        Items.ITEMS.getEntries().forEach(holder -> {
            if (!(holder.get() instanceof BlockItem))
                provider.addItem(holder, getTranslationFromIdentifier(holder.getId(), SimpleEnglishProvider::capitalizeFully));
        });
    }

    static void addSimpleTranslationFromKeys(
            LanguageProvider provider,
            @NonNull Function<String, String> formatter,
            @NonNull String... keys
    ) {
        for (String key : keys) {
            if (key.isBlank()) continue;
            provider.add(key, getTranslationFromKey(key, formatter));
        }
    }

    public static String getTranslationFromIdentifier(Identifier id, Function<String, String> formatter) {
        return getTranslationFromKey(Util.makeDescriptionId(null, id), formatter);
    }

    public static String getTranslationFromKey(String key, Function<String, String> formatter) {
        int delimiter = key.lastIndexOf(".");
        return formatter.apply(key.substring(delimiter + 1).replace("_", " "));
    }

    /**
     * The wrapper of {@link org.apache.commons.lang3.text.WordUtils#capitalizeFully(String)}}
     *
     */
    static String capitalizeFully(String string) {
        return WordUtils.capitalizeFully(string);
    }
}
