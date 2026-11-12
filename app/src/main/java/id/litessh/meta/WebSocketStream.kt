package id.litessh.meta

import java.io.InputStream
import java.io.OutputStream
import java.io.IOException
import java.io.ByteArrayOutputStream
import java.net.Socket
import java.security.SecureRandom

class WebSocketStream(
    private val socket: Socket
) {

    private val input = socket.getInputStream()
    private val output = socket.getOutputStream()
    private val random = SecureRandom()

    private var currentFrame = ByteArray(0)
    private var framePosition = 0

    private val writeLock = Any()

    private fun readByte(): Int {
        val value = input.read()
        if (value < 0) {
            throw IOException("WebSocket connection closed")
        }
        return value
    }

    private fun readExact(size: Int): ByteArray {
        val data = ByteArray(size)
        var offset = 0

        while (offset < size) {
            val count = input.read(data, offset, size - offset)
            if (count < 0) {
                throw IOException("Incomplete WebSocket frame")
            }
            offset += count
        }

        return data
    }

    private fun sendFrame(
        opcode: Int,
        data: ByteArray
    ) {
        synchronized(writeLock) {

            output.write(0x80 or opcode)

            if (data.size <= 125) {
                output.write(0x80 or data.size)
            } else if (data.size <= 65535) {
                output.write(0x80 or 126)
                output.write((data.size shr 8) and 255)
                output.write(data.size and 255)
            } else {
                output.write(0x80 or 127)

                for (i in 7 downTo 0) {
                    output.write(
                        ((data.size.toLong() shr (i * 8)) and 255).toInt()
                    )
                }
            }

            val mask = ByteArray(4)
            random.nextBytes(mask)

            output.write(mask)

            val masked = ByteArray(data.size)

            for (i in data.indices) {
                masked[i] = (
                    data[i].toInt() xor mask[i % 4].toInt()
                ).toByte()
            }

            output.write(masked)
            output.flush()
        }
    }

    private fun readFrame(): ByteArray? {

        val first = readByte()
        val second = readByte()

        val opcode = first and 15
        val masked = (second and 128) != 0

        var length = (second and 127).toLong()

        if (length == 126L) {
            length = (readByte().toLong() shl 8) or
                readByte().toLong()
        } else if (length == 127L) {
            length = 0

            repeat(8) {
                length = (length shl 8) or readByte().toLong()
            }
        }

        if (length > 16L * 1024L * 1024L) {
            throw IOException("WebSocket frame too large")
        }

        val mask = if (masked) readExact(4) else null
        val data = readExact(length.toInt())

        if (mask != null) {
            for (i in data.indices) {
                data[i] = (
                    data[i].toInt() xor
                        mask[i % 4].toInt()
                    ).toByte()
            }
        }

        when (opcode) {

            8 -> {
                try {
                    sendFrame(8, data)
                } catch (_: Exception) {
                }
                throw IOException("WebSocket closed by server")
            }

            9 -> {
                sendFrame(10, data)
                return ByteArray(0)
            }

            10 -> return ByteArray(0)

            1, 2 -> return data

            0 -> return data

            else -> return ByteArray(0)
        }
    }

    fun getInputStream(): InputStream {

        return object : InputStream() {

            override fun read(): Int {
                val buffer = ByteArray(1)
                val count = read(buffer, 0, 1)

                return if (count < 0) -1
                else buffer[0].toInt() and 255
            }

            override fun read(
                buffer: ByteArray,
                offset: Int,
                length: Int
            ): Int {

                if (length == 0) return 0

                while (framePosition >= currentFrame.size) {
                    currentFrame = readFrame() ?: return -1
                    framePosition = 0
                }

                val count = minOf(
                    length,
                    currentFrame.size - framePosition
                )

                System.arraycopy(
                    currentFrame,
                    framePosition,
                    buffer,
                    offset,
                    count
                )

                framePosition += count

                return count
            }
        }
    }

    fun getOutputStream(): OutputStream {

        return object : OutputStream() {

            override fun write(value: Int) {
                write(byteArrayOf(value.toByte()))
            }

            override fun write(
                data: ByteArray,
                offset: Int,
                length: Int
            ) {
                var position = offset
                val end = offset + length

                while (position < end) {
                    val count = minOf(16384, end - position)

                    val chunk = data.copyOfRange(
                        position,
                        position + count
                    )

                    sendFrame(2, chunk)
                    position += count
                }
            }

            override fun flush() {
                output.flush()
            }
        }
    }

    fun close() {
        try {
            sendFrame(8, ByteArray(0))
        } catch (_: Exception) {
        }

        socket.close()
    }
}
