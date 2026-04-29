package com.mistbeyond.examplemod.util.transfer;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class StacksHandlerUtil {
    private StacksHandlerUtil() {
    }

    public static ItemStack getItem(StacksResourceHandler<ItemStack, ItemResource> handler, int index) {
        return handler.getResource(index).toStack(handler.getAmountAsInt(index));
    }

    /**
     * Directly overwrites the contents of the handler.
     * It's unsafe.
     */
    public static void setItem(StacksResourceHandler<ItemStack, ItemResource> handler, int index, ItemStack stack) {
        handler.set(index, ItemResource.of(stack), stack.count());
    }

    public static int growItem(StacksResourceHandler<ItemStack, ItemResource> handler, int index, int amount, TransactionContext transaction) {
        var resource = handler.getResource(index);
        return handler.insert(index, resource, amount, transaction);
    }

    public static int shrinkItem(StacksResourceHandler<ItemStack, ItemResource> handler, int index, int amount, TransactionContext transaction) {
        var resource = handler.getResource(index);
        return handler.extract(index, resource, amount, transaction);
    }

    public static int replaceAndGrow(StacksResourceHandler<ItemStack, ItemResource> handler, int index, Item item, int amount, TransactionContext transaction) {
        var resource = ItemResource.of(item);
        return handler.insert(index, resource, amount, transaction);
    }

    /**
     * Unsafe, use the {@link StacksHandlerUtil#growItem(StacksResourceHandler, int, int, TransactionContext) transaction version}
     */
    public static void grow(StacksResourceHandler<ItemStack, ItemResource> handler, int index, int amount) {
        var updated = getItem(handler, index);
        updated.grow(amount);
        setItem(handler, index, updated);
    }

    /**
     * Unsafe, use the {@link StacksHandlerUtil#shrinkItem(StacksResourceHandler, int, int, TransactionContext) transaction version}
     */
    public static void shrink(StacksResourceHandler<ItemStack, ItemResource> handler, int index, int amount) {
        grow(handler, index, -amount);
    }

    /**
     * Unsafe, use the {transaction version}
     */
    public static void replaceAndGrow(StacksResourceHandler<ItemStack, ItemResource> handler, int index, Item item, int amount) {
        handler.set(index, ItemResource.of(item), handler.getAmountAsInt(index) + amount);
    }

    /**
     * Unsafe, use the {transaction version}
     */
    public static void replaceAndShrink(StacksResourceHandler<ItemStack, ItemResource> handler, int index, Item item, int amount) {
        replaceAndGrow(handler, index, item, -amount);
    }
}
