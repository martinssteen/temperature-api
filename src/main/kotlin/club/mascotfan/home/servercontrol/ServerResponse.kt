package club.mascotfan.home.servercontrol


sealed class ServerResponse(val serverStatus: ServerStatus) {
    enum class ServerStatus{ UP, DOWN}
    class Up(
        val serverName: String,
        val mapName: String,
        val numPlayers: Int,
        val maxPlayers: Int,
    ) : ServerResponse(ServerStatus.UP)
    class Down() : ServerResponse(ServerStatus.DOWN)
}