package com.mistbeyond.examplemod.core.registry;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus;

public interface ISingleRegistrar<T> {
    @SuppressWarnings("unchecked")
    private static <T> T typed(Object o) {
        return (T) o;
    }

    DeferredHolder<T, ? extends T> getEntry(Identifier id);

    T get(Identifier id);

    /**
     * Unsafe cast.
     * Invoker should ensure the correct registry-type relation.
     */
    @ApiStatus.Internal
    default <R extends T> R getTyped(Identifier id) {
        return typed(get(id));
    }

    void register(IEventBus bus);
}
