package club.mascotfan.home.temperature

import club.mascotfan.home.temperature.db.SQLTemperatureRepository
import club.mascotfan.home.temperature.model.HumidityMessage
import club.mascotfan.home.temperature.model.TemperatureMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper


//Handles messages from MQTT

@Service
class MessageHandlerService(
    private val jsonMapper: JsonMapper,
    private val sqlTemperatureRepository: SQLTemperatureRepository,
    @Value($$"${temperature.log-temperature}") private val recordTemperature: Boolean
) {

    private val log = LoggerFactory.getLogger(MessageHandlerService::class.java)
    fun handleTemperatureMessage(source: String, payload: String) {
        log.debug("Temperature message receieved from $source.")
        if (recordTemperature) {
            val json = jsonMapper.readTree(payload)
            val temperatureMessage = TemperatureMessage(
                source = source,
                temperatureCelsius = json["tC"].asDouble()
            )
            sqlTemperatureRepository.recordTemperature(temperatureMessage)
        } else {
            log.debug("Temperature not recorded as recordTemperature = false")
        }
    }

    fun handleHumidityMessage(source: String, payload: String) {
        log.debug("Humidity message received from $source.")
        if (recordTemperature) {
            val json = jsonMapper.readTree(payload)
            val humidityMessage = HumidityMessage(
                source = source,
                humidity = json["rh"].asDouble()
            )
            sqlTemperatureRepository.recordHumidity(humidityMessage)
        } else {
            log.debug("Humidity not recorded as recordTemperature = false")
        }
    }
}