package com.mistbeyond.examplemod.core.registry.impl;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.modscan.ModAnnotation;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// todo: refactor these pieces of sh1t
class NeoApiHelper {
    private NeoApiHelper() {
    }

    /**
     * Uses {@link ReflectHelper}'s class loader to load classes
     */
    public static List<Class<?>> loadSideInsensitiveClasses(ModFileScanData scanData) {
        return ReflectHelper.loadClasses(getSideInsensitiveClassNames(scanData));
    }

    /**
     * Uses {@link ReflectHelper}'s class loader to load classes
     */
    public static List<Class<?>> loadSideSensitiveClasses(ModFileScanData scanData, Dist dist) {
        return ReflectHelper.loadClasses(getSideSensitiveClassNames(scanData, dist));
    }

    public static List<ModFileScanData.AnnotationData> getNeoAnnotationDataBy(ModFileScanData scanData, Class<? extends Annotation> annotation) {
        return scanData.getAnnotatedBy(annotation, ElementType.TYPE).toList();
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> resolveAndValidate(ModFileScanData.AnnotationData data, Class<T> superclass) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(data.memberName());
        if (!superclass.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(String.format("Class %s is not a %s", data.memberName(), superclass.getName()));
        }
        return (Class<? extends T>) clazz;
    }

    static boolean isSideAnnotation(Type annotation) {
        return annotation.getClassName().equals(Side.class.getName());
    }

    static Set<String> getSideInsensitiveClassNames(ModFileScanData scanData) {
        var unsided = scanData.getClasses().stream().map(a -> a.clazz().getClassName()).filter(NeoApiHelper::acceptClasses).collect(Collectors.toSet());
        scanData.getAnnotations().stream()
                .filter(a -> NeoApiHelper.isSideAnnotation(a.annotationType()))
                .map(ModFileScanData.AnnotationData::memberName)
                .forEach(unsided::remove);

        var unsidedPackageNames = unsided.stream()
                .filter(s -> s.endsWith(".package-info"))
                .map(ReflectHelper::getPackageName)
                .collect(Collectors.toUnmodifiableSet());

        return unsided.stream()
                .filter(s -> !s.endsWith(".package-info"))
                .filter(s -> isClassInPackages(unsidedPackageNames, s))
                .collect(Collectors.toUnmodifiableSet());
    }

    static Set<String> getSideSensitiveClassNames(ModFileScanData scanData, Dist dist) {
        // holy sh1t, Element.PACKAGE does not exist in bytecode
        var sided = scanData.getAnnotations().stream()
                .filter(a -> NeoApiHelper.isSideAnnotation(a.annotationType()))
                .filter(a -> ((ModAnnotation.EnumHolder) a.annotationData().get("value")).value().equals(dist.name()))
                .map(ModFileScanData.AnnotationData::memberName)
                .filter(NeoApiHelper::acceptClasses)
                .collect(Collectors.toUnmodifiableSet());

        var sidedClassNames = sided.stream()
                .filter(s -> !s.endsWith(".package-info"))
                .collect(Collectors.toUnmodifiableSet());

        var sidedPackageNames = sided.stream()
                .filter(s -> s.endsWith(".package-info"))
                .map(ReflectHelper::getPackageName)
                .collect(Collectors.toUnmodifiableSet());

        return scanData.getClasses().stream()
                .map(d -> d.clazz().getClassName())
                .filter(s -> sidedClassNames.contains(s) || isClassInPackages(sidedPackageNames, s))
                .collect(Collectors.toUnmodifiableSet());
    }

    static boolean isClassInPackages(Collection<String> packageNames, String className) {
        return packageNames.stream().anyMatch(p -> p.equals(className.substring(0, className.lastIndexOf('.'))));
    }

    static boolean acceptClasses(String className) {
        return !className.contains("com.mistbeyond.examplemod.earlycheck") && !className.contains("com.mistbeyond.examplemod.data");
    }
}
