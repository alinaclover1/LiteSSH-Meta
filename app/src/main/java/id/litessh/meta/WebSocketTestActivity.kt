package id.litessh.meta

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.net.Socket

class WebSocketTestActivity : Activity() {

    private lateinit var resultText: TextView
    private lateinit var testButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.rgb(18, 18, 18))
        }

        val title = TextView(this).apply {
            text = "WEBSOCKET CONNECTION TEST"
            textSize = 20f
            setTextColor(Color.WHITE)
        }

        resultText = TextView(this).apply {
            text = "Ready to test"
            textSize = 15f
            setTextColor(Color.LTGRAY)
            setPadding(0, 30, 0, 30)
        }

        testButton = Button(this).apply {
            text = "TEST WEBSOCKET"
            setOnClickListener {
                runTest()
            }
        }

        layout.addView(title)
        layout.addView(resultText)
        layout.addView(testButton)

        setContentView(layout)
    }

    private fun runTest() {
        val prefs = getSharedPreferences("ssh_config", MODE_PRIVATE)

        val proxyHost = prefs.getString("proxy_host", "") ?: ""
        val proxyPort = prefs.getInt("proxy_port", 80)
        val target = prefs.getString("target", "") ?: ""
        val userAgent = prefs.getString("user_agent", "") ?: ""
        val payload = prefs.getString("payload", "") ?: ""

        if (proxyHost.isBlank() || target.isBlank()) {
            resultText.text = "ERROR: Proxy Host atau Target masih kosong."
            return
        }

        testButton.isEnabled = false
        resultText.text = "Connecting..."

        Thread {
            var socket: Socket? = null

            try {
                socket = HttpProxyConnector.connect(
                    proxyHost,
                    proxyPort,
                    target,
                    userAgent,
                    payload
                )

                runOnUiThread {
                    resultText.text = "WEBSOCKET HANDSHAKE SUCCESS!"
                }

            } catch (e: Exception) {
                runOnUiThread {
                    resultText.text =
                        "CONNECTION FAILED:\n${e.message}"
                }

            } finally {
                try {
                    socket?.close()
                } catch (_: Exception) {
                }

                runOnUiThread {
                    testButton.isEnabled = true
                }
            }
        }.start()
    }
}
