package com.mistbeyond.examplemod.integration.jade;

import com.mistbeyond.examplemod.block.TestMachine;
import com.mistbeyond.examplemod.block.entity.TestMachineBlockEntity;
import com.mistbeyond.examplemod.block.machine.SingleBlockMachine;
import com.mistbeyond.examplemod.integration.jade.provider.EUStorageProvider;
import com.mistbeyond.examplemod.integration.jade.provider.TestMachineProvider;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.HideThingsExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;

@WailaPlugin
public class ModJadePlugin implements IWailaPlugin {
    @SuppressWarnings("unchecked")
    public static void hideItemStorage(IWailaCommonRegistration registration, Class<?> blockOrBlockEntityClass) {
        registration.registerItemStorage(
                (IServerExtensionProvider<ItemStack>) (Object) HideThingsExtensionProvider.instance(),
                blockOrBlockEntityClass
        );
    }

    @SuppressWarnings("unchecked")
    public static void hideEnergyStorage(IWailaCommonRegistration registration, Class<?> blockOrBlockEntityClass) {
        registration.registerEnergyStorage(
                (IServerExtensionProvider<EnergyView.Data>) (Object) HideThingsExtensionProvider.instance(),
                blockOrBlockEntityClass
        );
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(TestMachineProvider.INSTANCE, TestMachineBlockEntity.class);
        hideItemStorage(registration, TestMachineBlockEntity.class);
        registration.registerBlockDataProvider(EUStorageProvider.BLOCK, SingleBlockMachine.class);
        hideItemStorage(registration, SingleBlockMachine.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(TestMachineProvider.Client.INSTANCE, TestMachine.class);
        registration.registerBlockComponent(EUStorageProvider.Client.BLOCK, SingleBlockMachine.class);
    }
}
