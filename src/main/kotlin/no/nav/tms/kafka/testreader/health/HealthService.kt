package no.nav.tms.kafka.testreader.health

import no.nav.tms.kafka.testreader.config.ApplicationContext

class HealthService(private val applicationContext: ApplicationContext) {

    suspend fun getHealthChecks(): List<HealthStatus> {
        return listOf(
                applicationContext.beskjedConsumer.status(),
        )
    }
}
