package club.mascotfan.temperature_api.model

data class HumidityMessage(
    val source: String,
    val humidity: Double
)