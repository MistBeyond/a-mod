package com.mistbeyond.examplemod;

import com.mistbeyond.examplemod.block.Blocks;
import com.mistbeyond.examplemod.block.entity.TestMachineBlockEntity;
import com.mistbeyond.examplemod.block.entity.machine.CrusherEntity;
import com.mistbeyond.examplemod.core.energy.EUEnergyHandler;
import com.mistbeyond.examplemod.core.registry.impl.CommonRegistrar;
import com.mistbeyond.examplemod.item.Items;
import com.mistbeyond.examplemod.item.componet.ModDataComponents;
import com.mistbeyond.examplemod.recipe.RecipeSerializers;
import com.mistbeyond.examplemod.recipe.RecipeTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.world.item.Items.GRASS_BLOCK;


@EventBusSubscriber(modid = Ids.MODID)
public class Init {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB, Ids.MODID
    );
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Ids.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, Ids.MODID);
    public static final CommonRegistrar REGISTRAR = CommonRegistrar.of(Ids.MODID, Blocks.BLOCKS, Items.ITEMS, BLOCK_ENTITIES, MENU_TYPES);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register(
            "example_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.examplemod")) //The language key for the title of your CreativeModeTab
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(GRASS_BLOCK::getDefaultInstance)
                    .build()
    );

    private Init() {
    }

    public static void registerCommon(IEventBus modEventBus, ModContainer modContainer) {
        RecipeTypes.RECIPE_TYPES.register(modEventBus);
        RecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        REGISTRAR.registerCommon(modEventBus, modContainer);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    public static void registerClient(IEventBus modEventBus, ModContainer modContainer) {
        REGISTRAR.registerClient(modEventBus, modContainer);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                REGISTRAR.<TestMachineBlockEntity>blockEntityTyped(Ids.TEST_MACHINE),
                (be, _) -> be.getItemHandler()
        );
        {
            BlockEntityType<CrusherEntity> crusher = REGISTRAR.blockEntityTyped(Ids.CRUSHER);
            event.registerBlockEntity(
                    Capabilities.Energy.BLOCK,
                    crusher,
                    (be, _) -> be.getEnergyHandler()
            );
            event.registerBlockEntity(
                    Capabilities.Item.BLOCK,
                    crusher,
                    (be, _) -> be.getItemHandler()
            );
        }
        event.registerItem(
                Capabilities.Energy.ITEM,
                (_, access) -> EUEnergyHandler.forItem(access),
                Items.TEST_ITEM
        );
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        REGISTRAR.registerContainerScreens(event);
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == EXAMPLE_TAB.getKey()) {
            Items.ITEMS.getEntries().forEach(it -> event.accept(it.get()));
        }
    }
}
