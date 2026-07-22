package club.mascotfan.home.servercontrol

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/docker")
class ServerControlController(private val serverControlService: ServerControlService) {
    @GetMapping("/list")
    fun getAllDockerContainers(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(serverControlService.getAllContainers())
    }

    @GetMapping("/logs")
    fun getAllDockerLogs(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(serverControlService.getLogs())
    }

    @GetMapping("/server")
    fun test(): ResponseEntity<ServerResponse> {
        return ResponseEntity.ok(serverControlService.getServerInfo())
    }

    @GetMapping("/start")
    fun startServer(): ResponseEntity<Unit> {
        return ResponseEntity.ok(serverControlService.startCS2Server())
    }
}