package com.mistbeyond.examplemod.core.registry.impl;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CheckReport {
    public static final String PREFIX = "-";
    public static final CheckReport IGNORE;
    private final StringBuilder errorMessages = new StringBuilder("Check failed: \n");
    private final String indent;
    private boolean error = false;

    public CheckReport() {
        indent = "  ";
    }

    public CheckReport(String indent) {
        this.indent = indent;
    }

    private static void addMsg(StringBuilder sb, String msg, String indent, String prefix) {
        sb.append(indent);
        sb.append(prefix);
        sb.append(msg);
        sb.append("\n");
    }

    public void addErrorMessage(String message) {
        addErrorMessage(message, "");
    }

    public String buildErrorMsg() {
        return errorMessages.toString();
    }

    public boolean isCheckFailed() {
        return error;
    }

    public void throwIfFailed(Logger logger) throws IllegalStateException {
        if (isCheckFailed()) {
            logger.error(buildErrorMsg());
            throw new IllegalStateException("Check failed, see log for details.");
        }
        logger.info("All checks passed.");
    }

    private void addErrorMessage(String message, String extraIndent) {
        addMsg(errorMessages, message, indent.concat(extraIndent), PREFIX);
        error = true;
    }

    static {
        IGNORE = new CheckReport() {
            @Override
            public void addErrorMessage(String message) {
            }

            @Override
            public String buildErrorMsg() {
                return "";
            }

            @Override
            public boolean isCheckFailed() {
                return false;
            }

            @Override
            public void throwIfFailed(Logger logger) throws IllegalStateException {
            }
        };
    }

    @FunctionalInterface
    public interface Adder {
        void addMessage(String message);
    }

    public static final class Lazy implements Adder {
        private final List<String> buffer = new ArrayList<>();
        private final String indent;

        public Lazy() {
            indent = "";
        }

        public Lazy(String indent) {
            this.indent = indent;
        }

        @Override
        public void addMessage(String message) {
            buffer.add(message);
        }

        public void addToError(CheckReport report) {
            buffer.forEach(s -> report.addErrorMessage(s, indent));
            buffer.clear();
        }
    }
}
