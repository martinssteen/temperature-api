package club.mascotfan.temperature_api.db

import club.mascotfan.temperature_api.controller.model.TemperatureResponse
import club.mascotfan.temperature_api.model.HumidityMessage
import club.mascotfan.temperature_api.model.TemperatureMessage
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneId

@Repository
class SQLTemperateRepository(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    fun recordTemperature(temperatureMessage: TemperatureMessage) {
        namedParameterJdbcTemplate.update(
            """
            INSERT INTO TEMPERATURES(SOURCE, TEMPERATURE_CELSIUS) VALUES (:source, :temperatureCelsius)
        """.trimIndent(),
            mapOf(
                "source" to temperatureMessage.source,
                "temperatureCelsius" to temperatureMessage.temperatureCelsius,
            )
        )
    }

    fun recordHumidity(humidityMessage: HumidityMessage) {
        namedParameterJdbcTemplate.update(
            """
                INSERT INTO HUMIDITY(SOURCE, HUMIDITY_PERCENTAGE) VALUES (:source, :humidity)
            """.trimIndent(),
            mapOf(
                "source" to humidityMessage.source,
                "humidity" to humidityMessage.humidity,
            )
        )
    }

    fun getLastTemperatures(): TemperatureResponse {
        val temps = getLastTemperaturesForAllSources()
        val humidity = getLastHumdityForAllSources()
        val allSources = (temps.keys + humidity.keys).toSet()
        val sources = allSources.map {
            TemperatureResponse.Sources(
                source = it,
                temperature = temps[it],
                humidity = humidity[it]
            )
        }
        return TemperatureResponse(
            sources = sources,
        )
    }

    private fun getLastTemperaturesForAllSources(): Map<String, TemperatureResponse.Sources.Temperature> {
        val temperatures : MutableMap<String, TemperatureResponse.Sources.Temperature> = mutableMapOf()
        namedParameterJdbcTemplate.query(
            """
                SELECT DISTINCT ON (source) source, temperature_celsius, recorded_at
                FROM data.temperatures
                ORDER BY source, recorded_at DESC
            """.trimIndent()
        ) { rs, _ ->
            val source = rs.getString("source") ?: "UNKNOWN"
            temperatures[source] =  TemperatureResponse.Sources.Temperature(
                rs.getString("temperature_celsius")?.toDouble() ?: 0.0,
                OffsetDateTime.ofInstant(rs.getTimestamp("recorded_at")?.toInstant(), ZoneId.of("Europe/Oslo"))
            )
        }

        return temperatures
    }

    private fun getLastHumdityForAllSources(): Map<String, TemperatureResponse.Sources.Humidity> {
        val humidity : MutableMap<String, TemperatureResponse.Sources.Humidity> = mutableMapOf()
        namedParameterJdbcTemplate.query(
            """
                SELECT DISTINCT ON (source) source, humidity_percentage, recorded_at
                FROM data.humidity
                ORDER BY source, recorded_at DESC;
            """.trimIndent()
        ) { rs, _ ->
            val source = rs.getString("source") ?: "UNKNOWN"
            humidity[source] = TemperatureResponse.Sources.Humidity(
                rs.getString("humidity_percentage")?.toDouble() ?: 0.0,
                OffsetDateTime.ofInstant(rs.getTimestamp("recorded_at")?.toInstant(), ZoneId.of("Europe/Oslo"))
            )
        }
        return humidity
    }
}