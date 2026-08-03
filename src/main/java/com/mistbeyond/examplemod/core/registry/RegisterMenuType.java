package com.mistbeyond.examplemod.core.registry;

import com.mistbeyond.examplemod.core.registry.impl.MenuTypeRegistration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a menu class for auto-registration in the mod's registry system.
 * The annotated class must contain exactly one static method, and strictly requires extending {@link net.minecraft.world.inventory.AbstractContainerMenu AbstractContainerMenu}.
 * The static method must be annotated with {@link SubscribeRegistration}, and its only parameter must be {@link MenuTypeRegistration}
 * <p>
 * Then, you can register menu types like this:
 * <pre>{@code
 * @SubscribeRegistration
 * private static void registerMenus(MenuTypeRegistration registration) {
 *     // if your menu constructor is the standard (containerId, inventory) -> YourMenu
 *     registration.register("your_menu_name", YourMenu::new);
 *     // else you can
 *     registration.register("your_menu_name", p -> new YourMenu(p, otherParam));
 * }}</pre>
 * <p>
 * Registering subclass instance in the superclass registration is not recommended, even though there are no checks.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterMenuType {
}
