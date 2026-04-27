package cn.minecraftbe.examplemod;

import cn.minecraftbe.examplemod.block.Blocks;
import cn.minecraftbe.examplemod.block.entity.BlockEntityTypes;
import cn.minecraftbe.examplemod.inventory.MenuTypes;
import cn.minecraftbe.examplemod.item.Items;
import cn.minecraftbe.examplemod.screen.inventory.TestMachineScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.world.item.Items.GRASS_BLOCK;


@EventBusSubscriber(modid = ExampleMod.MODID)
public class Init {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExampleMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.examplemod")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(GRASS_BLOCK::getDefaultInstance)
            .build());

    private Init() {
    }

    public static void registerCommon(IEventBus modEventBus) {
        Blocks.BLOCKS.register(modEventBus);
        Items.ITEMS.register(modEventBus);
        BlockEntityTypes.BLOCK_ENTITIES.register(modEventBus);
        MenuTypes.MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                BlockEntityTypes.TEST_MACHINE.get(),
                (be, _) -> be.getItemHandler()
        );
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuTypes.TEST_MACHINE.get(), TestMachineScreen::new);
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == EXAMPLE_TAB.getKey()) {
            Items.ITEMS.getEntries().forEach(it -> event.accept(it.get()));
        }
    }

    public static void loadIntegration() {
    }

    public static boolean modLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static boolean jeiLoaded() {
        return modLoaded("jei");
    }
}
