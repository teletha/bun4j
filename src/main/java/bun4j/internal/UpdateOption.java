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
 * The options of a bun update.
 */
public class UpdateOption extends Option<UpdateOption> {

    {
        directory(BunInstaller.defaultDirectory());
    }

    private String version = "latest";
    private boolean force;
    private boolean upgradeCommand = true;

    public UpdateOption to(String path) {
        return to(Path.of(path));
    }

    public UpdateOption to(Path path) {
        return directory(path);
    }

    /**
     * Pins the bun version to update to.
     *
     * @param version A version such as {@code 1.4.4} or {@code latest}.
     * @return Chainable API.
     */
    public UpdateOption version(String version) {
        this.version = version;
        return this;
    }

    /**
     * Allows reinstalling the same version.
     *
     * @param force Whether to force the update.
     * @return Chainable API.
     */
    public UpdateOption force(boolean force) {
        this.force = force;
        return this;
    }

    /**
     * Controls whether {@code bun upgrade} is tried before falling back to a fresh download.
     *
     * @param enabled Whether to use the official upgrade command.
     * @return Chainable API.
     */
    public UpdateOption upgradeCommand(boolean enabled) {
        this.upgradeCommand = enabled;
        return this;
    }

    public String version() {
        return version;
    }

    public boolean force() {
        return force;
    }

    public boolean upgradeCommand() {
        return upgradeCommand;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String command() {
        return "upgrade";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> arguments() {
        return List.of();
    }
}
