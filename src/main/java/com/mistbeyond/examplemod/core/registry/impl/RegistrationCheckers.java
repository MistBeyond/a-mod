package com.mistbeyond.examplemod.core.registry.impl;

import java.util.List;

/**
 * Central registry of which {@link Checker checkers} apply to which distribution side.
 */
public final class RegistrationCheckers {
    private RegistrationCheckers() {
    }

    public static List<Checker> sideInsensitive(ClassContainer container, CheckReport report) {
        return List.of(
                new SubscribeRegistrationChecker.Block(container, report),
                new SubscribeRegistrationChecker.Item(container, report),
                new FactoryProviderChecker.BlockEntityType(container, report),
                new SubscribeRegistrationChecker.MenuType(container, report)
        );
    }

    public static List<Checker> clientOnly(ClassContainer container, CheckReport report) {
        return List.of(
                new SubscribeRegistrationChecker.ContainerScreen(container, report)
        );
    }
}
