package no.nav.tms.kafka.testreader.feilrespons

import no.nav.brukernotifikasjon.schemas.output.Feilrespons
import no.nav.brukernotifikasjon.schemas.output.NokkelFeilrespons
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory

class FeilresponsEventService {

    val log = LoggerFactory.getLogger(FeilresponsEventService::class.java)

    fun processEvents(events: ConsumerRecords<NokkelFeilrespons, Feilrespons>) {
            log.info("Leste ${events.count()} feilresponseventer")
    }


}
