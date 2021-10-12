package no.nav.tms.kafka.testreader.health

interface HealthCheck {

    suspend fun status(): HealthStatus

}
