<p align="center">
    <a href="https://docs.oracle.com/en/java/javase/25/"><img src="https://img.shields.io/badge/Java-Release%2025-green"/></a>
    <span>&nbsp;</span>
    <a href="https://jitpack.io/#io.github.teletha/bun4j"><img src="https://img.shields.io/jitpack/version/io.github.teletha/bun4j?label=Repository&color=green"></a>
    <span>&nbsp;</span>
    <a href="https://teletha.github.io/bun4j"><img src="https://img.shields.io/website.svg?down_color=red&down_message=CLOSE&label=Official%20Site&up_color=green&up_message=OPEN&url=https%3A%2F%2Fteletha.github.io%2Fbun4j"></a>
</p>

## Summary
Bun4j operates Bun from Java. It installs and updates bun locally, runs its build, lint and format commands, and bundles TypeScript, React, Svelte and Vue libraries into JavaScript and CSS which can be consumed from external projects.

- The bun runtime is downloaded from the official GitHub releases into ~/.bun/bin. Before the first command of a JVM, bun is installed if missing and checked for updates at most once a week.
- Every command is synchronous and returns a Result with the exit code, stdout and stderr. Output can be streamed line by line.
- Library bundles are produced through the JavaScript Bun.build() API, which enables framework plugins, CSS extraction and type declarations. React, Svelte and Vue are detected automatically from package.json and source files.
<p align="right"><a href="#top">back to top</a></p>


## Usage
Bun is installed into ~/.bun/bin automatically before the first command in a JVM, and checked for updates at most once a week. The current state can be inspected at any time.
```java
boolean installed = Bun.installed();
String version = Bun.version();
java.time.Duration interval = Bun.updateInterval();
```

Run build, lint and format. Every command returns a Result which reports the exit code and both output streams. When bun has no built-in linter, lint automatically falls back to oxlint through bunx.
```java
Result built = Bun.build(o -> o.entry("src/index.ts")
        .outdir("dist")
        .target(Target.BROWSER)
        .format(ModuleFormat.ESM)
        .minify(true));

Bun.lint(o -> o.path("src").fix(true).maxWarnings(0));
Bun.format(o -> o.path("src").check(true));

if (!built.succeeded()) {
    System.err.println(built.output());
}
```

Bundle a library into JavaScript and CSS with type declarations and a package.json exposing the exports. React, Svelte and Vue are detected automatically from package.json and source files; they can also be selected explicitly with Framework.
```java
Bun.bundle(o -> o.entry("src/index.tsx")
        .outdir("dist")
        .format(ModuleFormat.ESM)
        .dts(true)
        .minify(true)
        .generatePackageJson(true)
        .packageName("my-library")
        .echo());
```

<p align="right"><a href="#top">back to top</a></p>




## Prerequisites
Bun4j runs on all major operating systems and requires only [Java version 25](https://docs.oracle.com/en/java/javase/25/) or later to run.
To check, please run `java -version` on your terminal.
<p align="right"><a href="#top">back to top</a></p>

## Install
For any code snippet below, please substitute the version given with the version of Bun4j you wish to use.
#### [Maven](https://maven.apache.org/)
Add JitPack repository at the end of repositories element in your build.xml:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
Add it into in the dependencies element like so:
```xml
<dependency>
    <groupId>io.github.teletha</groupId>
    <artifactId>bun4j</artifactId>
    <version>1.0.1</version>
</dependency>
```
#### [Gradle](https://gradle.org/)
Add JitPack repository at the end of repositories in your build.gradle:
```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```
Add it into the dependencies section like so:
```gradle
dependencies {
    implementation 'io.github.teletha:bun4j:1.0.1'
}
```
#### [SBT](https://www.scala-sbt.org/)
Add JitPack repository at the end of resolvers in your build.sbt:
```scala
resolvers += "jitpack" at "https://jitpack.io"
```
Add it into the libraryDependencies section like so:
```scala
libraryDependencies += "io.github.teletha" % "bun4j" % "1.0.1"
```
#### [Leiningen](https://leiningen.org/)
Add JitPack repository at the end of repositories in your project().clj:
```clj
:repositories [["jitpack" "https://jitpack.io"]]
```
Add it into the dependencies section like so:
```clj
:dependencies [[io.github.teletha/bun4j "1.0.1"]]
```
#### [Bee](https://teletha.github.io/bee)
Add it into your project definition class like so:
```java
require("io.github.teletha", "bun4j", "1.0.1");
```
<p align="right"><a href="#top">back to top</a></p>


## Contributing
Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.
If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

The overwhelming majority of changes to this project don't add new features at all. Optimizations, tests, documentation, refactorings -- these are all part of making this product meet the highest standards of code quality and usability.
Contributing improvements in these areas is much easier, and much less of a hassle, than contributing code for new features.

### Bug Reports
If you come across a bug, please file a bug report. Warning us of a bug is possibly the most valuable contribution you can make to Bun4j.
If you encounter a bug that hasn't already been filed, [please file a report](https://github.com/teletha/bun4j/issues/new) with an [SSCCE](http://sscce.org/) demonstrating the bug.
If you think something might be a bug, but you're not sure, ask on StackOverflow or on [bun4j-discuss](https://github.com/teletha/bun4j/discussions).
<p align="right"><a href="#top">back to top</a></p>


## Dependency
Bun4j depends on the following products on runtime.
* No Dependency
<p align="right"><a href="#top">back to top</a></p>


## License
Copyright (C) 2026 The BUN4J Development Team

MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
<p align="right"><a href="#top">back to top</a></p>
