package id.litessh.meta

import java.net.Socket
import java.io.InputStream
import java.io.OutputStream

class WebSocketSocket(
    private val underlyingSocket: Socket
) : Socket() {

    private val webSocketStream =
        WebSocketStream(underlyingSocket)

    override fun getInputStream(): InputStream {
        return webSocketStream.getInputStream()
    }

    override fun getOutputStream(): OutputStream {
        return webSocketStream.getOutputStream()
    }

    override fun close() {
        webSocketStream.close()
    }

    override fun isConnected(): Boolean {
        return underlyingSocket.isConnected
    }

    override fun isClosed(): Boolean {
        return underlyingSocket.isClosed
    }

    override fun getInetAddress() =
        underlyingSocket.inetAddress

    override fun getPort(): Int {
        return underlyingSocket.port
    }

    override fun getRemoteSocketAddress() =
        underlyingSocket.remoteSocketAddress
}
