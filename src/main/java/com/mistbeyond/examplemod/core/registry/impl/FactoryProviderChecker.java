package com.mistbeyond.examplemod.core.registry.impl;

import com.mistbeyond.examplemod.core.registry.ProvideFactory;
import com.mistbeyond.examplemod.core.registry.RegisterBlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.annotation.Annotation;

/**
 * Validates that every class annotated with a {@link RegisterBlockEntityType} annotation exposes exactly
 * one static {@link ProvideFactory} method with no parameters that returns the expected factory type.
 */
public abstract class FactoryProviderChecker extends RegistrationChecker {
    private final Class<?> factoryType;

    protected FactoryProviderChecker(
            ClassContainer classContainer,
            Class<? extends Annotation> annotation,
            Class<?> factoryType,
            Class<?> requiredSuperclass,
            CheckReport report
    ) {
        super(classContainer, annotation, requiredSuperclass, report);
        this.factoryType = factoryType;
    }

    @Override
    protected boolean checkAnnotatedClass(Class<?> clazz, CheckReport.Adder errors) {
        if (!Checks.checkAnnotatedStaticMethods(clazz, ProvideFactory.class, 1, errors)) {
            return false;
        }
        var method = ReflectHelper.getFirstAnnotatedStaticMethod(clazz, ProvideFactory.class);
        return Checks.checkNoParamsMethod(method, errors)
                & Checks.checkReturnType(method, factoryType, errors, true);
    }

    @Override
    protected String failureSummary(Class<?> clazz) {
        return String.format("The class '%s' does not have a proper factory provider.", clazz.getName());
    }

    public static final class BlockEntityType extends FactoryProviderChecker {
        public BlockEntityType(ClassContainer classContainer, CheckReport report) {
            super(classContainer, RegisterBlockEntityType.class, net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier.class, BlockEntity.class, report);
        }
    }
}
