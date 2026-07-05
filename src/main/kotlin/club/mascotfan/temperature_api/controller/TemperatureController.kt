package club.mascotfan.temperature_api.controller

import club.mascotfan.temperature_api.controller.model.TemperatureResponse
import club.mascotfan.temperature_api.db.SQLTemperatureRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

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
}
