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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The options of {@code bun install}, {@code bun add}, {@code bun remove} and {@code bun update}.
 */
public class DependencyOption extends Option<DependencyOption> {

    private final List<String> packages = new ArrayList();

    private DependencyOperation operation = DependencyOperation.INSTALL;
    private boolean dev;
    private boolean optional;
    private boolean peer;
    private boolean exact;
    private boolean noSave;
    private boolean frozenLockfile;
    private boolean production;
    private boolean dryRun;
    private boolean offline;
    private boolean force;
    private boolean ignoreScripts;
    private String registry;
    private Linker linker;
    private String cacheDir;
    private String config;

    DependencyOption operation(DependencyOperation operation) {
        this.operation = operation;
        return this;
    }

    public DependencyOption packageNames(String... packages) {
        for (String name : packages) {
            if (name != null && !name.isBlank()) {
                this.packages.add(name);
            }
        }
        return this;
    }

    public DependencyOption dev(boolean enabled) {
        this.dev = enabled;
        return this;
    }

    public DependencyOption optional(boolean enabled) {
        this.optional = enabled;
        return this;
    }

    public DependencyOption peer(boolean enabled) {
        this.peer = enabled;
        return this;
    }

    public DependencyOption exact(boolean enabled) {
        this.exact = enabled;
        return this;
    }

    public DependencyOption noSave(boolean enabled) {
        this.noSave = enabled;
        return this;
    }

    public DependencyOption frozenLockfile(boolean enabled) {
        this.frozenLockfile = enabled;
        return this;
    }

    public DependencyOption production(boolean enabled) {
        this.production = enabled;
        return this;
    }

    public DependencyOption dryRun(boolean enabled) {
        this.dryRun = enabled;
        return this;
    }

    public DependencyOption offline(boolean enabled) {
        this.offline = enabled;
        return this;
    }

    public DependencyOption force(boolean enabled) {
        this.force = enabled;
        return this;
    }

    public DependencyOption ignoreScripts(boolean enabled) {
        this.ignoreScripts = enabled;
        return this;
    }

    public DependencyOption registry(String url) {
        this.registry = url;
        return this;
    }

    /**
     * Selects how dependencies are laid out in {@code node_modules}.
     *
     * @param linker A linker strategy.
     * @return Chainable API.
     */
    public DependencyOption linker(Linker linker) {
        this.linker = linker;
        return this;
    }

    /**
     * Shares package installations across projects through the global virtual store in
     * {@code ~/.bun/install/cache/links}. This requires the isolated linker and is configured with
     * {@code BUN_INSTALL_GLOBAL_STORE}.
     *
     * @param enabled Whether to use the global store.
     * @return Chainable API.
     */
    public DependencyOption globalStore(boolean enabled) {
        return env("BUN_INSTALL_GLOBAL_STORE", enabled ? "1" : "0");
    }

    /**
     * Sets the directory for packages installed with {@code bun add --global}.
     *
     * @param directory A directory.
     * @return Chainable API.
     */
    public DependencyOption globalDir(String directory) {
        return env("BUN_INSTALL_GLOBAL_DIR", directory);
    }

    public DependencyOption globalDir(Path directory) {
        return globalDir(directory.toString());
    }

    /**
     * Sets the directory where binaries of globally installed packages are linked.
     *
     * @param directory A directory.
     * @return Chainable API.
     */
    public DependencyOption globalBinDir(String directory) {
        return env("BUN_INSTALL_BIN", directory);
    }

    public DependencyOption globalBinDir(Path directory) {
        return globalBinDir(directory.toString());
    }

    /**
     * Sets the directory which stores downloaded packages.
     *
     * @param directory A directory.
     * @return Chainable API.
     */
    public DependencyOption cacheDir(String directory) {
        this.cacheDir = directory;
        return this;
    }

    public DependencyOption cacheDir(Path directory) {
        return cacheDir(directory.toString());
    }

    /**
     * Sets the path to a {@code bunfig.toml} used for this command.
     *
     * @param path A configuration file path.
     * @return Chainable API.
     */
    public DependencyOption config(String path) {
        this.config = path;
        return this;
    }

    public DependencyOption config(Path path) {
        return config(path.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String command() {
        return operation.command();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> arguments() {
        List<String> arguments = new ArrayList();
        arguments.add(operation.command());

        value(arguments, "--config", config);
        value(arguments, "--cache-dir", cacheDir);
        if (linker != null) {
            arguments.add("--linker=" + linker.value());
        }

        flag(arguments, "--dev", dev && operation == DependencyOperation.ADD);
        flag(arguments, "--optional", optional && operation == DependencyOperation.ADD);
        flag(arguments, "--peer", peer && operation == DependencyOperation.ADD);
        flag(arguments, "--exact", exact && operation == DependencyOperation.ADD);
        flag(arguments, "--no-save", noSave);
        flag(arguments, "--frozen-lockfile", frozenLockfile && operation == DependencyOperation.INSTALL);
        flag(arguments, "--production", production);
        flag(arguments, "--dry-run", dryRun);
        flag(arguments, "--offline", offline);
        flag(arguments, "--force", force && operation != DependencyOperation.REMOVE);
        flag(arguments, "--ignore-scripts", ignoreScripts);
        value(arguments, "--registry", registry);

        arguments.addAll(packages);
        arguments.addAll(extra);
        return arguments;
    }
}
