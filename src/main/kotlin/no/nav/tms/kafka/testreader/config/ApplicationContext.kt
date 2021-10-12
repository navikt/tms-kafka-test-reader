package no.nav.tms.kafka.testreader.config

import no.nav.tms.kafka.testreader.feilrespons.FeilresponsEventService
import no.nav.tms.kafka.testreader.health.HealthService
import org.slf4j.LoggerFactory

class ApplicationContext {

    private val log = LoggerFactory.getLogger(ApplicationContext::class.java)

    val environment = Environment()

    val beskjedEventProcessor = FeilresponsEventService()
    val beskjedKafkaProps = Kafka.feilresponsConsumerProps(environment)
    var beskjedConsumer = initializeBeskjedConsumer()

    val healthService = HealthService(this)

    private fun initializeBeskjedConsumer() =
            KafkaConsumerSetup.setupConsumerForTheBeskjedTopic(beskjedKafkaProps, beskjedEventProcessor, environment.feilresponsTopicName)

    fun reinitializeConsumers() {
        if (beskjedConsumer.isCompleted()) {
            beskjedConsumer = initializeBeskjedConsumer()
            log.info("beskjedConsumer har blitt reinstansiert.")
        } else {
            log.warn("beskjedConsumer kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }
    }
}
