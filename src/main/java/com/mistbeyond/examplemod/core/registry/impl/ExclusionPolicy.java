package com.mistbeyond.examplemod.core.registry.impl;

import java.util.List;

/**
 * Packages whose classes must be skipped by the runtime registration check,
 * since they are checked elsewhere or side-specific.
 *
 * @param packagePrefixes fully qualified package names; matching includes subpackages
 */
record ExclusionPolicy(List<String> packagePrefixes) {

    /**
     * @param className the fully qualified class name
     * @return {@code true} when the class is inside one of the excluded packages
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean excludes(String className) {
        return packagePrefixes.stream()
                .anyMatch(p -> className.equals(p) || className.startsWith(p + "."));
    }
}
