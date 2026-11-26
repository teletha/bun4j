/*
 * Copyright (C) 2026 The BUN4J Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bun4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import bun4j.internal.BunInstaller;
import bun4j.internal.CommandRunner;
import bun4j.internal.InstallOption;
import bun4j.internal.UpdateOption;

/**
 * The entry point of bun4j.
 *
 * <pre>
 * Bun.build(o -&gt; o.entry("src/index.ts").outdir("dist").minify(true));
 * Bun.bundle(o -&gt; o.entry("src/index.tsx").framework(Framework.REACT).dts(true));
 * </pre>
 */
public final class Bun {

    /** Caches whether a bun executable provides the built-in linter. */
    private static final Map<Path, Boolean> NATIVE_LINT = new ConcurrentHashMap();

    /** Ensures that installation and update run only once in this JVM. */
    private static final AtomicBoolean ENSURED = new AtomicBoolean();

    /** The default interval between two automatic update checks. */
    private static final String DEFAULT_UPDATE_INTERVAL = "P7D";

    /** The directory which stores the timestamp of the last update check. */
    static Path stateDirectory = Path.of(System.getProperty("user.home"), ".bun4j");

    /** The minimum interval between two automatic update checks. */
    private static volatile Duration updateInterval = readInterval();

    /** Controls the automatic installation and update of the bun runtime. */
    private static volatile boolean autoManage = !"false".equalsIgnoreCase(System.getProperty("bun4j.autoManage", "true"));

    private Bun() {
    }

    /**
     * Enables or disables the automatic install and update which runs before the first bun command
     * in this JVM. The initial value can also be set with the {@code bun4j.autoManage} system
     * property.
     *
     * @param enabled Whether to manage bun automatically.
     */
    public static void autoManage(boolean enabled) {
        autoManage = enabled;
    }

    /**
     * Returns the minimum interval between two automatic update checks.
     *
     * @return A duration.
     */
    public static Duration updateInterval() {
        return updateInterval;
    }

    /**
     * Changes the minimum interval between two automatic update checks. The initial value can be
     * set with the {@code bun4j.updateInterval} system property (for example {@code P7D}).
     *
     * @param interval A duration.
     */
    public static void updateInterval(Duration interval) {
        if (interval != null && !interval.isNegative()) {
            updateInterval = interval;
        }
    }

    /**
     * Returns when bun was checked for updates the last time.
     *
     * @return An instant, or null if no check has been recorded.
     */
    public static Instant lastUpdateCheck() {
        try {
            String value = Files.readString(stateFile()).strip();
            return value.isEmpty() ? null : Instant.ofEpochMilli(Long.parseLong(value));
        } catch (Exception e) {
            return null;
        }
    }

    private static Duration readInterval() {
        try {
            return Duration.parse(System.getProperty("bun4j.updateInterval", DEFAULT_UPDATE_INTERVAL));
        } catch (RuntimeException e) {
            return Duration.parse(DEFAULT_UPDATE_INTERVAL);
        }
    }

    private static Path stateFile() {
        return stateDirectory.resolve("last-update.txt");
    }

    static boolean needsUpdateCheck() {
        Instant last = lastUpdateCheck();
        return last == null || !Instant.now().minus(updateInterval).isBefore(last);
    }

