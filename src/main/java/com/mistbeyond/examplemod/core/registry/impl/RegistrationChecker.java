package com.mistbeyond.examplemod.core.registry.impl;

import java.lang.annotation.Annotation;

/**
 * Base template for checkers that validate every class carrying a given registration annotation.
 * <p>
 * For each annotated class the inheritance requirement and the subclass-specific validation are
 * evaluated; concrete failures are grouped under a per-class summary.
 */
public abstract class RegistrationChecker implements Checker {
    private final ClassContainer classContainer;
    private final Class<? extends Annotation> annotation;
    private final Class<?> requiredSuperclass;
    private final CheckReport report;

    protected RegistrationChecker(
            ClassContainer classContainer,
            Class<? extends Annotation> annotation,
            Class<?> requiredSuperclass,
            CheckReport report
    ) {
        this.classContainer = classContainer;
        this.annotation = annotation;
        this.requiredSuperclass = requiredSuperclass;
        this.report = report;
    }

    @Override
    public final void check() {
        for (Class<?> clazz : classContainer.getAnnotatedBy(annotation)) {
            var errors = new CheckReport.Lazy("  ");
            if (!checkClass(clazz, errors)) {
                report.addErrorMessage(failureSummary(clazz));
                errors.addToError(report);
            }
        }
    }

    private boolean checkClass(Class<?> clazz, CheckReport.Adder errors) {
        return Checks.checkSubclass(clazz, requiredSuperclass, errors) & checkAnnotatedClass(clazz, errors);
    }

    /**
     * Validates the annotated class beyond the inheritance requirement.
     *
     * @param clazz  the annotated class under validation
     * @param errors sink for concrete failure messages
     * @return {@code true} when the class satisfies all requirements
     */
    protected abstract boolean checkAnnotatedClass(Class<?> clazz, CheckReport.Adder errors);

    /**
     * Returns the summary message shown above the concrete errors of a failing class.
     *
     * @param clazz the failing annotated class
     * @return summary message
     */
    protected abstract String failureSummary(Class<?> clazz);
}