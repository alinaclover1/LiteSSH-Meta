package id.litessh.meta

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.SocketFactory
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class SshEngine {

    @Volatile
    private var session: Session? = null

    fun connect(
        sshHost: String,
        sshPort: Int,
        username: String,
        password: String,
        proxyHost: String,
        proxyPort: Int,
        targetHost: String,
        userAgent: String,
        payload: String,
        timeout: Int = 15000
    ): Session {

        disconnect()

        val connector = HttpProxyConnector()

        val jsch = JSch()

        val newSession = jsch.getSession(
            username,
            sshHost,
            sshPort
        )

        newSession.setPassword(password)

        newSession.setConfig(
            "StrictHostKeyChecking",
            "no"
        )

        newSession.setSocketFactory(
            object : SocketFactory {

                override fun createSocket(
                    host: String,
                    port: Int
                ): Socket {

                    val rawSocket = connector.connect(
                        proxyHost,
                        proxyPort,
                        targetHost,
                        userAgent,
                        payload
                    )

                    return WebSocketSocket(rawSocket)
                }

                override fun getInputStream(
                    socket: Socket
                ): InputStream {

                    return socket.getInputStream()
                }

                override fun getOutputStream(
                    socket: Socket
                ): OutputStream {

                    return socket.getOutputStream()
                }
            }
        )

        try {
            newSession.connect(timeout)
            session = newSession
            return newSession

        } catch (e: Exception) {
            try {
                newSession.disconnect()
            } catch (_: Exception) {
            }

            throw e
        }
    }

    fun isConnected(): Boolean {
        return session?.isConnected == true
    }

    fun disconnect() {
        try {
            session?.disconnect()
        } catch (_: Exception) {
        }

        session = null
    }
}
