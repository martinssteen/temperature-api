package club.mascotfan.home.servercontrol.config

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DockerConfig {

    @Bean
    @Profile("!local")
    fun dockerConfig(): DockerClientConfig {
        return DefaultDockerClientConfig.createDefaultConfigBuilder().build()
    }

    @Bean
    @Profile("local")
    fun dockerConfigLocal() : DockerClientConfig {
        return DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("tcp://127.0.0.1:23750")
            .withDockerTlsVerify(false)
            .build()
    }

    @Bean
    fun dockerClient(config: DockerClientConfig): DockerClient {
        val httpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(config.dockerHost)
            .sslConfig(config.sslConfig).build()
        return DockerClientImpl.getInstance(config, httpClient)
    }
}