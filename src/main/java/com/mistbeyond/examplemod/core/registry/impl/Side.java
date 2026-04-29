package com.mistbeyond.examplemod.core.registry.impl;

import net.neoforged.api.distmarker.Dist;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the targeted side (client or server) when using the mod's automatic registration <b>checking</b>.
 * If a package is annotated with this annotation, any {@code @Side} annotation on a class within that package will be ignored.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface Side {
    Dist value();
}
