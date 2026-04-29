package com.mistbeyond.examplemod.core.registry;

import com.mistbeyond.examplemod.core.registry.impl.ItemRegistration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an item class for auto-registration in the mod's registry system.
 * The annotated class must contain exactly one static method, and strictly requires extending {@link net.minecraft.world.item.Item Item}.
 * The static method must be annotated with {@link SubscribeRegistration}, and its only parameter must be {@link ItemRegistration}
 * <p>
 * Then, you can register items like this:
 * <pre>{@code
 * @SubscribeRegistration
 * private static void registerItems(ItemRegistration registration) {
 *     // if your item constructor is the standard (Properties) -> YourItem
 *     registration.register("your_item_name", YourItem::new);
 *     // else you can
 *     registration.register("your_item_name", p -> new YourItem(p, otherParam));
 *     // you can also set the property
 *     registration.register("your_item_name", YourItem::new, p -> p.stacksTo(16));
 * }}</pre>
 * <p>
 * Registering subclass instance in the superclass registration is not recommended, even though there are no checks.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterItem {
}
