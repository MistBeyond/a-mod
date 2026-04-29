package com.mistbeyond.examplemod.core.registry.impl;

import com.mistbeyond.examplemod.core.registry.ProvideFactory;
import com.mistbeyond.examplemod.core.registry.RegisterBlockEntityType;
import com.mistbeyond.examplemod.core.registry.RegisterContainerScreen;
import com.mistbeyond.examplemod.core.registry.RegisterMenuType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.annotation.Annotation;
import java.util.List;

public abstract class FactoryAutoRegisterChecker implements Checker {
    protected final List<Class<?>> annotatedClasses;
    protected final Class<?> superClass;
    protected final Class<?> factoryClass;
    protected final CheckReport report;

    public FactoryAutoRegisterChecker(ClassContainer classContainer, Class<? extends Annotation> annotation, Class<?> factoryClass, Class<?> superClass, CheckReport report) {
        this.factoryClass = factoryClass;
        this.superClass = superClass;
        this.annotatedClasses = classContainer.getAnnotatedBy(annotation);
        this.report = report;
    }

    private static boolean checkFactory(Class<?> clazz, Class<? extends Annotation> annotation, Class<?> factoryType, CheckReport.Adder adder) {
        if (Checks.checkAnnotatedStaticMethods(clazz, annotation, 1, adder)) {
            var method = ReflectHelper.getFirstAnnotatedStaticMethod(clazz, annotation);
            return Checks.checkNoParamsMethod(method, adder)
                    & Checks.checkReturnType(method, factoryType, adder, true);
        }
        return false;
    }

    @Override
    public boolean inheritanceChainCheck(Class<?> clazz, CheckReport.Adder adder) {
        return Checks.checkSubclass(clazz, superClass, adder);
    }

    @Override
    public void check() {
        var lazy = new CheckReport.Lazy("  ");
        for (Class<?> clazz : annotatedClasses) {
            inheritanceChainCheck(clazz, report::addErrorMessage);
            if (!checkFactory(clazz, lazy)) {
                report.addErrorMessage(String.format("The class '%s' does not have a proper factory provider.", clazz.getName()));
                lazy.addToError(report);
            }
        }
    }

    public boolean checkFactory(Class<?> clazz, CheckReport.Adder adder) {
        return checkFactory(clazz, ProvideFactory.class, factoryClass, adder);
    }

    public static class ContainerScreen extends FactoryAutoRegisterChecker {
        public ContainerScreen(ClassContainer classContainer, CheckReport report) {
            super(classContainer, RegisterContainerScreen.class, MenuScreens.ScreenConstructor.class, AbstractContainerScreen.class, report);
        }
    }

    public static class MenuType extends FactoryAutoRegisterChecker {
        public MenuType(ClassContainer classContainer, CheckReport report) {
            super(classContainer, RegisterMenuType.class, net.minecraft.world.inventory.MenuType.MenuSupplier.class, AbstractContainerMenu.class, report);
        }
    }

    public static class BlockEntityType extends FactoryAutoRegisterChecker {
        public BlockEntityType(ClassContainer classContainer, CheckReport report) {
            super(classContainer, RegisterBlockEntityType.class, net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier.class, BlockEntity.class, report);
        }
    }
}
