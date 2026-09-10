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
// JVM target alignment fix
// ------------------------------------------------------------------
// Flutter plugins like `shizuku_api` were written for JVM 11, while
// this app (AGP 9 / Gradle 9.1) compiles against JVM 17. Gradle 8+
// treats that mismatch as a hard error:
//   "Inconsistent JVM-target compatibility detected for tasks
//    'compileDebugJavaWithJavac' (11) and 'compileDebugKotlin' (17)"
// The block below forces every subproject to compile Java AND Kotlin
// against JVM 17, at task-execution time so it overrides whatever
// the plugin set.
// ==================================================================
subprojects {
    afterEvaluate {
        // --- Java ---
        tasks.withType<JavaCompile>().configureEach {
            sourceCompatibility = JavaVersion.VERSION_17.toString()
            targetCompatibility = JavaVersion.VERSION_17.toString()
        }

        // --- Kotlin ---
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
