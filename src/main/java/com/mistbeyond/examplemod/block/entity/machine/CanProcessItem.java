package com.mistbeyond.examplemod.block.entity.machine;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public interface CanProcessItem {
    StacksResourceHandler<ItemStack, ItemResource> getItemHandler();
}
