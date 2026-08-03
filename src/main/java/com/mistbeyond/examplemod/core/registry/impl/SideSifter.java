package com.mistbeyond.examplemod.core.registry.impl;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.modscan.ModAnnotation;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Classifies the classes of a {@link ModFileScanData} by the mod's {@link Side} annotation and
 * loads them with {@link ReflectHelper}'s class loader.
 * <p>
 * The {@link Side} annotation may target both classes and packages. Since bytecode has no
 * {@link java.lang.annotation.ElementType#PACKAGE} member, a package-level annotation is scanned
 * as the member {@code <package>.package-info} and must be expanded to the package's classes.
 */
class SideSifter {
    private static final String PACKAGE_INFO_SUFFIX = ".package-info";

    private SideSifter() {
    }

    /**
     * Loads the classes that are neither side-annotated themselves nor members of a
     * side-annotated package.
     * <p>
     * A class is included only when its package declares a plain (non-{@code @Side})
     * {@code package-info}; packages without one are skipped.
     *
     * @param exclusion the packages to skip entirely
     * @return the loaded classes
     */
    public static List<Class<?>> loadInsensitive(ModFileScanData scanData, ExclusionPolicy exclusion) {
        return ReflectHelper.loadClasses(insensitiveNames(scanData, exclusion));
    }

    /**
     * Loads the classes that are annotated with {@code @Side(dist)} or members of a package
     * annotated with {@code @Side(dist)}.
     *
     * @param dist      the side to load
     * @param exclusion the packages to skip entirely
     * @return the loaded classes
     */
    public static List<Class<?>> loadSensitive(ModFileScanData scanData, Dist dist, ExclusionPolicy exclusion) {
        return ReflectHelper.loadClasses(sensitiveNames(scanData, dist, exclusion));
    }

    static Set<String> insensitiveNames(ModFileScanData scanData, ExclusionPolicy exclusion) {
        var remaining = scanData.getClasses().stream()
                .map(d -> d.clazz().getClassName())
                .filter(s -> !exclusion.excludes(s))
                .collect(Collectors.toCollection(HashSet::new));
        sideAnnotatedMembers(scanData).forEach(remaining::remove);

        var plainPackageNames = packageInfoNames(remaining).stream()
                .map(ReflectHelper::getPackageName)
                .collect(Collectors.toUnmodifiableSet());

        return remaining.stream()
                .filter(SideSifter::isNotPackageInfo)
                .filter(s -> isInPackages(plainPackageNames, s))
                .collect(Collectors.toUnmodifiableSet());
    }

    static Set<String> sensitiveNames(ModFileScanData scanData, Dist dist, ExclusionPolicy exclusion) {
        var sided = sideAnnotatedData(scanData)
                .filter(a -> sideDistValue(a).equals(dist.name()))
                .map(ModFileScanData.AnnotationData::memberName)
                .filter(s -> !exclusion.excludes(s))
                .collect(Collectors.toUnmodifiableSet());

        var sidedClassNames = sided.stream()
                .filter(SideSifter::isNotPackageInfo)
                .collect(Collectors.toUnmodifiableSet());
        var sidedPackageNames = packageInfoNames(sided).stream()
                .map(ReflectHelper::getPackageName)
                .collect(Collectors.toUnmodifiableSet());

        return scanData.getClasses().stream()
                .map(d -> d.clazz().getClassName())
                .filter(s -> sidedClassNames.contains(s) || isInPackages(sidedPackageNames, s))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> sideAnnotatedMembers(ModFileScanData scanData) {
        return sideAnnotatedData(scanData)
                .map(ModFileScanData.AnnotationData::memberName)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * The annotation data of every member annotated with {@link Side}.
     */
    private static Stream<ModFileScanData.AnnotationData> sideAnnotatedData(ModFileScanData scanData) {
        return scanData.getAnnotations().stream()
                .filter(a -> isSideAnnotation(a.annotationType()));
    }

    private static boolean isSideAnnotation(Type annotation) {
        return annotation.getClassName().equals(Side.class.getName());
    }

    /**
     * @return the name of the {@link Dist} value of a {@code @Side} annotation
     */
    private static String sideDistValue(ModFileScanData.AnnotationData data) {
        return ((ModAnnotation.EnumHolder) data.annotationData().get("value")).value();
    }

    private static Set<String> packageInfoNames(Collection<String> names) {
        return names.stream().filter(SideSifter::isPackageInfo).collect(Collectors.toUnmodifiableSet());
    }

    private static boolean isPackageInfo(String className) {
        return className.endsWith(PACKAGE_INFO_SUFFIX);
    }

    private static boolean isNotPackageInfo(String className) {
        return !isPackageInfo(className);
    }

    /**
     * @return whether the class is a direct member of one of the given packages
     */
    private static boolean isInPackages(Collection<String> packageNames, String className) {
        var dot = className.lastIndexOf('.');
        if (dot == -1) {
            return false;
        }
        return packageNames.contains(className.substring(0, dot));
    }
}
