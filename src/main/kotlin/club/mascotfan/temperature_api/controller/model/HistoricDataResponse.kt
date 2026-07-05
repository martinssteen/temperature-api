package club.mascotfan.temperature_api.controller.model

import java.time.OffsetDateTime

data class HistoricDataResponse(val source: String,
                                val fromTimestamp: OffsetDateTime,
                                val toTimestamp: OffsetDateTime,
                                val temperatures: List<Data>,
                                val humidity: List<Data>) {
    data class Data(
        val value: Double,
        val recorded: OffsetDateTime
    )
}