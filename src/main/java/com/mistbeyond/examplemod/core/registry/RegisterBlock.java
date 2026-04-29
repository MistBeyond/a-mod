package com.mistbeyond.examplemod.core.registry;

import com.mistbeyond.examplemod.core.registry.impl.BlockRegistration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a block class for auto-registration in the mod's registry system.
 * The annotated class must contain exactly one static method, and strictly requires extending {@link net.minecraft.world.level.block.Block Block}.
 * The static method must be annotated with {@link SubscribeRegistration}, and its only parameter must be {@link BlockRegistration}
 * <p>
 * Then, you can register blocks like this:
 * <pre>{@code
 * @SubscribeRegistration
 * private static void registerBlocks(BlockRegistration registration) {
 *     // if your block class constructor is the standard (Properties) -> YourBlock
 *     registration.register("your_block_name", YourBlock::new);
 *     // else you can
 *     registration.register("your_block_name", p -> new YourBlock(p, otherParams));
 *     // you can also set the property
 *     registration.register("your_block_name", YourBlock::new, p -> p.strength(1.0f));
 * }}</pre>
 * <p>
 * Registering subclass instance in the superclass registration is not recommended, even though there are no checks.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterBlock {
    /**
     * Permits auto register block item
     */
    boolean registerBlockItem() default true;
}
