package com.mistbeyond.examplemod.util;

import com.mistbeyond.examplemod.core.energy.EUEnergyHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public final class ItemUtil {
    private ItemUtil() {
    }

    public static boolean isBatteryLike(ItemStack stack, boolean allowEnergyConversion) {
        var energyHandler = stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack));
        return energyHandler != null && (allowEnergyConversion || energyHandler instanceof EUEnergyHandler);
    }
}
