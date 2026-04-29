package com.mistbeyond.examplemod.core.registry;

import com.mistbeyond.examplemod.core.registry.impl.BaseRegistration;
import com.mistbeyond.examplemod.core.registry.impl.BlockRegistration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated method must be static, and its only parameter must be an {@link BaseRegistration} implementation, such as {@link BlockRegistration}.
 * <p>
 * Then, you can register objects, like blocks:
 * <pre>{@code
 * @SubscribeRegistration
 * private static void registerBlocks(BlockRegistration registration) {
 *     // if your block constructor is the standard (Properties) -> YourBlock
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
@Target(ElementType.METHOD)
public @interface SubscribeRegistration {
}
