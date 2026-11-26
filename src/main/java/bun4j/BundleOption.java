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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The options of a library bundle.
 *
 * <p>
 * Unlike {@link BuildOption}, this option uses the JavaScript {@code Bun.build()} API through a
 * generated build script. This allows plugins for Svelte, Vue and custom loaders, type declaration
 * generation and CSS extraction.
 * </p>
 */
public class BundleOption extends BuildConfig<BundleOption> {

    private final List<String> plugins = new ArrayList();
    private final Set<Framework> frameworks = EnumSet.noneOf(Framework.class);

    /** The project directory used to detect frameworks when none are specified. */
    private Path projectDirectory;

    private String packageName;
    private boolean installPlugins = true;
    private boolean generatePackageJson;
    private boolean keepScript;

    {
        outdir("dist");
        target(Target.BROWSER);
        format(ModuleFormat.ESM);
        splitting(true);
    }

    /**
     * Sets the package name used when a package.json is generated.
     *
     * @param name A package name.
     * @return Chainable API.
     */
    public BundleOption packageName(String name) {
        this.packageName = name;
        return this;
    }

    /**
     * Enables a framework preset. React is handled natively by bun and requires no package.
     *
     * @param frameworks Frameworks to support.
     * @return Chainable API.
     */
    public BundleOption framework(Framework... frameworks) {
        for (Framework framework : frameworks) {
            if (framework != null) {
                this.frameworks.add(framework);
            }
        }
        return this;
    }

    /**
     * Adds a custom Bun plugin package or a path to a plugin module.
     *
     * @param specifiers Package names or module paths.
     * @return Chainable API.
     */
    public BundleOption plugin(String... specifiers) {
        for (String specifier : specifiers) {
            if (specifier != null && !specifier.isBlank()) {
                plugins.add(specifier);
            }
        }
        return this;
    }

    /**
     * Controls whether required framework plugins are installed automatically with {@code bun add
     * -D}.
     *
     * @param enabled Whether to install plugins.
     * @return Chainable API.
     */
    public BundleOption installPlugins(boolean enabled) {
        this.installPlugins = enabled;
        return this;
    }

    /**
     * Controls whether a package.json describing the bundle is generated in the output directory.
     *
     * @param enabled Whether to generate a package.json.
     * @return Chainable API.
     */
    public BundleOption generatePackageJson(boolean enabled) {
        this.generatePackageJson = enabled;
        return this;
    }

    /**
     * Keeps the generated build script after the bundle finishes, which is useful for debugging.
     *
     * @param enabled Whether to keep the script.
     * @return Chainable API.
     */
    public BundleOption keepScript(boolean enabled) {
        this.keepScript = enabled;
        return this;
    }

    public List<String> entries() {
        return List.copyOf(entries);
    }

    public String outdir() {
        return outdir;
    }

    public String packageName() {
        return packageName;
    }

    public boolean generatePackageJson() {
        return generatePackageJson;
    }

    public boolean keepScript() {
        return keepScript;
    }

    /**
     * Sets the project directory used for framework detection.
     *
     * @param directory A project directory.
     */
    BundleOption projectDirectory(Path directory) {
        this.projectDirectory = directory;
        return this;
    }

    /**
     * Returns the frameworks to bundle. When none are specified, they are detected from the
     * project's {@code package.json} and source files. React is the fallback because bun handles
     * JSX/TSX natively.
     *
     * @return A set of frameworks.
     */
    public Set<Framework> frameworks() {
        if (!frameworks.isEmpty()) {
            return EnumSet.copyOf(frameworks);
        }
        Set<Framework> detected = detectFrameworks();
        return detected.isEmpty() ? EnumSet.of(Framework.REACT) : detected;
    }

