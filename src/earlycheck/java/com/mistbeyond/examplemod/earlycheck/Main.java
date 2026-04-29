package com.mistbeyond.examplemod.earlycheck;

import com.google.common.base.Stopwatch;
import com.mistbeyond.examplemod.core.registry.impl.CheckReport;
import com.mistbeyond.examplemod.core.registry.impl.Checks;
import com.mistbeyond.examplemod.core.registry.impl.ClassContainer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
public class Main {
    private static final ClassLoader loader = Main.class.getClassLoader();
    private static final Stopwatch stopwatch = Stopwatch.createUnstarted();

    static void main(String[] args) {
        if (args.length < 2 || !args[0].equals("source")) {
            log.error("Wrong arguments: {}", Arrays.toString(args));
            throw new IllegalStateException("Wrong arguments");
        }
        var report = new CheckReport();

        var container = timed("class loading", () -> parseAndLoad(args[1]));
        timed("checking", () -> Checks.checkAllRegistration(container, report));

        report.throwIfFailed(log);
    }

    private static void timed(String taskName, Runnable runnable) {
        timed(taskName, () -> {
            runnable.run();
            return new Object();
        });
    }

    private static <T> T timed(String taskName, Supplier<T> s) {
        log.info("Start {}", taskName);
        stopwatch.reset().start();
        var ret = s.get();
        var d = stopwatch.stop().elapsed();
        log.info("Finished {}, took {}.{}s", taskName, d.toSeconds(), String.format("%03d", d.toMillisPart()));
        return ret;
    }

    private static ClassContainer parseAndLoad(String arg) {
        var paths = Arrays.stream(arg.split(";"))
                .map(String::trim)
                .map(Path::of)
                .filter(Files::exists)
                .toList();

        return loadClasses(getClassNames(paths));
    }

    private static List<String> getClassNames(List<Path> paths) {
        List<String> ret = new ArrayList<>(paths.size());
        for (Path path : paths) {
            try (Stream<Path> stream = Files.walk(path)) {
                stream.filter(Files::isRegularFile)
                        .map(path::relativize)
                        .filter(p -> p.toString().endsWith(".class"))
                        .map(p -> p.toString().replace(p.getFileSystem().getSeparator(), "."))
                        .map(s -> s.replace(".class", ""))
                        .forEach(ret::add);
            } catch (IOException e) {
                log.error("Caught an exception: ", e);
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    private static ClassContainer loadClasses(List<String> classNames) {
        var ret = new ClassContainer();

        for (String className : classNames) {
            try {
                ret.addClass(loader.loadClass(className));
            } catch (ClassNotFoundException e) {
                log.error("Caught an exception: ", e);
                throw new RuntimeException(e);
            }
        }
        return ret;
    }
}
