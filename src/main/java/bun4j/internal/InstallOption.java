/*
 * Copyright (C) 2026 The BUN4J Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bun4j.internal;

import java.nio.file.Path;
import java.util.List;

import bun4j.Option;

/**
 * The options of a local bun installation.
 */
public class InstallOption extends Option<InstallOption> {

    {
        directory(BunInstaller.defaultDirectory());
    }

    private String version = "latest";
    private boolean force;

    /**
     * Changes the installation directory.
     *
     * @param path A directory path.
     * @return Chainable API.
     */
    public InstallOption to(String path) {
        return to(Path.of(path));
    }

    /**
     * Changes the installation directory.
     *
     * @param path A directory path.
     * @return Chainable API.
     */
    public InstallOption to(Path path) {
        return directory(path);
    }

    /**
     * Pins the bun version to install. The default is the latest release.
     *
     * @param version A version such as {@code 1.4.4}, {@code bun-v1.4.4} or {@code latest}.
     * @return Chainable API.
     */
    public InstallOption version(String version) {
        this.version = version;
        return this;
    }

    /**
     * Downloads bun even if the requested version is already installed.
     *
     * @param force Whether to force the installation.
     * @return Chainable API.
     */
    public InstallOption force(boolean force) {
        this.force = force;
        return this;
    }

    public String version() {
        return version;
    }

    public boolean force() {
        return force;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String command() {
        return "install";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> arguments() {
        return List.of();
    }
}
