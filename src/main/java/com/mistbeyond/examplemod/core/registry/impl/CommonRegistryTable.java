package com.mistbeyond.examplemod.core.registry.impl;

import com.google.common.base.Stopwatch;
import com.mistbeyond.examplemod.core.registry.*;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
public class CommonRegistryTable extends CommonRegistrar {
    /**
     * Packages that must be excluded from the runtime check, since they are checked elsewhere or side-specific.
     */
    private static final List<String> EXCLUDED_PACKAGES = List.of(
            "com.mistbeyond.examplemod.earlycheck",
            "com.mistbeyond.examplemod.data",
            "com.mistbeyond.examplemod.integration");

    private final BlockRegistration blockRegistration;
    private final ItemRegistration itemRegistration;
    private final MenuTypeRegistration menuTypeRegistration;
    private final ContainerScreenRegistration containerScreenRegistration;
    private final ExclusionPolicy exclusionPolicy = new ExclusionPolicy(EXCLUDED_PACKAGES);

    private boolean insensitiveProcessed = false;
    private boolean clientProcessed = false;

    public CommonRegistryTable(String modId, DeferredRegister.Blocks blockRegister, DeferredRegister.Items itemRegister, DeferredRegister<BlockEntityType<?>> blockEntityRegister, DeferredRegister<MenuType<?>> menuRegister) {
        super(modId, blockRegister, itemRegister, blockEntityRegister, menuRegister);
        blockRegistration = new BlockRegistration(modId, block, blockRegister);
        itemRegistration = new ItemRegistration(modId, item, itemRegister);
        menuTypeRegistration = new MenuTypeRegistration(modId, menuType, menuRegister);
        containerScreenRegistration = new ContainerScreenRegistration(modId, containerScreen);
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

    /**
     * Resolves every class annotated with {@code annotation} and invokes its unique
     * {@link SubscribeRegistration} static method with {@code receiver}.
     */
    private static <T, R> void invokeRegistration(ModFileScanData scanResult, Class<? extends Annotation> annotation, Class<T> superclass, R receiver) {
        for (var data : ScanDataHelper.annotationDataOf(scanResult, annotation)) {
            invokeSubscribeRegistration(ScanDataHelper.resolveAndValidate(data, superclass), receiver);
        }
    }

    /**
     * Invokes the unique {@link SubscribeRegistration} static method of {@code clazz} with {@code receiver}.
     */
    private static void invokeSubscribeRegistration(Class<?> clazz, Object receiver) {
        try {
            ReflectHelper.getFirstAnnotatedStaticMethod(clazz, SubscribeRegistration.class).invoke(null, receiver);
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
            checkClientOnly(new ClassContainer(SideSifter.loadSensitive(scanResult, Dist.CLIENT, exclusionPolicy)));
            processContainerScreen(scanResult);
            validateClientFamilyConsistency();
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
            checkInsensitive(new ClassContainer(SideSifter.loadInsensitive(scanResult, exclusionPolicy)));
            // no checks during processing
            processBlock(scanResult);
            processItem(scanResult);
            processMenu(scanResult);
            processBlockEntity(scanResult);
            validateCommonFamilyConsistency();
            insensitiveProcessed = true;
        }
    }

    private void processBlock(ModFileScanData scanResult) {
        for (var data : ScanDataHelper.annotationDataOf(scanResult, RegisterBlock.class)) {
            var clazz = ScanDataHelper.resolveAndValidate(data, Block.class);
            var v = data.annotationData().get("registerBlockItem");
            blockRegistration.setAllowRegisteringBlockItem(v == null || (boolean) v);
            invokeSubscribeRegistration(clazz, blockRegistration);
        }
        blockRegistration.registerBlockItem(item, itemRegister);
    }

    private void processItem(ModFileScanData scanResult) {
        invokeRegistration(scanResult, RegisterItem.class, Item.class, itemRegistration);
    }

    private void processMenu(ModFileScanData scanResult) {
        invokeRegistration(scanResult, RegisterMenuType.class, AbstractContainerMenu.class, menuTypeRegistration);
    }

    private void processBlockEntity(ModFileScanData scanResult) {
        for (var data : ScanDataHelper.annotationDataOf(scanResult, RegisterBlockEntityType.class)) {
            var clazz = ScanDataHelper.resolveAndValidate(data, BlockEntity.class);
            var name = (String) data.annotationData().get("value");
            var id = Identifier.fromNamespaceAndPath(this.modId, name);
            if (this.blockEntityType.containsKey(id)) {
                throw new IllegalStateException("BlockEntityType '" + name + "' was registered twice.");
            }
            this.blockEntityType.put(id, blockEntityRegister.register(name, () -> new BlockEntityType<>(getFactory(clazz), block.get(id).value())));
        }
    }

    private void processContainerScreen(ModFileScanData scanResult) {
        invokeRegistration(scanResult, RegisterContainerScreen.class, AbstractContainerScreen.class, containerScreenRegistration);
    }

    /**
     * Ensures every registered menu/block entity type id has a matching block registered under the same id.
     */
    private void validateCommonFamilyConsistency() {
        var report = new CheckReport();
        for (var id : menuType.keySet()) {
            if (!block.containsKey(id)) {
                report.addErrorMessage(String.format("MenuType '%s' has no matching Block registered.", id.getPath()));
            }
        }
        for (var id : blockEntityType.keySet()) {
            if (!block.containsKey(id)) {
                report.addErrorMessage(String.format("BlockEntityType '%s' has no matching Block registered.", id.getPath()));
            }
        }
        report.throwIfFailed(log);
    }

    /**
     * Ensures every registered container screen id has a matching menu type.
     */
    private void validateClientFamilyConsistency() {
        var report = new CheckReport();
        for (var id : containerScreen.keySet()) {
            if (!menuType.containsKey(id)) {
                report.addErrorMessage(String.format("ContainerScreen '%s' has no matching MenuType registered.", id.getPath()));
            }
        }
        report.throwIfFailed(log);
    }
}
