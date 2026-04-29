package com.mistbeyond.examplemod.core.registry.impl;

import com.mistbeyond.examplemod.core.registry.RegisterBlock;
import com.mistbeyond.examplemod.core.registry.RegisterItem;
import com.mistbeyond.examplemod.core.registry.SubscribeRegistration;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Abbreviation of SubscribeRegistrationAutoRegisterChecker
 */
public abstract class SRAutoRegisterChecker implements Checker {
    protected final List<Class<?>> annotatedClasses;
    protected final Class<?> superClass;
    protected final Class<? extends BaseRegistration<?, ?, ?, ?>> registrationClass;
    protected final CheckReport report;

    public SRAutoRegisterChecker(ClassContainer classContainer, Class<? extends Annotation> annotation, Class<? extends BaseRegistration<?, ?, ?, ?>> registrationClass, Class<?> superClass, CheckReport report) {
        this.registrationClass = registrationClass;
        this.superClass = superClass;
        this.annotatedClasses = classContainer.getAnnotatedBy(annotation);
        this.report = report;
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
            if (!staticRegistrationCheck(clazz, lazy)) {
                report.addErrorMessage(String.format("The class '%s' does not have a proper registration receiver.", clazz.getName()));
                lazy.addToError(report);
            }
        }
    }

    public boolean staticRegistrationCheck(Class<?> clazz, CheckReport.Adder adder) {
        if (Checks.checkAnnotatedStaticMethods(clazz, SubscribeRegistration.class, 1, adder)) {
            var method = ReflectHelper.getAnnotatedStaticMethods(clazz, SubscribeRegistration.class).getFirst();
            return Checks.checkParamType(method, registrationClass, adder, false);
        }
        return false;
    }

    public static final class Block extends SRAutoRegisterChecker {
        public Block(ClassContainer classContainer, CheckReport report) {
            super(classContainer, RegisterBlock.class, BlockRegistration.class, net.minecraft.world.level.block.Block.class, report);
        }
    }

    public static final class Item extends SRAutoRegisterChecker {
        public Item(ClassContainer classContainer, CheckReport report) {
            super(classContainer, RegisterItem.class, ItemRegistration.class, net.minecraft.world.item.Item.class, report);
        }
    }
}
