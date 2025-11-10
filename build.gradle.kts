import java.net.URLClassLoader

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.github.classgraph:classgraph:4.8.177")
    }
}

plugins {
    java
    application
    id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "com.dtsx.astra.cli"
version = "1.0.0-rc.5"

val mockitoAgent = configurations.create("mockitoAgent")

repositories {
    mavenCentral()
}

dependencies {
    // shuts up some errors about missing SLF4J implementations
    implementation("org.slf4j:slf4j-nop:2.0.17")

    // for underlying api calls
    implementation("com.datastax.astra:astra-db-java:2.1.0")
//    implementation("com.datastax.astra:astra-sdk-devops:1.2.9")
    implementation(files("astra-sdk-devops.jar")) // temporary until it's on maven central

    // unzip downloaded external programs (cqlsh, dsbulk, etc.)
    implementation("org.apache.commons:commons-compress:1.28.0")

    // guess
    implementation("info.picocli:picocli:4.7.7")
    annotationProcessor("info.picocli:picocli-codegen:4.7.7")
    implementation("info.picocli:picocli-codegen:4.7.7")

    // help with cross-platform console output
    implementation("org.fusesource.jansi:jansi:2.4.2")
    implementation("info.picocli:picocli-jansi-graalvm:1.2.0")

    // guess
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // prettier assertions, may remove if it ends up not being super useful
    testImplementation("org.assertj:assertj-core:3.27.4")

    // property-based testing
    testImplementation("net.jqwik:jqwik:1.9.3")

    // snapshot tests (golden-master-esque)
    testImplementation("com.approvaltests:approvaltests:25.0.23")

    // in-memory file system for testing
    testImplementation("com.google.jimfs:jimfs:1.3.0")

    // mocking (also, https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#mockito-instrumentation)
    testImplementation("org.mockito:mockito-core:5.19.0")
    mockitoAgent("org.mockito:mockito-core:5.19.0") { isTransitive = false }

    // verifying csv output
    testImplementation("org.apache.commons:commons-csv:1.14.1")

    // using modified env-vars/system-props for testing + capturing system.exit() calls
    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.1.8")

    // test creds and such from .env files
    testImplementation("io.github.cdimascio:dotenv-java:3.2.0")

    // compileOnly & annotationProcessor declarations need to be duplicated to work for both main and test source sets
    compileOnly("org.jetbrains:annotations:26.0.2")
    testCompileOnly("org.jetbrains:annotations:26.0.2")

    compileOnly("org.projectlombok:lombok:1.18.42")
    testCompileOnly("org.projectlombok:lombok:1.18.42")

    annotationProcessor("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
}

tasks.compileJava {
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

application {
    mainClass.set("com.dtsx.astra.cli.AstraCli")
}

val isProd = project.hasProperty("prod")

graalvmNative {
    binaries.all {
        imageName.set("astra")

        buildArgs.add("--enable-http")
        buildArgs.add("--enable-https")
        buildArgs.add("--enable-native-access=ALL-UNNAMED")
        buildArgs.add("-H:-CheckToolchain")
        
        if (project.hasProperty("prod")) {
            buildArgs.add("-Os")
        } else {
            buildArgs.add("-Ob")
        }
    }
}

initNativeArchiveTask<Tar>("nativeTar") {
    compression = Compression.GZIP
    archiveExtension = "tar.gz"

    from(tasks.nativeCompile.get().outputs.files.singleFile) {
        filePermissions {
            unix("rwxr-xr-x")
        }
    }
}

initNativeArchiveTask<Zip>("nativeZip") {
    from(tasks.nativeCompile.get().outputs.files) {
        include("*.exe")
    }
    from("scripts/uninstall.ps1")
    from("assets/astra.ico")
}

inline fun <reified T : AbstractArchiveTask>initNativeArchiveTask(name: String, crossinline otherConfiguration: T.() -> Unit) {
    tasks.register<T>(name) {
        dependsOn("nativeCompile")
        archiveBaseName.set("astra")
        archiveVersion.set("")
        archiveClassifier.set(getOsArch())
        destinationDirectory.set(file("${layout.buildDirectory.get()}/distributions"))
        otherConfiguration()
        into("astra/bin")
    }
}

tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter", "jqwik")
    }

    jvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
        "-javaagent:${mockitoAgent.asPath}"
    )
}

tasks.register<Exec>("lifecycleTest") {
    group = "verification"

    dependsOn("nativeCompile")

    commandLine("bash", "src/test/lifecycle/test.sh")

    isIgnoreExitValue = false
}

val nativeImageGeneratedDir = layout.buildDirectory.dir("classes/java/main/META-INF/native-image/astra-cli-generated/${project.group}/${project.name}").get().asFile

tasks.register<JavaExec>("generateJniConfig") {
    group = "build"

    val outputFile = file("${nativeImageGeneratedDir}/jni-config.json")

    doFirst {
        outputFile.parentFile.mkdirs()
    }

    mainClass.set("picocli.codegen.aot.graalvm.JniConfigGenerator")

    classpath = files((configurations.runtimeClasspath.get() + configurations.annotationProcessor.get()).filter {
        it.name.startsWith("picocli") || it.name.startsWith("jansi")
    })

    args = listOf(
        "org.fusesource.jansi.internal.CLibrary",
        "org.fusesource.jansi.internal.Kernel32",
        "-o=${outputFile.absolutePath}"
    )

    jvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED"
    )

    outputs.file(outputFile)
}

