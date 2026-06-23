package club.mascotfan.temperature_api.controller.model

import java.time.OffsetDateTime

data class TemperatureResponse(
    val sources: List<Sources>
) {
    data class Sources(
        val source : String,
        val temperature: Temperature?,
        val humidity: Humidity?
    ) {
        data class Temperature(
            val degreesCelsius: Double,
            val lastRecording: OffsetDateTime
        )

        data class Humidity(
            val humidityPercent: Double,
            val lastRecording: OffsetDateTime
        )
    }
}