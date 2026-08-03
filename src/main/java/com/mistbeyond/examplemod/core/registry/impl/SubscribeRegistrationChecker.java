package com.mistbeyond.examplemod.core.registry.impl;

import com.mistbeyond.examplemod.core.registry.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.lang.annotation.Annotation;

/**
 * Validates that every class annotated with a {@link RegisterBlock}, {@link RegisterItem},
 * {@link RegisterMenuType} or {@link RegisterContainerScreen} annotation exposes exactly one static
 * {@link SubscribeRegistration} method accepting the expected registration receiver.
 */
public abstract class SubscribeRegistrationChecker extends RegistrationChecker {
    private final Class<?> registrationClass;

    protected SubscribeRegistrationChecker(
            ClassContainer classContainer,
            Class<? extends Annotation> annotation,
            Class<?> registrationClass,
            Class<?> requiredSuperclass,
            CheckReport report
    ) {
        super(classContainer, annotation, requiredSuperclass, report);
        this.registrationClass = registrationClass;
    }

    @Override
    protected boolean checkAnnotatedClass(Class<?> clazz, CheckReport.Adder errors) {
        if (!Checks.checkAnnotatedStaticMethods(clazz, SubscribeRegistration.class, 1, errors)) {
            return false;
        }
        var method = ReflectHelper.getFirstAnnotatedStaticMethod(clazz, SubscribeRegistration.class);
        return Checks.checkParamType(method, registrationClass, errors);
    }

    @Override
    protected String failureSummary(Class<?> clazz) {
        return String.format("The class '%s' does not have a proper registration receiver.", clazz.getName());
    }

    public static final class Block extends SubscribeRegistrationChecker {
        public Block(ClassContainer classContainer, CheckReport report) {
            super(classContainer, RegisterBlock.class, BlockRegistration.class, net.minecraft.world.level.block.Block.class, report);
        }
    }

    public static final class Item extends SubscribeRegistrationChecker {
        public Item(ClassContainer classContainer, CheckReport report) {
            super(classContainer, RegisterItem.class, ItemRegistration.class, net.minecraft.world.item.Item.class, report);
        }
    }

    public static final class MenuType extends SubscribeRegistrationChecker {
        public MenuType(ClassContainer classContainer, CheckReport report) {
            super(classContainer, RegisterMenuType.class, MenuTypeRegistration.class, AbstractContainerMenu.class, report);
        }
    }

    public static final class ContainerScreen extends SubscribeRegistrationChecker {
        public ContainerScreen(ClassContainer classContainer, CheckReport report) {
            super(classContainer, RegisterContainerScreen.class, ContainerScreenRegistration.class, AbstractContainerScreen.class, report);
        }
    }
}
