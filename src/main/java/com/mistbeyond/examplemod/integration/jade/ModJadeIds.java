package com.mistbeyond.examplemod.integration.jade;

import com.mistbeyond.examplemod.Ids;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public final class ModJadeIds {
    public static final Set<Identifier> IDENTIFIERS = new HashSet<>();
    public static final Identifier TEST_MACHINE = thisMod("test_machine");
    public static final Identifier EU_STORAGE = thisMod("eu_storage");

    private ModJadeIds() {
    }

    static Identifier thisMod(String path) {
        var id = Identifier.fromNamespaceAndPath(Ids.MODID, path);
        IDENTIFIERS.add(id);
        return id;
    }
}
