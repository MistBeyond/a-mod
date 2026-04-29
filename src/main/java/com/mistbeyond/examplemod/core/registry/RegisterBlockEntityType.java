package com.mistbeyond.examplemod.core.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a block entity class for auto-registration in the mod's registry system.
 * The annotated class must contain exactly one static method, and strictly requires extending {@link net.minecraft.world.level.block.entity.BlockEntity BlockEntity}.
 * The static method must be annotated with {@link ProvideFactory}, have no parameters, and return a type that implements the {@link net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier factory}.
 * <p>
 * You can provide the factory like this:
 * <pre>{@code
 * @ProvideFactory
 * private static BlockEntityType.BlockEntitySupplier<?> provideFactory() {
 *    return YourBlockEntity::new;
 * }
 * }</pre>
 * <p>
 * Providing the subclass factory in the superclass is not recommended, even though there are no runtime checks.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterBlockEntityType {
    /**
     * Must be a valid {@link net.minecraft.resources.Identifier#getPath() Identifier path}.
     */
    String value();
}
