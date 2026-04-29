package com.mistbeyond.examplemod.core.registry.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public record ClassContainer(List<Class<?>> classes) {
    public ClassContainer() {
        this(new ArrayList<>());
    }

    public List<Class<?>> getAnnotatedBy(Class<? extends Annotation> annotation) {
        return ReflectHelper.getAnnotatedClass(classes, annotation);
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }
}
