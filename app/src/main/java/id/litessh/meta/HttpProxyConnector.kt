package id.litessh.meta

import java.net.Socket
import java.io.BufferedInputStream
import java.io.OutputStream
import java.io.IOException

class HttpProxyConnector {

    fun connect(
        proxyHost: String,
        proxyPort: Int,
        targetHost: String,
        userAgent: String,
        payloadTemplate: String
    ): Socket {

        val socket = Socket()

        try {
            socket.connect(
                java.net.InetSocketAddress(proxyHost, proxyPort),
                10000
            )

            socket.soTimeout = 15000

            val payload = payloadTemplate
                .replace("[crlf]", "\r\n")
                .replace("[host_port]", "$targetHost:80")
                .replace("[host]", targetHost)
                .replace("[ua]", userAgent)

            val output: OutputStream = socket.getOutputStream()

            output.write(
                payload.toByteArray(Charsets.UTF_8)
            )

            output.flush()

            val input = BufferedInputStream(
                socket.getInputStream()
            )

            val response = StringBuilder()
            val buffer = ByteArray(1)

            while (response.length < 16384) {

                val count = input.read(buffer)

                if (count == -1) {
                    throw IOException("Proxy closed connection")
                }

                response.append(
                    String(buffer, 0, count, Charsets.ISO_8859_1)
                )

                if (response.toString().endsWith("\r\n\r\n")) {
                    break
                }
            }

            val statusLine = response.toString()
                .substringBefore("\r\n")

            if (!statusLine.contains(" 101 ")) {
                throw IOException(
                    "WebSocket handshake rejected: $statusLine"
                )
            }

            return socket

        } catch (e: Exception) {
            try {
                socket.close()
            } catch (_: Exception) {
            }

            throw e
        }
    }
}
