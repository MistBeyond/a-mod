package cn.minecraftbe.examplemod.datagen;

import cn.minecraftbe.examplemod.ExampleMod;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.apache.commons.lang3.StringUtils;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class LanguageProvider extends net.neoforged.neoforge.common.data.LanguageProvider {

    public LanguageProvider(PackOutput output) {
        super(output, ExampleMod.MODID, "en_us");
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        event.createProvider(LanguageProvider::new);
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
