package club.mascotfan.home.servercontrol

import club.mascotfan.home.servercontrol.config.ServerControlConfigurationProperties
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import com.ibasco.agql.core.util.GeneralOptions
import com.ibasco.agql.protocols.valve.source.query.SourceQueryClient
import com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Service
import org.springframework.web.ErrorResponseException
import java.net.InetSocketAddress

class HomeApiException(val errorCode: Int, title: String, detail: String, cause: Throwable? = null) :
    ErrorResponseException(HttpStatusCode.valueOf(errorCode), run {
        val problem = ProblemDetail.forStatus(errorCode)
        problem.title = title
        problem.detail = detail
        return@run problem
    }, cause)


@Service
class ServerControlService(
    private val dockerClient: DockerClient,
    private val serverControlConfigurationProperties: ServerControlConfigurationProperties
) {
    fun getAllContainers(): List<String> {
        return dockerClient.listContainersCmd().withShowAll(true).exec()
            .map { it.names.joinToString(separator = " | ") }
    }

    fun startCS2Server(): Unit {
        val first = dockerClient.listContainersCmd().withShowAll(true).exec()
            .first { it.names.joinToString().contains("cs2-dedicated") }
        dockerClient.startContainerCmd(first.id).exec()
    }

    fun getLogs(): List<String> {
        val dockerLogs = mutableListOf<String>()
        val container =
            dockerClient.listContainersCmd().exec().first { it.names.joinToString().contains("cs2-dedicated") }
        dockerClient.logContainerCmd(container.id)
            .withStdOut(true)    // Capture standard output
            .withStdErr(true)    // Capture standard error
            .withTail(10)      // Fetch only the last 100 lines
            .exec(object : ResultCallback.Adapter<Frame>() {
                override fun onNext(frame: Frame) {
                    dockerLogs.add(String(frame.payload))
                }
            }).awaitCompletion()
        return dockerLogs.toList()
    }

    fun getServerInfo(): ServerResponse {
        val address = InetSocketAddress("192.168.50.137", 27015)

        val options = SourceQueryOptions.builder()
            .option(GeneralOptions.READ_TIMEOUT, 1000)
            .option(GeneralOptions.WRITE_TIMEOUT, 1000)
            .option(GeneralOptions.SOCKET_CONNECT_TIMEOUT, 1000)
            .build()
        try {
            SourceQueryClient(options).use { client ->
                val server = client.getInfo(address).join().result
                return ServerResponse.Up(
                    server.name,
                    server.mapName,
                    server.numOfPlayers,
                    server.maxPlayers
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ServerResponse.Down()
    }
}