package cn.minecraftbe.examplemod.util.transfer;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class StacksHandlerUtil {
    private StacksHandlerUtil() {
    }
    public static ItemStack getStack(StacksResourceHandler<ItemStack, ItemResource> handler, int index) {
        return handler.getResource(index).toStack(handler.getAmountAsInt(index));
    }

    public static void setStack(StacksResourceHandler<ItemStack, ItemResource> handler, int index, ItemStack stack) {
        handler.set(index, ItemResource.of(stack), stack.count());
    }

    public static void grow(StacksResourceHandler<ItemStack, ItemResource> handler, int index, int amount) {
        var updated = getStack(handler, index);
        updated.grow(amount);
        setStack(handler, index, updated);
    }

    public static void shrink(StacksResourceHandler<ItemStack, ItemResource> handler, int index, int amount) {
        grow(handler, index, -amount);
    }

    public static void replaceAndGrow(StacksResourceHandler<ItemStack, ItemResource> handler, int index, Item item, int amount) {
        handler.set(index, ItemResource.of(item), handler.getAmountAsInt(index) + amount);
    }

    public static void replaceAndShrink(StacksResourceHandler<ItemStack, ItemResource> handler, int index, Item item, int amount) {
        replaceAndGrow(handler, index, item, -amount);
    }
}
