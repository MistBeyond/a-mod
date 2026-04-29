package com.mistbeyond.examplemod.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public final class CommonConfig {
    public static final ModConfigSpec CONFIG_SPEC;
    public static final CommonConfig instance;

    public final ModConfigSpec.BooleanValue logDirtBlock;
    public final ModConfigSpec.IntValue magicNumber;
    public final ModConfigSpec.ConfigValue<String> magicNumberIntroduction;
    public final ModConfigSpec.ConfigValue<List<? extends String>> itemStrings; // a list of strings that are treated as resource locations for items

    private CommonConfig(ModConfigSpec.Builder builder) {
        logDirtBlock = builder
                .comment("Whether to log the dirt block on common setup")
                .define("logDirtBlock", true);
        magicNumber = builder
                .comment("A magic number")
                .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);
        magicNumberIntroduction = builder
                .comment("What you want the introduction message to be for the magic number")
                .define("magicNumberIntroduction", "The magic number is... ");
        itemStrings = builder
                .comment("A list of items to log on common setup.")
                .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", CommonConfig::validateItemName);
    }

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
    }

    static {
        var pair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        instance = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}
