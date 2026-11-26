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

import static antibug.Tester.same;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void successCombinesStreams() {
        Result result = new Result(0, "first\n\nsecond\n", "warning\n");
        assertTrue(result.succeeded());
        assertEquals("first\n\nsecond\nwarning\n", result.output());
        assertTrue(same(List.of("first", "second"), result.lines()));
        assertEquals(result, result.requireSuccess());
    }

    @Test
    void failureThrowsOnRequireSuccess() {
        Result result = new Result(2, "", "error\n");
        assertFalse(result.succeeded());
        assertEquals("error\n", result.output());
        assertThrows(BunException.class, result::requireSuccess);
    }
}
