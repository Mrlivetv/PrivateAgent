allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

val newBuildDir: Directory =
    rootProject.layout.buildDirectory
        .dir("../../build")
        .get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}

subprojects {
    project.evaluationDependsOn(":app")
}

// ==================================================================
// Kotlin JVM target alignment
// ------------------------------------------------------------------
// Plugins like `shizuku_api` ship Kotlin bytecode targeting JVM 11
// while AGP compiles Java against JVM 17, triggering:
//   "Inconsistent JVM-target compatibility detected for tasks
//    'compileDebugJavaWithJavac' (11) and 'compileDebugKotlin' (17)"
//
// We do NOT touch the Android extension's compileOptions (that fails
// with "sourceCompatibility has been finalized" on AGP 9). Instead:
//   1. Force Kotlin's jvmTarget to 17 (matches AGP's Java default).
//   2. Rely on `kotlin.jvm.target.validation.mode=IGNORE` in
//      gradle.properties to bypass the check for legacy plugins.
// ==================================================================
subprojects {
    plugins.withId("org.jetbrains.kotlin.android") {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
            .configureEach {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
