package id.litessh.meta

import java.net.Socket
import java.net.InetSocketAddress
import java.io.BufferedInputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

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
                InetSocketAddress(proxyHost, proxyPort),
                15000
            )

            socket.soTimeout = 15000

            val authority = targetHost
                .substringBefore("@")
                .trim()

            val hostname = authority
                .substringBeforeLast(":")
                .takeIf {
                    authority.substringAfterLast(":").toIntOrNull() != null
                } ?: authority

            val randomBytes = ByteArray(16)
            SecureRandom().nextBytes(randomBytes)

            val key = Base64.getEncoder()
                .encodeToString(randomBytes)

            var request = payloadTemplate
                .replace("[crlf]", "\r\n")
                .replace("[host_port]", authority)
                .replace("[host]", hostname)
                .replace("[ua]", userAgent)

            request = request.trimEnd('\r', '\n')

            if (!request.contains(
                    Regex("(?im)^Sec-WebSocket-Key:")
                )
            ) {
                request += "\r\nSec-WebSocket-Key: $key"
            }

            if (!request.contains(
                    Regex("(?im)^Sec-WebSocket-Version:")
                )
            ) {
                request += "\r\nSec-WebSocket-Version: 13"
            }

            request += "\r\n\r\n"

            val output = socket.getOutputStream()

            output.write(request.toByteArray(Charsets.UTF_8))
            output.flush()

            val input = BufferedInputStream(
                socket.getInputStream()
            )

            val response = StringBuilder()

            while (response.length < 16384) {

                val value = input.read()

                if (value == -1) {
                    throw IOException("Proxy closed connection")
                }

                response.append(value.toChar())

                if (response.endsWith("\r\n\r\n")) {
                    break
                }
            }

            val responseText = response.toString()

            val statusLine = responseText
                .substringBefore("\r\n")

            if (!statusLine.contains(" 101 ")) {
                throw IOException(
                    "WebSocket rejected: $statusLine"
                )
            }

            val expectedAccept = Base64.getEncoder()
                .encodeToString(
                    MessageDigest.getInstance("SHA-1").digest(
                        (
                            key +
                            "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
                        ).toByteArray(Charsets.UTF_8)
                    )
                )

            val actualAccept = responseText
                .lineSequence()
                .firstOrNull {
                    it.startsWith(
                        "Sec-WebSocket-Accept:",
                        ignoreCase = true
                    )
                }
                ?.substringAfter(":")
                ?.trim()

            if (actualAccept != expectedAccept) {
                throw IOException(
                    "Invalid WebSocket handshake"
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
