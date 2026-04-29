package com.mistbeyond.examplemod.inventory;

import com.mistbeyond.examplemod.core.energy.EnergyConversionPermission;
import com.mistbeyond.examplemod.util.ItemUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class BatterySlot extends ResourceHandlerSlot implements EnergyConversionPermission {

    public BatterySlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier, int handlerSlot, int xPosition, int yPosition) {
        super(handler, slotModifier, handlerSlot, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (ItemUtil.isBatteryLike(stack, allowEnergyConversion())) {
            return super.mayPlace(stack);
        }
        return false;
    }
}
