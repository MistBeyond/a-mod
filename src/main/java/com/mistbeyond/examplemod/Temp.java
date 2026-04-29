package com.mistbeyond.examplemod;

import com.mistbeyond.examplemod.core.energy.EUEnergyHandler;
import com.mistbeyond.examplemod.item.ElectricItem;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;

@EventBusSubscriber(modid = Ids.MODID)
public class Temp {
    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        var stack = event.getItemStack();
        if (stack.getItem() instanceof ElectricItem) {
            var energyHandler = stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack));
            var player = event.getPlayer();
            if (player != null && energyHandler != null) {
                var msg = Component.literal("Amount(FE):")
                        .append(String.valueOf(energyHandler.getAmountAsLong()))
                        .append(Component.literal(" Capacity(FE):"))
                        .append(String.valueOf(energyHandler.getCapacityAsLong()));
                if (energyHandler instanceof EUEnergyHandler euEnergyHandler) {
                    msg.append(Component.literal(" Amount(EU):"))
                            .append(String.valueOf(euEnergyHandler.getEUAmount()))
                            .append(Component.literal(" Capacity(EU):"))
                            .append(String.valueOf(euEnergyHandler.getEUCapacity()));
                }
                player.sendOverlayMessage(msg);
            }
        }
    }

}