tasks.register("generateGraalReflectionConfig") {
    group = "build"

    val inputFile = file("reflected.txt")

    val outputFile = file("${nativeImageGeneratedDir}/reflect-config.json")

    inputs.file(inputFile)
    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()

        val classpath = sourceSets.main.get().runtimeClasspath
        val classLoader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray())

        val inputLines = inputFile.readLines().map(String::trim).filter { it.isNotBlank() && !it.startsWith("#") }
        val defaultReflectionKeys = listOf("allPublicConstructors", "allDeclaredMethods")

        val scanResult = io.github.classgraph.ClassGraph()
            .overrideClassLoaders(classLoader)
            .enableAllInfo()
            .scan()

        val classesWithConfigs = inputLines.flatMap { line ->
            val (classPattern, customConfig) = if (line.contains(" = ")) {
                val parts = line.split(" = ", limit = 2)
                parts[0].trim() to parts[1].trim().split(",").map(String::trim)
            } else {
                line to defaultReflectionKeys
            }

            // Expand pattern to actual class names
            if (classPattern.endsWith(".*") || classPattern.endsWith("]")) {
                val packageName = classPattern.substringBeforeLast(".^[", "").ifBlank { classPattern.substringBeforeLast(".[", "").ifBlank { classPattern.substringBeforeLast(".") } }
                val classesInPackage = scanResult.getPackageInfo(packageName)?.classInfo ?: emptyList()

                val filter: (String) -> Boolean = if (classPattern.contains("[")) {
                    val regexes = classPattern.substringAfter("[").substringBefore("]").split(",").map(String::trim).map(String::toRegex)

                    if (classPattern.contains("^[")) {
                        { className -> regexes.none { className.matches(it) } }
                    } else {
                        { className -> regexes.any { className.matches(it) } }
                    }
                } else {
                    { true }
                }

                classesInPackage
                    .filter { !it.isInterface && !it.isAbstract && filter(it.name.substringAfterLast('.')) }
                    .map { it.name to customConfig }
            } else {
                listOf(classPattern to customConfig)
            }
        }.distinctBy { it.first }

        scanResult.close()

        val reflectionConfig = classesWithConfigs.map { (className, configKeys) ->
            try {
                Class.forName(className, false, classLoader)
            } catch (_: ClassNotFoundException) {
                throw GradleException("Invalid class '$className' found in reflected.txt")
            }

            buildMap {
                put("name", className)
                configKeys.forEach { put(it, true) }
            }
        }

        val json = groovy.json.JsonBuilder(reflectionConfig).toString()
        outputFile.writeText(json)
    }
}

val resourcePatterns = mutableListOf<String>()

tasks.register("includeJansiNativeLibResources") {
    group = "build"

    val (os, arch) = getOsArch().split("-")

    val osPattern = when (os) {
        "windows" -> "Windows"
        "macos" -> "Mac"
        "linux" -> "Linux"
        else -> ".*"
    }

    val archPattern = when (arch) {
        "x86_64", "arm64" -> arch
        else -> ".*"
    }

    resourcePatterns.add(
        "\\QMETA-INF/native-image/org/fusesource/jansi/$osPattern/$archPattern/.*\\E"
    )
}

// Can't get graal-compiled binary to recognize system properties set at runtime,
// so we're doing this fun workaround of generating a properties file at build time instead.
tasks.register("createDynamicProperties") {
    group = "build"

    val outputFile = layout.buildDirectory.file("resources/main/dynamic.properties").get().asFile

    val cliSystemProperties = providers.provider {
        mapOf(
            "cli.version" to project.version.toString(),
            "cli.rc-file.name" to if (isProd) ".astrarc" else ".astrarc-dev",
            "cli.home-folder.name" to if (isProd) "astra" else "astra-dev",
        )
    }

    inputs.property("cliSystemProperties", cliSystemProperties)
    outputs.file(outputFile)

    doLast {
        val output = cliSystemProperties.get().map { (key, value) -> "$key=$value" }.joinToString("\n")

        outputFile.parentFile.mkdirs()
        outputFile.writeText(output)

        resourcePatterns.add(
            "\\Qdynamic.properties\\E"
        )
    }
}

tasks.register("generateGraalResourceConfig") {
    dependsOn("includeJansiNativeLibResources")
    dependsOn("createDynamicProperties")

    val outputFile = file("${nativeImageGeneratedDir}/resource-config.json")

    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()

        val resourceConfig = mapOf(
            "resources" to mapOf(
                "includes" to resourcePatterns.map { mapOf("pattern" to it) }
            ),
            "bundles" to emptyList<Any>()
        )

        val json = groovy.json.JsonBuilder(resourceConfig).toString()
        outputFile.writeText(json)
    }
}

tasks.jar {
    dependsOn("generateGraalReflectionConfig")
    dependsOn("generateGraalResourceConfig")
    dependsOn("generateJniConfig")
}

tasks.compileTestJava {
    dependsOn("createDynamicProperties")
    options.compilerArgs.add("-parameters")
}

tasks.run {
    dependsOn("createDynamicProperties")
    standardInput = System.`in`
}

fun getOsArch(): String {
    val os = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()

    return when {
        os.contains("windows") -> {
            if (arch.contains("aarch64") || arch.contains("arm")) "windows-arm64" else "windows-x86_64"
        }
        os.contains("mac") || os.contains("darwin") -> {
            if (arch.contains("aarch64") || arch.contains("arm")) "macos-arm64" else "macos-x86_64"
        }
        os.contains("linux") -> {
            if (arch.contains("aarch64") || arch.contains("arm")) "linux-arm64" else "linux-x86_64"
        }
        else -> throw GradleException("Unsupported OS: $os with architecture $arch")
    }
}

tasks.register<Jar>("fatJar") {
    dependsOn(configurations.runtimeClasspath)
    dependsOn("createDynamicProperties")

    archiveFileName.set("fat.jar")
    archiveVersion.set(project.version.toString())

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "com.dtsx.astra.cli.AstraCli"
    }

    from(sourceSets.main.get().output)

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