    private Set<Framework> detectFrameworks() {
        Set<Framework> detected = EnumSet.noneOf(Framework.class);
        if (projectDirectory == null) {
            return detected;
        }

        Path manifest = projectDirectory.resolve("package.json");
        if (Files.isRegularFile(manifest)) {
            try {
                String json = Files.readString(manifest);
                if (hasDependency(json, "react") || hasDependency(json, "react-dom")) {
                    detected.add(Framework.REACT);
                }
                if (hasDependency(json, "svelte")) {
                    detected.add(Framework.SVELTE);
                }
                if (hasDependency(json, "vue") || hasDependency(json, "@vue/compiler-sfc")) {
                    detected.add(Framework.VUE);
                }
            } catch (IOException ignored) {
                // Fall back to the source files below.
            }
        }

        Path source = projectDirectory.resolve("src");
        if (Files.isDirectory(source)) {
            try (var paths = Files.walk(source)) {
                paths.forEach(path -> {
                    String name = path.getFileName().toString();
                    if (name.endsWith(".svelte")) {
                        detected.add(Framework.SVELTE);
                    } else if (name.endsWith(".vue")) {
                        detected.add(Framework.VUE);
                    } else if (name.endsWith(".jsx") || name.endsWith(".tsx")) {
                        detected.add(Framework.REACT);
                    }
                });
            } catch (IOException ignored) {
                // Keep whatever was detected from package.json.
            }
        }
        return detected;
    }

    private static boolean hasDependency(String json, String name) {
        return Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:").matcher(json).find();
    }

    public boolean installPlugins() {
        return installPlugins;
    }

    /**
     * Returns the npm packages required by the selected frameworks.
     *
     * @return A list of package specifiers.
     */
    public List<String> requiredPackages() {
        List<String> packages = new ArrayList();
        for (Framework framework : frameworks()) {
            packages.addAll(framework.dependencies());
        }
        return packages;
    }

