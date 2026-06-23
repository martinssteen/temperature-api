package club.mascotfan.temperature_api.model

data class TemperatureMessage(
    val source: String,
    val temperatureCelsius: Double
)

