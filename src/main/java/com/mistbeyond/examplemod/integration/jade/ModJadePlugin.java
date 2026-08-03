package com.mistbeyond.examplemod.integration.jade;

import com.mistbeyond.examplemod.block.TestMachine;
import com.mistbeyond.examplemod.block.entity.TestMachineBlockEntity;
import com.mistbeyond.examplemod.block.machine.SingleBlockMachine;
import com.mistbeyond.examplemod.integration.jade.provider.EUStorageProvider;
import com.mistbeyond.examplemod.integration.jade.provider.TestMachineProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.view.HideThingsExtensionProvider;

@WailaPlugin
public class ModJadePlugin implements IWailaPlugin {
    public static void hideItemStorage(IWailaCommonRegistration registration, Class<?> blockOrBlockEntityClass) {
        registration.registerItemStorage(HideThingsExtensionProvider.instance(), blockOrBlockEntityClass);
    }

    public static void hideEnergyStorage(IWailaCommonRegistration registration, Class<?> blockOrBlockEntityClass) {
        registration.registerEnergyStorage(HideThingsExtensionProvider.instance(), blockOrBlockEntityClass);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(TestMachineProvider.INSTANCE, TestMachineBlockEntity.class);
        hideItemStorage(registration, TestMachineBlockEntity.class);
        registration.registerBlockDataProvider(EUStorageProvider.INSTANCE, SingleBlockMachine.class);
        hideItemStorage(registration, SingleBlockMachine.class);
        hideEnergyStorage(registration, SingleBlockMachine.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(TestMachineProvider.Client.INSTANCE, TestMachine.class);
        registration.registerBlockComponent(EUStorageProvider.Client.INSTANCE, SingleBlockMachine.class);
    }
}
