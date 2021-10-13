package no.nav.tms.kafka.testreader.common.kafka

import no.nav.brukernotifikasjon.schemas.output.Feilrespons
import no.nav.brukernotifikasjon.schemas.output.NokkelFeilrespons
import org.apache.kafka.clients.consumer.KafkaConsumer

fun KafkaConsumer<NokkelFeilrespons, Feilrespons>.rollbackToLastCommitted() {
    val assignedPartitions = assignment()
    val partitionCommittedInfo = committed(assignedPartitions)
    partitionCommittedInfo.forEach { (partition, lastCommitted) ->
        seek(partition, lastCommitted.offset())
    }
}
