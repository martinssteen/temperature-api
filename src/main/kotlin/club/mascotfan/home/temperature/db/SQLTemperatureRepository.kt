package club.mascotfan.home.temperature.db

import club.mascotfan.home.temperature.controller.model.HistoricDataResponse
import club.mascotfan.home.temperature.controller.model.TemperatureResponse
import club.mascotfan.home.temperature.model.HumidityMessage
import club.mascotfan.home.temperature.model.TemperatureMessage
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@Repository
class SQLTemperatureRepository(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

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

    fun getHistoricDataFromSource(
        source: String,
        fromTimestamp: Instant,
        toTimestamp: Instant
    ): HistoricDataResponse {
        return HistoricDataResponse(
            source = source,
            temperatures = getHistoricTemperatureDataFromSource(source, fromTimestamp, toTimestamp),
            humidity = getHistoricHumidityDataFromSource(source, fromTimestamp, toTimestamp),
            fromTimestamp = OffsetDateTime.ofInstant(fromTimestamp, ZoneId.of("Europe/Oslo")),
            toTimestamp = OffsetDateTime.ofInstant(toTimestamp, ZoneId.of("Europe/Oslo"))
        )
    }

    private fun getHistoricTemperatureDataFromSource(
        source: String,
        fromTimestamp: Instant,
        toTimestamp: Instant
    ): List<HistoricDataResponse.Data> {
        return namedParameterJdbcTemplate.query(
            """
                select * from data.temperatures
                WHERE  source = :source AND recorded_at >= :from_timestamp AND recorded_at < :to_timestamp
                ORDER BY recorded_at 
            """.trimIndent(),
            mapOf(
                "source" to source,
                "from_timestamp" to Timestamp.from(fromTimestamp),
                "to_timestamp" to Timestamp.from(toTimestamp)
            )
        )
        { rs, _ ->
            HistoricDataResponse.Data(
                value = rs.getString("temperature_celsius")?.toDouble() ?: 0.0,
                recorded = OffsetDateTime.ofInstant(
                    rs.getTimestamp("recorded_at")?.toInstant(),
                    ZoneId.of("Europe/Oslo")
                )
            )
        }
    }

    fun getHistoricHumidityDataFromSource(
        source: String,
        fromTimestamp: Instant,
        toTimestamp: Instant
    ): List<HistoricDataResponse.Data> {
        return namedParameterJdbcTemplate.query(
            """
                select * from data.humidity
                WHERE  source = :source AND recorded_at >= :from_timestamp AND recorded_at < :to_timestamp
                ORDER BY recorded_at 
            """.trimIndent(),
            mapOf(
                "source" to source,
                "from_timestamp" to Timestamp.from(fromTimestamp),
                "to_timestamp" to Timestamp.from(toTimestamp)
            )
        )
        { rs, _ ->
            HistoricDataResponse.Data(
                value = rs.getString("humidity_percentage")?.toDouble() ?: 0.0,
                recorded = OffsetDateTime.ofInstant(
                    rs.getTimestamp("recorded_at")?.toInstant(),
                    ZoneId.of("Europe/Oslo")
                )
            )
        }
    }

    private fun getLastTemperaturesForAllSources(): Map<String, TemperatureResponse.Sources.Temperature> {
        val temperatures: MutableMap<String, TemperatureResponse.Sources.Temperature> = mutableMapOf()
        namedParameterJdbcTemplate.query(
            """
                SELECT DISTINCT ON (source) source, temperature_celsius, recorded_at
                FROM data.temperatures
                ORDER BY source, recorded_at DESC
            """.trimIndent()
        ) { rs, _ ->
            val source = rs.getString("source") ?: "UNKNOWN"
            temperatures[source] = TemperatureResponse.Sources.Temperature(
                rs.getString("temperature_celsius")?.toDouble() ?: 0.0,
                OffsetDateTime.ofInstant(rs.getTimestamp("recorded_at")?.toInstant(), ZoneId.of("Europe/Oslo"))
            )
        }

        return temperatures
    }

    private fun getLastHumdityForAllSources(): Map<String, TemperatureResponse.Sources.Humidity> {
        val humidity: MutableMap<String, TemperatureResponse.Sources.Humidity> = mutableMapOf()
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