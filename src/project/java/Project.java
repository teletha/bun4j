
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
        license(MIT);
        versionControlSystem("https://github.com/teletha/bun4j");

        require("io.github.teletha", "psychopath");
        require("io.github.teletha", "antibug").atTest();
    }
}