    /**
     * Returns the file name of the primary output, used for package.json generation.
     *
     * @return A file name such as {@code index.js}.
     */
    public String mainFileName() {
        if (entries.isEmpty()) {
            return "index.js";
        }
        String name = Path.of(entries.get(0)).getFileName().toString();
        int index = name.lastIndexOf('.');
        return (index == -1 ? name : name.substring(0, index)) + ".js";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String command() {
        return "bundle";
    }

    /**
     * Builds the low level CLI arguments from the shared options. This is used when the same option
     * object is passed to {@link Bun#build(BundleOption)}.
     *
     * @return A list of arguments for {@code bun build}.
     */
    @Override
    public List<String> arguments() {
        List<String> arguments = new ArrayList();
        arguments.add("build");
        arguments.addAll(entries);
        headerArguments(arguments);
        flagArguments(arguments);
        definedArguments(arguments);
        arguments.addAll(extra);
        return arguments;
    }

    /**
     * Generates the JavaScript build script executed by bun.
     *
     * @return The script source.
     */
    public String script() {
        StringBuilder source = new StringBuilder();
        Set<Framework> active = frameworks();

        if (active.contains(Framework.SVELTE)) {
            source.append("import { SveltePlugin } from \"bun-plugin-svelte\";\n");
        }
        if (active.contains(Framework.VUE)) {
            source.append("import vuePlugin from \"@eckidevs/bun-plugin-vue\";\n");
        }
        for (int i = 0; i < plugins.size(); i++) {
            source.append("import plugin").append(i).append(" from ").append(quote(plugins.get(i))).append(";\n");
        }
        source.append('\n');

        if (active.contains(Framework.VUE)) {
            source.append("const bun4jVue = vuePlugin();\n");
            source.append("const bun4jVueWindows = {\n");
            source.append("  name: \"bun4j-vue-path-fix\",\n");
            source.append("  setup(build) {\n");
            source.append("    const originalOnLoad = build.onLoad.bind(build);\n");
            source.append("    build.onLoad = (options, callback) => originalOnLoad(options, async (args) => {\n");
            source.append("      const normalized = Object.assign({}, args, { path: args.path.replace(/\\\\/g, \"/\") });\n");
            source.append("      const result = await callback(normalized);\n");
            source.append("      if (result && typeof result.contents === \"string\") {\n");
            source.append("        result.contents = result.contents.replace(/\\\\/g, \"/\");\n");
            source.append("      }\n");
            source.append("      return result;\n");
            source.append("    });\n");
            source.append("    bun4jVue.setup(build);\n");
            source.append("  }\n");
            source.append("};\n\n");
        }

        source.append("const result = await Bun.build({\n");
        source.append("  entrypoints: ").append(array(entries)).append(",\n");
        source.append("  outdir: ").append(quote(outdir)).append(",\n");
        if (target != null) {
            source.append("  target: ").append(quote(target.value())).append(",\n");
        }
        if (format != null) {
            source.append("  format: ").append(quote(format.value())).append(",\n");
        }
        source.append("  splitting: ").append(splitting).append(",\n");
        source.append("  minify: ").append(minify).append(",\n");
        if (sourcemap != null && !active.contains(Framework.SVELTE)) {
            source.append("  sourcemap: ").append(quote(sourcemap.value())).append(",\n");
        } else if (sourcemap != null) {
            source.append("  // sourcemaps are not supported by bun-plugin-svelte\n");
        }
        if (check || dts) {
            source.append("  check: true,\n");
        }
        if (dts) {
            source.append("  dts: true,\n");
        }
        if (!external.isEmpty()) {
            source.append("  external: ").append(array(external)).append(",\n");
        }
        if (!define.isEmpty()) {
            source.append("  define: {\n");
            define.forEach((key, expression) -> source.append("    ").append(quote(key)).append(": ").append(expression).append(",\n"));
            source.append("  },\n");
        }

        List<String> activePlugins = new ArrayList();
        if (active.contains(Framework.SVELTE)) {
            activePlugins.add("SveltePlugin({ development: false })");
        }
        if (active.contains(Framework.VUE)) {
            activePlugins.add("bun4jVueWindows");
        }
        for (int i = 0; i < plugins.size(); i++) {
            activePlugins.add("plugin" + i + "()");
        }
        if (!activePlugins.isEmpty()) {
            source.append("  plugins: [").append(String.join(", ", activePlugins)).append("],\n");
        }
        source.append("});\n\n");

        source.append("if (!result.success) {\n");
        source.append("  for (const log of result.logs) console.error(log);\n");
        source.append("  process.exit(1);\n");
        source.append("}\n");
        source.append("console.log(\"bun4j: bundled \" + result.outputs.length + \" file(s)\");\n");
        return source.toString();
    }

    /**
     * Generates a minimal package.json which exposes the bundle to external projects.
     *
     * @return The package.json content.
     */
    public String packageJson() {
        String main = mainFileName();
        String types = main.substring(0, main.lastIndexOf('.')) + ".d.ts";
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"name\": ").append(quote(packageName == null ? "bun4j-library" : packageName)).append(",\n");
        json.append("  \"type\": \"module\",\n");
        if (dts) {
            json.append("  \"types\": ").append(quote("./" + types)).append(",\n");
        }
        json.append("  \"exports\": {\n");
        json.append("    \".\": {\n");
        if (dts) {
            json.append("      \"types\": ").append(quote("./" + types)).append(",\n");
        }
        json.append("      \"import\": ").append(quote("./" + main)).append("\n");
        json.append("    }\n");
        json.append("  }\n");
        json.append("}\n");
        return json.toString();
    }

    private static String array(List<String> values) {
        StringBuilder text = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i != 0) {
                text.append(", ");
            }
            text.append(quote(values.get(i)));
        }
        return text.append(']').toString();
    }

    private static String quote(String value) {
        StringBuilder text = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> text.append("\\\\");
                case '"' -> text.append("\\\"");
                case '\n' -> text.append("\\n");
                case '\r' -> text.append("\\r");
                case '\t' -> text.append("\\t");
                default -> text.append(c);
            }
        }
        return text.append('"').toString();
    }
}
