import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm").version(Kotlin.version)
    kotlin("plugin.allopen").version(Kotlin.version)

    id(Flyway.pluginId) version (Flyway.version)
    id(Shadow.pluginId) version (Shadow.version)

    // Apply the application plugin to add support for building a CLI application.
    application
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "13"
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://packages.confluent.io/maven")
    mavenLocal()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.navikt:brukernotifikasjon-schemas-internal:1.2021.09.27-09.56-fee7c58af3ee")
    implementation(DittNAV.Common.utils)
    implementation(DittNAV.Common.influxdb)
    implementation(Influxdb.java)
    implementation(Kafka.Apache.clients)
    implementation(Kafka.Confluent.avroSerializer)
    implementation(Ktor.serverNetty)
    implementation(Ktor.htmlBuilder)
    implementation(Logback.classic)
    implementation(Logstash.logbackEncoder)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }

    register("runServer", JavaExec::class) {
        println("Setting default environment variables for running with DittNAV docker-compose")
        DockerComposeDefaults.environomentVariables.forEach { (name, value) ->
            println("Setting the environment variable $name")
            environment(name, value)
        }

        main = application.mainClass.get()
        classpath = sourceSets["main"].runtimeClasspath
    }
}

project.setProperty("mainClassName", application.mainClass.get())
apply(plugin = Shadow.pluginId)
