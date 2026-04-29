package com.mistbeyond.examplemod.core.registry.impl;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@Slf4j
class ReflectHelper {

    private ReflectHelper() {
    }

    public static <T, A> Constructor<T> getConstructor(Class<T> clazz, Class<A> paramType) {
        try {
            return clazz.getConstructor(paramType);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T, A, B> Constructor<T> getConstructor(Class<T> clazz, Class<A> paramTypeA, Class<B> paramTypeB) {
        try {
            return clazz.getConstructor(paramTypeA, paramTypeB);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T, A, B, C> Constructor<T> getConstructor(Class<T> clazz, Class<A> paramTypeA, Class<B> paramTypeB, Class<C> paramTypeC) {
        try {
            return clazz.getConstructor(paramTypeA, paramTypeB, paramTypeC);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T, A> T newInstance(Class<T> clazz, Class<A> paramType, A param) {
        try {
            return getConstructor(clazz, paramType).newInstance(param);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    public static <T, A, B> T newInstance(Class<T> clazz, Class<A> paramTypeA, A paramA, Class<B> paramTypeB, B paramB) {
        try {
            return getConstructor(clazz, paramTypeA, paramTypeB).newInstance(paramA, paramB);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    public static <T, A, B, C> T newInstance(Class<T> clazz, Class<A> paramTypeA, A paramA, Class<B> paramTypeB, B paramB, Class<C> paramTypeC, C paramC) {
        try {
            return getConstructor(clazz, paramTypeA, paramTypeB, paramTypeC).newInstance(paramA, paramB, paramC);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    /**
     * @throws NoSuchElementException when there is no any annotated static method.
     */
    public static Method getFirstAnnotatedStaticMethod(Class<?> clazz, Class<? extends Annotation> annotation) throws NoSuchElementException {
        return getAnnotatedStaticMethods(clazz, annotation).getFirst();
    }

    public static List<Method> getAnnotatedStaticMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getAnnotatedMethods(clazz, annotation, true);
    }

    public static List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation, boolean requireStatic) {
        List<Method> ret = new ArrayList<>(clazz.getDeclaredMethods().length);
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(annotation))
                .filter(m -> {
                    if (requireStatic) {
                        return isStatic(m);
                    }
                    return true;
                })
                .forEach(m -> {
                    m.setAccessible(true);
                    ret.add(m);
                });
        return ret;
    }

    public static List<Class<?>> getAnnotatedClass(List<Class<?>> classes, Class<? extends Annotation> annotation) {
        return classes.stream().filter(c -> c.isAnnotationPresent(annotation)).toList();
    }

    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    public static String getPackageName(String className) {
        return className.substring(0, className.lastIndexOf('.'));
    }

    /**
     * Uses {@link ReflectHelper}'s class loader to load classes
     */
    public static List<Class<?>> loadClasses(Collection<String> classNames) {
        List<Class<?>> ret = new ArrayList<>(classNames.size());
        var classLoader = ReflectHelper.class.getClassLoader();
        for (var name : classNames) {
            try {
                ret.add(classLoader.loadClass(name));
            } catch (ClassNotFoundException e) {
                log.error("Failed to load class: ", e);
                throw new IllegalStateException(e);
            }
        }
        return ret;
    }
}
