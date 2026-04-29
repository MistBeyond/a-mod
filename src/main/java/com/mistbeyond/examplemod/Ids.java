package com.mistbeyond.examplemod;

import net.minecraft.resources.Identifier;

public final class Ids {
    public static final String MODID = "examplemod";
    public static final String TEST_MACHINE = "test_machine";
    public static final String CRUSHER = "crusher";

    private Ids() {
    }

    public static Identifier thisMod(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static boolean isThisMod(Identifier id) {
        return id.getNamespace().equals(MODID);
    }

    public static boolean isThisMod(String fullIdentifier) {
        var id = Identifier.tryParse(fullIdentifier);
        return id != null && isThisMod(id);
    }

    public static boolean isVanilla(Identifier id) {
        return id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE);
    }

    public static boolean isVanilla(String id) {
        return isVanilla(Identifier.parse(id));
    }
}
