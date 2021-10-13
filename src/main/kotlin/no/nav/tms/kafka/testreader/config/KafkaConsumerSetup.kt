package no.nav.tms.kafka.testreader.config

import no.nav.brukernotifikasjon.schemas.output.*
import no.nav.tms.kafka.testreader.common.kafka.FeilresponsConsumer
import no.nav.tms.kafka.testreader.feilrespons.FeilresponsEventService
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object KafkaConsumerSetup {

    private val log: Logger = LoggerFactory.getLogger(KafkaConsumerSetup::class.java)

    fun startAllKafkaPollers(appContext: ApplicationContext) {
        appContext.beskjedConsumer.startPolling()
    }

    suspend fun stopAllKafkaConsumers(appContext: ApplicationContext) {
        log.info("Begynner å stoppe kafka-pollerne...")

        if (!appContext.beskjedConsumer.isCompleted()) {
            appContext.beskjedConsumer.stopPolling()
        }

        log.info("...ferdig med å stoppe kafka-pollerne.")
    }

    suspend fun restartPolling(appContext: ApplicationContext) {
        stopAllKafkaConsumers(appContext)
        appContext.reinitializeConsumers()
        startAllKafkaPollers(appContext)
    }

    fun setupConsumerForTheBeskjedTopic(kafkaProps: Properties,
                                        eventProcessor: FeilresponsEventService,
                                        feilresponsTopicName: String
    ): FeilresponsConsumer {
        val kafkaConsumer = KafkaConsumer<NokkelFeilrespons, Feilrespons>(kafkaProps)
        return FeilresponsConsumer(feilresponsTopicName, kafkaConsumer, eventProcessor)
    }
}
