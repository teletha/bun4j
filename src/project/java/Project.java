
/*
 * Copyright (C) 2026 The BUN4J Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
import static bee.api.License.*;

public class Project extends bee.api.Project {
    {
        product("io.github.teletha", "bun4j", "1.0");
        describe("""
                Bun4j operates Bun from Java. It installs and updates bun locally, runs its build, lint and format commands, and bundles TypeScript, React, Svelte and Vue libraries into JavaScript and CSS which can be consumed from external projects.

                - The bun runtime is downloaded from the official GitHub releases into ~/.bun/bin. Before the first command of a JVM, bun is installed if missing and checked for updates at most once a week.
                - Every command is synchronous and returns a Result with the exit code, stdout and stderr. Output can be streamed line by line.
                - Library bundles are produced through the JavaScript Bun.build() API, which enables framework plugins, CSS extraction and type declarations. React, Svelte and Vue are detected automatically from package.json and source files.
                """);
        license(MIT);
        versionControlSystem("https://github.com/teletha/bun4j");

        require("io.github.teletha", "antibug").atTest();
    }
}