package com.mistbeyond.examplemod.core.registry.impl;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.Range;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Checks {
    private Checks() {
    }

    @CanIgnoreReturnValue
    public static boolean checkAnnotatedStaticMethods(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            @Range(from = 1, to = Integer.MAX_VALUE) int expectedAmount,
            CheckReport.Adder report
    ) {
        var methods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(annotation))
                .filter(ReflectHelper::isStatic)
                .toList();
        if (methods.isEmpty()) {
            report.addMessage(String.format(
                    "No static method in '%s' is annotated with @%s.", clazz.getName(), annotation.getName()
            ));
            return false;
        }

        if (methods.size() != expectedAmount) {
            String methodNames = methods.stream()
                    .map(Method::getName)
                    .collect(Collectors.joining(", "));
            report.addMessage(String.format("Expected exactly %d @%s method in '%s', found %d: [%s].", expectedAmount,
                    annotation.getName(), clazz.getName(), methods.size(), methodNames
            ));
            return false;
        }
        return true;
    }

    @CanIgnoreReturnValue
    public static boolean checkNoParamsMethod(Method method, CheckReport.Adder report) {
        method.setAccessible(true);
        var count = method.getParameterCount();
        if (count != 0) {
            report.addMessage(String.format("Method '%s' has %d parameter(s), expected none.", method.getName(), count));
            return false;
        }
        return true;
    }

    @CanIgnoreReturnValue
    public static boolean checkParamType(Method method, Class<?> paramType, CheckReport.Adder report, boolean delegated) {
        method.setAccessible(true);
        if (!delegated && method.getParameterCount() != 1) {
            report.addMessage(String.format("Method '%s' in '%s' has incorrect parameter count, expected 1, actual %d", method.getName(), method.getDeclaringClass().getName(), method.getParameterCount()));
            return false;
        }
        var params = method.getParameterTypes();
        if (params[0] != paramType) {
            report.addMessage(String.format("The first param type of '%s' in '%s' is wrong, expected: %s, actual: %s", method.getName(), method.getDeclaringClass().getName(), paramType.getName(), params[0].getName()));
            return false;
        }
        return true;
    }

    @CanIgnoreReturnValue
    public static boolean checkParamType(Method method, Class<?> paramType1, Class<?> paramType2, CheckReport.Adder report, boolean delegated) {
        if (!checkParamType(method, paramType1, report, true)) {
            return false;
        }
        if (!delegated && method.getParameterCount() != 2) {
            report.addMessage(String.format("Method '%s' in %s has incorrect parameter count, expected 2, actual %d", method.getName(), method.getDeclaringClass(), method.getParameterCount()));
            return false;
        }
        var params = method.getParameterTypes();
        if (params[1] != paramType2) {
            report.addMessage(String.format("The second param type of '%s' in '%s' is wrong, expected: %s, actual: %s", method.getName(), method.getDeclaringClass(), paramType2.getName(), params[1].getName()));
            return false;
        }
        return true;
    }

    @CanIgnoreReturnValue
    public static boolean checkParamType(Method method, Class<?> paramType1, Class<?> paramType2, Class<?> paramType3, CheckReport.Adder report, boolean delegated) {
        if (!checkParamType(method, paramType1, paramType2, report, true)) {
            return false;
        }
        if (!delegated && method.getParameterCount() != 3) {
            report.addMessage(String.format("Method '%s' in %s has incorrect parameter count, expected 2, actual %d", method.getName(), method.getDeclaringClass(), method.getParameterCount()));
            return false;
        }
        var params = method.getParameterTypes();
        if (params[2] != paramType3) {
            report.addMessage(String.format("The third param type of '%s' in '%s' is wrong, expected: %s, actual: %s", method.getName(), method.getDeclaringClass(), paramType2.getName(), params[1].getName()));
            return false;
        }
        return true;
    }

    @CanIgnoreReturnValue
    public static boolean checkReturnType(Method method, Class<?> returnType, CheckReport.Adder report, boolean covariantReturnType) {
        if (returnType == method.getReturnType()) {
            return true;
        }
        if (covariantReturnType) {
            if (returnType.isAssignableFrom(method.getReturnType())) {
                return true;
            }
        }
        report.addMessage(String.format("The return type of method '%s' is wrong, expected: %s, actual: %s", method.getName(), returnType.getName(), method.getReturnType().getName()));
        return false;
    }

    public static boolean checkSubclass(Class<?> clazz, Class<?> superclass, CheckReport.Adder report) {
        if (!superclass.isAssignableFrom(clazz)) {
            report.addMessage(String.format("'%s' does not extend or implement '%s'", clazz.getName(), superclass.getName()));
            return false;
        }
        return true;
    }

    public static void checkAllRegistration(ClassContainer container, CheckReport report) {
        checkSideInsensitiveRegistration(container, report);
        checkClientOnlyRegistration(container, report);
        checkServerOnlyRegistration(container, report);
    }

    public static void checkSideInsensitiveRegistration(ClassContainer container, CheckReport report) {
        var checks = List.of(
                new SRAutoRegisterChecker.Block(container, report),
                new SRAutoRegisterChecker.Item(container, report),
                new FactoryAutoRegisterChecker.BlockEntityType(container, report),
                new FactoryAutoRegisterChecker.MenuType(container, report)
        );
        checks.forEach(Checker::check);
    }

    public static void checkServerOnlyRegistration(ClassContainer container, CheckReport report) {
    }

    public static void checkClientOnlyRegistration(ClassContainer container, CheckReport report) {
        var checks = List.of(
                new FactoryAutoRegisterChecker.ContainerScreen(container, report)
        );
        checks.forEach(Checker::check);
    }
}
