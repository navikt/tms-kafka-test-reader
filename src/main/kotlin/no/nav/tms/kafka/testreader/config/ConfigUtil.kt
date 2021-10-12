package no.nav.tms.kafka.testreader.config

object ConfigUtil {

    fun isCurrentlyRunningOnNais(): Boolean {
        return System.getenv("NAIS_APP_NAME") != null
    }

}
