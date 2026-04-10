package cn.minecraftbe.examplemod;

import net.minecraft.data.PackOutput;
import org.apache.commons.lang3.StringUtils;


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

        // Prevent the entire process from crashing due to duplicate translations
        try {
            SimpleEnglishProvider.addSimpleTranslations(this);
            SimpleEnglishProvider.addSimpleTranslationFromKeys(
                    this,
                    StringUtils::capitalize,
                    "examplemod.overlaymessage.wrench.rotation_failed"
            );
        } catch (IllegalStateException e) {
            LOGGER.warn("Duplicated translation: ", e);
        }
    }
}
