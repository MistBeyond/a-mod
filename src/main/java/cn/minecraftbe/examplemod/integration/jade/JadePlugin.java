package cn.minecraftbe.examplemod.integration.jade;

import cn.minecraftbe.examplemod.ExampleMod;
import cn.minecraftbe.examplemod.block.TestMachine;
import cn.minecraftbe.examplemod.block.entity.TestMachineBlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.view.HideThingsExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;

import java.util.HashSet;
import java.util.Set;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(TestMachineProvider.Client.INSTANCE, TestMachine.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(TestMachineProvider.INSTANCE, TestMachineBlockEntity.class);
        hideItemStorage(registration, TestMachineBlockEntity.class);
    }

    @SuppressWarnings("unchecked")
    public void hideItemStorage(IWailaCommonRegistration registration, Class<?> blockOrBlockEntityClass) {
        registration.registerItemStorage(
                (IServerExtensionProvider<ItemStack>) (Object) HideThingsExtensionProvider.instance(),
                blockOrBlockEntityClass
        );
    }

    public interface Ids {
        Set<Identifier> IDENTIFIERS = new HashSet<>();
        Identifier TEST_MACHINE = thisMod("test_machine");

        static Identifier thisMod(String path) {
            var id = Identifier.fromNamespaceAndPath(ExampleMod.MODID, path);
            IDENTIFIERS.add(id);
            return id;
        }
    }


}
