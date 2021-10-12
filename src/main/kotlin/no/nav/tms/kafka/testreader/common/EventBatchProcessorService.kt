package no.nav.tms.kafka.testreader.common

import org.apache.kafka.clients.consumer.ConsumerRecords

interface EventBatchProcessorService {

    suspend fun processEvents(events: ConsumerRecords<*,*>)

}
