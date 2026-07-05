package club.mascotfan.temperature_api.controller

import club.mascotfan.temperature_api.db.SQLTemperatureRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping


@Controller
class HtmlController(
    private val sqlTemperatureRepository: SQLTemperatureRepository
) {
    @GetMapping("/temperature/static")
    fun webpage(model: Model): String {
        model.addAttribute("sources", sqlTemperatureRepository.getLastTemperatures().sources)
        return "temperatures"
    }
}
