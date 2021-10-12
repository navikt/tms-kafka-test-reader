package no.nav.tms.kafka.testreader.common.kafka

import kotlinx.coroutines.*
import no.nav.brukernotifikasjon.schemas.internal.Feilrespons
import no.nav.brukernotifikasjon.schemas.internal.NokkelFeilrespons
import no.nav.tms.kafka.testreader.common.exceptions.UntransformableRecordException
import no.nav.tms.kafka.testreader.feilrespons.FeilresponsEventService
import no.nav.tms.kafka.testreader.health.HealthCheck
import no.nav.tms.kafka.testreader.health.HealthStatus
import no.nav.tms.kafka.testreader.health.Status
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.RetriableException
import org.apache.kafka.common.errors.TopicAuthorizationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.coroutines.CoroutineContext

class FeilresponsConsumer(
        val topic: String,
        val kafkaConsumer: KafkaConsumer<NokkelFeilrespons, Feilrespons>,
        val eventBatchProcessorService: FeilresponsEventService,
        val job: Job = Job(),
        val maxPollTimeout: Long = 100L
) : CoroutineScope, HealthCheck {

    private val log: Logger = LoggerFactory.getLogger(FeilresponsConsumer::class.java)

    companion object {
        private const val ONE_MINUTE_IN_MS = 60000L
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    suspend fun stopPolling() {
        job.cancelAndJoin()
    }

    fun isCompleted(): Boolean {
        return job.isCompleted
    }

    fun isStopped(): Boolean {
        return !job.isActive
    }

    override suspend fun status(): HealthStatus {
        val serviceName = topic + "consumer"
        return if (job.isActive) {
            HealthStatus(serviceName, Status.OK, "Consumer is running", includeInReadiness = false)
        } else {
            log.info("Selftest mot Kafka-consumere feilet, consumer kjører ikke. Vil startes igjen av PeriodicConsumerPollingCheck innen 30 minutter.")
            HealthStatus(serviceName, Status.ERROR, "Consumer is not running", includeInReadiness = false)
        }
    }

    fun startPolling() {
        launch {
            log.info("Starter en coroutine for polling på topic-en $topic.")
            kafkaConsumer.use { consumer ->
                consumer.subscribe(listOf(topic))

                while (job.isActive) {
                    processBatchOfEvents()
                }
            }
        }
    }

    private suspend fun processBatchOfEvents() = withContext(Dispatchers.IO) {
        try {
            val records = kafkaConsumer.poll(Duration.of(maxPollTimeout, ChronoUnit.MILLIS))
            if (records.containsEvents()) {
                eventBatchProcessorService.processEvents(records)
                kafkaConsumer.commitSync()
            }
        } catch (re: RetriableException) {
            log.warn("Polling mot Kafka feilet, prøver igjen senere. Topic: $topic", re)
            rollbackOffset()

        } catch (ure: UntransformableRecordException) {
            val msg = "Et eller flere eventer kunne ikke transformeres, stopper videre polling. Topic: $topic. \n Bruker appen sisteversjon av brukernotifikasjon-schemas?"
            log.error(msg, ure)
            stopPolling()
        } catch (ce: CancellationException) {
            log.info("Denne coroutine-en ble stoppet. ${ce.message}", ce)

        } catch (tae: TopicAuthorizationException) {
            log.warn("Pauser polling i ett minutt, er ikke autorisert for å lese: ${tae.unauthorizedTopics()}", tae)
            delay(ONE_MINUTE_IN_MS)

        } catch (e: Exception) {
            log.error("Noe uventet feilet, stopper polling. Topic: $topic", e)
            stopPolling()
        }
    }

    fun ConsumerRecords<NokkelFeilrespons, Feilrespons>.containsEvents() = count() > 0

    private suspend fun rollbackOffset() {
        withContext(Dispatchers.IO) {
            kafkaConsumer.rollbackToLastCommitted()
        }
    }
}