    static void recordUpdateCheck() {
        try {
            Files.createDirectories(stateDirectory);
            Files.writeString(stateFile(), String.valueOf(System.currentTimeMillis()), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // A missing marker only causes an extra check on the next start.
        }
    }

    /**
     * Returns whether a bun runtime is available.
     *
     * @return true if bun is installed locally or available on the PATH.
     */
    public static boolean installed() {
        return BunInstaller.resolve() != null;
    }

    /**
     * Returns the bun runtime which bun4j uses.
     *
     * @return The installation, or null if bun is not available.
     */
    public static Installation installation() {
        return BunInstaller.resolve();
    }

    /**
     * Returns the path to the bun executable.
     *
     * @return An executable path.
     * @throws BunException If bun is not installed.
     */
    public static Path executable() {
        ensureBun();
        Installation installation = installation();
        if (installation == null) {
            throw new BunException("bun is not installed and could not be installed automatically.");
        }
        return installation.executable();
    }

    /**
     * Returns the version of the available bun, or an empty string if bun is not installed.
     *
     * @return A version string.
     */
    public static String version() {
        Installation installation = installation();
        return installation == null ? "" : installation.version();
    }

    /**
     * Runs {@code bun build} with the default options.
     *
     * @return The command result.
     */
    public static Result build() {
        return build(new BuildOption());
    }

    public static Result build(Function<BuildOption, BuildOption> configurator) {
        return build(configure(configurator, new BuildOption()));
    }

    public static Result build(BuildOption option) {
        return run(option);
    }

    /**
     * Runs {@code bun lint} with the default options.
     *
     * @return The command result.
     */
    public static Result lint() {
        return lint(new LintOption());
    }

    public static Result lint(Function<LintOption, LintOption> configurator) {
        return lint(configure(configurator, new LintOption()));
    }

    public static Result lint(LintOption option) {
        Path executable = option.executable() != null ? option.executable() : executable();
        if (option.fallback() && !supportsNativeLint(executable)) {
            return CommandRunner.run(executable, option.fallbackArguments(), option);
        }
        return CommandRunner.run(executable, option.arguments(), option);
    }

    /**
     * Detects whether bun ships the native {@code bun lint} command. The result is cached per
     * executable because the check starts an extra process.
     */
    private static boolean supportsNativeLint(Path executable) {
        return NATIVE_LINT.computeIfAbsent(executable.toAbsolutePath().normalize(), key -> {
            try {
                Result probe = CommandRunner.run(key, List.of("lint", "--help"), new LintOption().timeout(Duration.ofSeconds(15)));
                return probe.succeeded() && probe.stdout().contains("bun lint");
            } catch (BunException e) {
                return false;
            }
        });
    }

    /**
     * Runs {@code bun fmt} with the default options.
     *
     * @return The command result.
     */
    public static Result format() {
        return format(new FormatOption());
    }

    public static Result format(Function<FormatOption, FormatOption> configurator) {
        return format(configure(configurator, new FormatOption()));
    }

    public static Result format(FormatOption option) {
        return run(option);
    }

    /**
     * Bundles a library through the JavaScript {@code Bun.build()} API.
     *
     * @param option The bundle options.
     * @return The command result.
     */
    public static Result bundle(BundleOption option) {
        return bundle((Function<BundleOption, BundleOption>) null, option);
    }

    public static Result bundle(Function<BundleOption, BundleOption> configurator) {
        return bundle(configurator, new BundleOption());
    }

    private static Result bundle(Function<BundleOption, BundleOption> configurator, BundleOption template) {
        BundleOption option = configure(configurator, template);
        Path executable = option.executable() != null ? option.executable() : executable();
        Path directory = option.directory() == null ? Path.of("").toAbsolutePath() : option.directory().toAbsolutePath();
        option.projectDirectory(directory);

        installPlugins(executable, option, directory);

        Path script = directory.resolve(".bun4j-build-" + UUID.randomUUID() + ".mjs");
        try {
            Files.writeString(script, option.script(), StandardCharsets.UTF_8);
            Result result = CommandRunner.run(executable, List.of(script.toString()), option);
            if (result.succeeded() && option.generatePackageJson()) {
                generatePackageJson(directory.resolve(option.outdir()), option);
            }
            return result;
        } catch (IOException e) {
            throw new BunException("Cannot write the generated build script " + script + ".", e);
        } finally {
            if (!option.keepScript()) {
                try {
                    Files.deleteIfExists(script);
                } catch (IOException ignored) {
                    // The temporary script is best effort only.
                }
            }
        }
    }

    /**
     * Installs framework plugins which are not present in node_modules yet.
     */
    private static void installPlugins(Path executable, BundleOption option, Path directory) {
        if (!option.installPlugins()) {
            return;
        }
        List<String> missing = new ArrayList();
        for (String specifier : option.requiredPackages()) {
            String name = packageName(specifier);
            if (!Files.isDirectory(directory.resolve("node_modules").resolve(name))) {
                missing.add(specifier);
            }
        }
        if (missing.isEmpty()) {
            return;
        }

        List<String> arguments = new ArrayList();
        arguments.add("add");
        arguments.add("-D");
        arguments.addAll(missing);
        Result result = CommandRunner.run(executable, arguments, option);
        result.requireSuccess();
    }

    private static void generatePackageJson(Path outdir, BundleOption option) {
        Path file = outdir.resolve("package.json");
        if (Files.exists(file)) {
            return;
        }
        try {
            Files.createDirectories(outdir);
            Files.writeString(file, option.packageJson(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BunException("Cannot generate " + file + ".", e);
        }
    }

    static String packageName(String specifier) {
        int start = specifier.startsWith("@") ? specifier.indexOf('/', 1) + 1 : 0;
        int version = specifier.indexOf('@', start);
        return version == -1 ? specifier : specifier.substring(0, version);
    }

    private static Result run(Option<?> option) {
        Path executable = option.executable() != null ? option.executable() : executable();
        return CommandRunner.run(executable, option.arguments(), option);
    }

    /**
     * Installs bun if it is missing and updates it to the latest version on the first command of
     * this JVM. A failed update is ignored so that the already installed runtime can still be used.
     */
    private static void ensureBun() {
        if (!autoManage || !ENSURED.compareAndSet(false, true)) {
            return;
        }

        Installation current = BunInstaller.resolve();
        if (current == null) {
            BunInstaller.install(new InstallOption());
            recordUpdateCheck();
        } else if (needsUpdateCheck()) {
            UpdateOption update = new UpdateOption().to(current.directory()).executable(current.executable());
            try {
                BunInstaller.update(update);
            } catch (BunException ignored) {
                // Keep the working installation when the network is unavailable.
            }
            recordUpdateCheck();
        }
    }

    private static <T> T configure(Function<T, T> configurator, T option) {
        return configurator == null ? option : configurator.apply(option);
    }
}
