package club.mascotfan.home.temperature.controller

import club.mascotfan.home.temperature.db.SQLTemperatureRepository
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
