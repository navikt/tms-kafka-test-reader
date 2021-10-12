package no.nav.tms.kafka.testreader.config

import io.ktor.application.Application
import io.ktor.application.ApplicationStarted
import io.ktor.application.ApplicationStopPreparing
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.routing.routing
import kotlinx.coroutines.runBlocking
import no.nav.tms.kafka.testreader.health.healthApi

fun Application.mainModule(appContext: ApplicationContext = ApplicationContext()) {
    install(DefaultHeaders)
    routing {
        healthApi(appContext.healthService)
    }

    configureStartupHook(appContext)
    configureShutdownHook(appContext)
}

private fun Application.configureStartupHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStarted) {
        KafkaConsumerSetup.startAllKafkaPollers(appContext)
    }
}

private fun Application.configureShutdownHook(appContext: ApplicationContext) {
    environment.monitor.subscribe(ApplicationStopPreparing) {
        runBlocking {
            KafkaConsumerSetup.stopAllKafkaConsumers(appContext)
        }
    }
}
