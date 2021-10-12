package no.nav.tms.kafka.testreader.config

import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar
import no.nav.tms.kafka.testreader.config.ConfigUtil.isCurrentlyRunningOnNais

data class Environment(val username: String = getEnvVar("SERVICEUSER_USERNAME"),
                       val password: String = getEnvVar("SERVICEUSER_PASSWORD"),
                       val groupId: String = getEnvVar("GROUP_ID"),
                       val clusterName: String = getEnvVar("NAIS_CLUSTER_NAME"),
                       val namespace: String = getEnvVar("NAIS_NAMESPACE"),
                       val aivenBrokers: String = getEnvVar("KAFKA_BROKERS"),
                       val aivenSchemaRegistry: String = getEnvVar("KAFKA_SCHEMA_REGISTRY"),
                       val securityConfig: SecurityConfig = SecurityConfig(isCurrentlyRunningOnNais()),
                       val feilresponsTopicName: String = getEnvVar("FEILRESPONS_TOPIC")
)

data class SecurityConfig(
        val enabled: Boolean,

        val variables: SecurityVars? = if (enabled) {
            SecurityVars()
        } else {
            null
        }
)

data class SecurityVars(
        val aivenTruststorePath: String = getEnvVar("KAFKA_TRUSTSTORE_PATH"),
        val aivenKeystorePath: String = getEnvVar("KAFKA_KEYSTORE_PATH"),
        val aivenCredstorePassword: String = getEnvVar("KAFKA_CREDSTORE_PASSWORD"),
        val aivenSchemaRegistryUser: String = getEnvVar("KAFKA_SCHEMA_REGISTRY_USER"),
        val aivenSchemaRegistryPassword: String = getEnvVar("KAFKA_SCHEMA_REGISTRY_PASSWORD")
)

fun isOtherEnvironmentThanProd() = System.getenv("NAIS_CLUSTER_NAME") != "prod-sbs"
