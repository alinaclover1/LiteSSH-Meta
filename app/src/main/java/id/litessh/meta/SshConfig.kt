package id.litessh.meta

data class SshConfig(
    var host: String = "",
    var port: Int = 22,
    var username: String = "",
    var password: String = "",
    var protocol: String = "SSH_WEBSOCKET",
    var websocketPath: String = "/",
    var sni: String = "",
    var nameserver: String = "",
    var publicKey: String = ""
)
