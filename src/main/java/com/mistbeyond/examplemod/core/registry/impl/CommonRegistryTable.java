package com.mistbeyond.examplemod.core.registry.impl;

import com.google.common.base.Stopwatch;
import com.mistbeyond.examplemod.core.registry.*;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforgespi.language.ModFileScanData;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

// todo:refactor the whole system in the future
@Slf4j
public class CommonRegistryTable extends CommonRegistrar {
    private final BlockRegistration blockRegistration;
    private final ItemRegistration itemRegistration;

    private boolean insensitiveProcessed = false;
    private boolean clientProcessed = false;

    public CommonRegistryTable(String modId, DeferredRegister.Blocks blockRegister, DeferredRegister.Items itemRegister, DeferredRegister<BlockEntityType<?>> blockEntityRegister, DeferredRegister<MenuType<?>> menuRegister) {
        super(modId, blockRegister, itemRegister, blockEntityRegister, menuRegister);
        blockRegistration = new BlockRegistration(modId, block, blockRegister);
        itemRegistration = new ItemRegistration(modId, item, itemRegister);
    }

    /**
     * Gets factory from provider (usually it's the only annotated static method), no runtime checks.
     *
     * @param <F> the factory
     * @throws NoSuchElementException when there is no a factory provider.
     */
    @SuppressWarnings("unchecked")
    private static <F> F getFactory(Class<?> clazz) throws NoSuchElementException {
        try {
            return (F) ReflectHelper.getFirstAnnotatedStaticMethod(clazz, ProvideFactory.class).invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void checkClientOnly(ClassContainer container) {
        log.info("Starting check for client-only classes");
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        var report = new CheckReport();
        Checks.checkClientOnlyRegistration(container, report);
        var duration = stopwatch.stop().elapsed();
        log.info("Finished check for client-only classes, took {}.{}s", duration.toSeconds(), String.format("%03d", duration.toMillisPart()));
        report.throwIfFailed(log);
    }

    private static void checkInsensitive(ClassContainer container) {
        log.info("Starting check for side-insensitive classes");
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        var report = new CheckReport();
        Checks.checkSideInsensitiveRegistration(container, report);
        var duration = stopwatch.stop().elapsed();
        log.info("Finished check for side-insensitive classes, took {}.{}s", duration.toSeconds(), String.format("%03d", duration.toMillisPart()));
        report.throwIfFailed(log);
    }

    public void registerCommon(IEventBus modEventBus, ModContainer modContainer) {
        processSideInsensitive(modContainer);

        blockRegister.register(modEventBus);
        itemRegister.register(modEventBus);
        blockEntityRegister.register(modEventBus);
        menuRegister.register(modEventBus);
    }

    public void registerClient(IEventBus modEventBus, ModContainer modContainer) {
        processClientSide(modContainer);
    }

    private void processClientSide(ModContainer modContainer) {
        if (!clientProcessed) {
            var scanResult = modContainer.getModInfo()
                    .getOwningFile()
                    .getFile()
                    .getScanResult();
            // check
            checkClientOnly(new ClassContainer(NeoApiHelper.loadSideSensitiveClasses(scanResult, Dist.CLIENT)));
            try {
                processContainerScreen(scanResult);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
            clientProcessed = true;
        }
    }

    private void processSideInsensitive(ModContainer modContainer) {
        if (!insensitiveProcessed) {
            var scanResult = modContainer.getModInfo()
                    .getOwningFile()
                    .getFile()
                    .getScanResult();
            // check
            checkInsensitive(new ClassContainer(NeoApiHelper.loadSideInsensitiveClasses(scanResult)));
            try {
                // no checks during processing
                processBlock(scanResult);
                processItem(scanResult);
                processMenu(scanResult);
                processBlockEntity(scanResult);
            } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            insensitiveProcessed = true;
        }
    }


    private void processBlock(ModFileScanData scanResult) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        var annotationData = NeoApiHelper.getNeoAnnotationDataBy(scanResult, RegisterBlock.class);
        for (var data : annotationData) {
            var clazz = NeoApiHelper.resolveAndValidate(data, Block.class);
            var v = data.annotationData().get("registerBlockItem");
            blockRegistration.setAllowRegisteringBlockItem(v == null || (boolean) v);
            ReflectHelper.getFirstAnnotatedStaticMethod(clazz, SubscribeRegistration.class).invoke(null, blockRegistration);
        }
        blockRegistration.registerBlockItem(item, itemRegister);
    }

    private void processItem(ModFileScanData scanResult) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        var annotationData = NeoApiHelper.getNeoAnnotationDataBy(scanResult, RegisterItem.class);
        for (var data : annotationData) {
            var clazz = NeoApiHelper.resolveAndValidate(data, Item.class);
            ReflectHelper.getFirstAnnotatedStaticMethod(clazz, SubscribeRegistration.class).invoke(null, itemRegistration);
        }
    }

    private void processMenu(ModFileScanData scanResult) throws ClassNotFoundException {
        var annotationData = NeoApiHelper.getNeoAnnotationDataBy(scanResult, RegisterMenuType.class);
        for (var data : annotationData) {
            var clazz = NeoApiHelper.resolveAndValidate(data, AbstractContainerMenu.class);
            var name = (String) data.annotationData().get("value");
            var id = Identifier.fromNamespaceAndPath(this.modId, name);
            this.menuType.put(id, menuRegister.register(name, () -> new MenuType<>(getFactory(clazz), FeatureFlags.DEFAULT_FLAGS)));
        }
    }

    private void processBlockEntity(ModFileScanData scanResult) throws ClassNotFoundException {
        var annotationData = NeoApiHelper.getNeoAnnotationDataBy(scanResult, RegisterBlockEntityType.class);
        for (var data : annotationData) {
            var clazz = NeoApiHelper.resolveAndValidate(data, BlockEntity.class);
            var name = (String) data.annotationData().get("value");
            var id = Identifier.fromNamespaceAndPath(this.modId, name);
            this.blockEntityType.put(id, blockEntityRegister.register(name, () -> new BlockEntityType<>(getFactory(clazz), block.get(id).value())));
        }
    }

    @SuppressWarnings("unchecked")
    private <M extends AbstractContainerMenu, S extends AbstractContainerScreen<M>> void processContainerScreen(ModFileScanData scanResult) throws ClassNotFoundException {
        var annotationData = NeoApiHelper.getNeoAnnotationDataBy(scanResult, RegisterContainerScreen.class);
        for (var data : annotationData) {
            var clazz = (Class<S>) NeoApiHelper.resolveAndValidate(data, AbstractContainerScreen.class);
            var name = (String) data.annotationData().get("value");
            var id = Identifier.fromNamespaceAndPath(this.modId, name);
            this.containerScreen.put(id, getFactory(clazz));
        }
    }
}
