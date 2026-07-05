package club.mascotfan.temperature_api

import club.mascotfan.temperature_api.db.SQLTemperatureRepository
import club.mascotfan.temperature_api.model.HumidityMessage
import club.mascotfan.temperature_api.model.TemperatureMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper


//Handles messages from MQTT

@Service
class MessageHandlerService(private val jsonMapper: JsonMapper, private val sqlTemperatureRepository: SQLTemperatureRepository) {

    val log = LoggerFactory.getLogger(MessageHandlerService::class.java)
    fun handleTemperatureMessage(source: String, payload: String) {
        log.debug("Temperature message receieved from $source.")
        val json = jsonMapper.readTree(payload)
        val temperatureMessage = TemperatureMessage(
            source = source,
            temperatureCelsius = json["tC"].asDouble()
        )
        sqlTemperatureRepository.recordTemperature(temperatureMessage)
    }
    fun handleHumidityMessage(source: String, payload: String) {
        log.debug("Humidity message received from $source.")
        val json = jsonMapper.readTree(payload)
        val humidityMessage = HumidityMessage(
            source = source,
            humidity = json["rh"].asDouble()
        )
        sqlTemperatureRepository.recordHumidity(humidityMessage)
    }
}