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

/**
 * The dependency operation to run.
 */
public enum DependencyOperation {
    INSTALL("install"), ADD("add"), REMOVE("remove"), UPDATE("update");

    private final String command;

    DependencyOperation(String command) {
        this.command = command;
    }

    public String command() {
        return command;
    }
}
