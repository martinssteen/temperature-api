package club.mascotfan.temperature_api.controller

import club.mascotfan.temperature_api.controller.model.HistoricDataResponse
import club.mascotfan.temperature_api.controller.model.TemperatureResponse
import club.mascotfan.temperature_api.db.SQLTemperatureRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class TemperatureController(
    private val sqlTemperatureRepository: SQLTemperatureRepository
) {

    @GetMapping("/temperature")
    fun temperature(): ResponseEntity<TemperatureResponse> {
        try {
            return ResponseEntity.ok(sqlTemperatureRepository.getLastTemperatures())
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/temperature/source/{source}")
    fun getTempsForSourceDuringPeriod(
        @PathVariable source: String,
        @RequestParam(value = "fromTimestamp", required = false) _fromTimestamp: Instant?,
        @RequestParam(value = "toTimestamp", required = false) _toTimestamp: Instant?
    ): ResponseEntity<HistoricDataResponse> {
        val fromTimestamp = _fromTimestamp ?: Instant.now().minusSeconds(60 * 60 * 24 * 7) // default to 7 days ago
        val toTimestamp = _toTimestamp ?: Instant.now()
        try {
            return ResponseEntity.ok(
                sqlTemperatureRepository.getHistoricDataFromSource(
                    source,
                    fromTimestamp,
                    toTimestamp
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.internalServerError().build()
        }
    }
}
