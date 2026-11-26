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
 * Thrown when bun cannot be installed, started or completed.
 */
public class BunException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BunException(String message) {
        super(message);
    }

    public BunException(String message, Throwable cause) {
        super(message, cause);
    }
}
