// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
        fabricPublic
    }
    dependencies {
        classpath(androidPlugin)
        classpath(kotlinPlugin)
        classpath(googleServices)
        classpath(fabricPlugin)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

loadProperties("local.properties")

allprojects {
    repositories {
        google()
        jcenter()
    }
}

subprojects {
    val ktlintCfg by configurations.creating // add ktlint configuration

    dependencies {
        ktlintCfg(ktlint)
    }

    tasks {
        val ktlint by creating(JavaExec::class) {
            group = "verification"
            description = "Check Kotlin code style."
            main = "com.pinterest.ktlint.Main"
            classpath = ktlintCfg
            args("--verbose", "--reporter=plain", "--reporter=checkstyle,output=$buildDir/reports/ktlint.xml", "src/**/*.kt")
        }

        afterEvaluate {
            tasks.findByPath("check")?.dependsOn(ktlint)
        }
    }
}

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}
