package club.mascotfan.home.servercontrol.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "server-control")
data class ServerControlConfigurationProperties(
    val whitelist: List<String>
) {

    fun isWhitelisted(name: String) : Boolean = whitelist.contains(name)
}