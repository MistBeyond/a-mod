package com.mistbeyond.examplemod.core.registry.impl;

import net.neoforged.neoforgespi.language.ModFileScanData;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.List;

/**
 * Access to the NeoForge {@link ModFileScanData} API.
 */
class ScanDataHelper {
    private ScanDataHelper() {
    }

    /**
     * Returns the annotation data of the classes annotated with {@code annotation} (type-level only).
     */
    public static List<ModFileScanData.AnnotationData> annotationDataOf(ModFileScanData scanData, Class<? extends Annotation> annotation) {
        return scanData.getAnnotatedBy(annotation, ElementType.TYPE).toList();
    }

    /**
     * Resolves the annotated member as a loaded class and validates that it is a subtype of {@code superclass}.
     *
     * @param superclass the required superclass
     * @return the resolved class
     * @throws IllegalArgumentException when the annotated member is not a {@code superclass}
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> resolveAndValidate(ModFileScanData.AnnotationData data, Class<T> superclass) {
        Class<?> clazz = ReflectHelper.loadClasses(List.of(data.memberName())).getFirst();
        if (!superclass.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(String.format("Class %s is not a %s", data.memberName(), superclass.getName()));
        }
        return (Class<? extends T>) clazz;
    }
}
