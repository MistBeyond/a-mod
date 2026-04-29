package com.mistbeyond.examplemod.core.registry.impl;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;

/**
 * A convenience lookup table that associates a shared, human-readable name
 * with multiple registered game objects (block, item, block entity, menu type).
 * <p>
 * <b>Design intent</b><br>
 * The table allows you to use a single logical id (for example {@code "crusher"})
 * to retrieve different registry entries that belong together.  Internally it
 * creates a namespaced {@link net.minecraft.resources.Identifier Identifier}
 * by combining the mod's namespace with the supplied name, and uses that
 * identifier as a map key.  This key is <b>not</b> guaranteed to match the final
 * registered id used by the game.
 * <p>
 * <b>How registration works</b><br>
 * The actual registration is performed by the {@link net.neoforged.neoforge.registries.DeferredRegister} instances passed into the constructor (one for each registry
 * type).  Each {@code DeferredRegister} already holds the mod's namespace and
 * receives only the plain name from the annotation's {@code value()} when
 * {@code register(...)} is called.
 * <p>
 * <b>Typical usage</b><br>
 * After the table has been populated via {@link #registerCommon(IEventBus, ModContainer)},
 * you can write:
 * <pre>{@code
 *   REGISTRY_TABLE.block("crusher")       // returns the Block instance
 *   REGISTRY_TABLE.blockEntityType("crusher")  // returns the BlockEntityType<?>
 *   REGISTRY_TABLE.menuType("crusher")    // returns the MenuType<?>
 * }</pre>
 * This avoids having to hard-code separate {@code DeferredHolder} constants for
 * every related object and keeps the common name as the single point of reference.
 *
 * @author deepseek-v4-pro
 * @see DeferredRegister
 * @see DeferredHolder
 */
public abstract class CommonRegistrar {
    public final String modId;
    protected final DeferredRegister.Blocks blockRegister;
    protected final DeferredRegister.Items itemRegister;
    protected final DeferredRegister<BlockEntityType<?>> blockEntityRegister;
    protected final DeferredRegister<MenuType<?>> menuRegister;

    protected final HashMap<Identifier, DeferredBlock<?>> block = new HashMap<>();
    protected final HashMap<Identifier, DeferredItem<?>> item = new HashMap<>();
    protected final HashMap<Identifier, DeferredHolder<MenuType<?>, ?>> menuType = new HashMap<>();
    protected final HashMap<Identifier, DeferredHolder<BlockEntityType<?>, ?>> blockEntityType = new HashMap<>();
    protected final HashMap<Identifier, MenuScreens.ScreenConstructor<?, ? extends AbstractContainerScreen<?>>> containerScreen = new HashMap<>();

    protected CommonRegistrar(String modId, DeferredRegister.Blocks blockRegister, DeferredRegister.Items itemRegister, DeferredRegister<BlockEntityType<?>> blockEntityRegister, DeferredRegister<MenuType<?>> menuRegister) {
        this.modId = modId;
        this.blockRegister = blockRegister;
        this.itemRegister = itemRegister;
        this.blockEntityRegister = blockEntityRegister;
        this.menuRegister = menuRegister;
    }

    public static CommonRegistrar of(String modId, DeferredRegister.Blocks blockRegister, DeferredRegister.Items itemRegister, DeferredRegister<BlockEntityType<?>> blockEntityRegister, DeferredRegister<MenuType<?>> menuRegister) {
        return new CommonRegistryTable(modId, blockRegister, itemRegister, blockEntityRegister, menuRegister);
    }

    protected static <T> T requireRegistered(@Nullable T obj, String type, String id) {
        if (obj == null) {
            throw new IllegalStateException(String.format("%s '%s' was not registered!", type, id));
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    private static <T> T typed(Object o) {
        return (T) o;
    }

    public abstract void registerCommon(IEventBus modEventBus, ModContainer modContainer);

    public abstract void registerClient(IEventBus modEventBus, ModContainer modContainer);

    public void registerContainerScreens(RegisterMenuScreensEvent event) {
        for (var e : containerScreen.entrySet()) {
            var id = e.getKey();
            var constructor = e.getValue();
            event.register(menuTyped(id.getPath()), typed(constructor));
        }
    }

    public Block block(String id) {
        return requireRegistered(block.get(getId(id)), "Block", id).value();
    }

    public Item item(String id) {
        return requireRegistered(item.get(getId(id)), "Item", id).value();
    }

    public MenuType<?> menuType(String id) {
        return requireRegistered(menuType.get(getId(id)), "MenuType", id).value();
    }

    public BlockEntityType<?> blockEntityType(String id) {
        return requireRegistered(blockEntityType.get(getId(id)), "BlockEntityType", id).value();
    }

    /**
     * Unsafe cast.
     * Invoker should ensure the correct registry-type relation.
     */
    @ApiStatus.Internal
    public final <T extends Block> T blockTyped(String id) {
        return typed(block(id));
    }

    /**
     * Unsafe cast.
     * Invoker should ensure the correct registry-type relation.
     */
    @ApiStatus.Internal
    public final <T extends Item> T itemTyped(String id) {
        return typed(item(id));
    }

    /**
     * Unsafe cast.
     * Invoker should ensure the correct registry-type relation.
     */
    @ApiStatus.Internal
    public final <T extends AbstractContainerMenu> MenuType<T> menuTyped(String id) {
        return typed(menuType(id));
    }

    /**
     * Unsafe cast.
     * Invoker should ensure the correct registry-type relation.
     */
    @ApiStatus.Internal
    public final <T extends BlockEntity> BlockEntityType<T> blockEntityTyped(String id) {
        return typed(blockEntityType(id));
    }

    protected Identifier getId(String id) {
        return Identifier.fromNamespaceAndPath(modId, id);
    }
}
