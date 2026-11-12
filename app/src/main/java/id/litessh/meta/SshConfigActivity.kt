package id.litessh.meta

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.text.InputType

class SshConfigActivity : Activity() {

    private lateinit var sshHost: EditText
    private lateinit var sshPort: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText

    private lateinit var proxyHost: EditText
    private lateinit var proxyPort: EditText
    private lateinit var target: EditText

    private lateinit var payload: EditText
    private lateinit var userAgent: EditText
    private lateinit var wsPath: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(
            "ssh_config",
            MODE_PRIVATE
        )

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(Color.rgb(15, 20, 32))
        }

        val scroll = ScrollView(this)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        fun title(text: String) {
            val view = TextView(this).apply {
                this.text = text
                textSize = 19f
                setTextColor(Color.WHITE)
                setPadding(0, 22, 0, 10)
            }
            form.addView(view)
        }

        fun field(
            label: String,
            key: String,
            defaultValue: String = "",
            inputType: Int = InputType.TYPE_CLASS_TEXT,
            multiline: Boolean = false
        ): EditText {

            val labelView = TextView(this).apply {
                text = label
                textSize = 14f
                setTextColor(Color.LTGRAY)
            }

            form.addView(labelView)

            val edit = EditText(this).apply {
                setText(prefs.getString(key, defaultValue))
                setTextColor(Color.WHITE)
                setHintTextColor(Color.GRAY)
                hint = label
                this.inputType = inputType

                if (multiline) {
                    setSingleLine(false)
                    minLines = 4
                    gravity = Gravity.TOP
                } else {
                    setSingleLine(true)
                }
            }

            form.addView(edit)

            return edit
        }

        val header = TextView(this).apply {
            text = "LITESSSH META"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        form.addView(header)

        title("SSH CONFIGURATION")

        sshHost = field("SSH Host", "host")
        sshPort = field(
            "SSH Port",
            "port",
            "22",
            InputType.TYPE_CLASS_NUMBER
        )

        username = field("Username", "username")

        password = field(
            "Password",
            "password",
            "",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        title("PROXY CONFIGURATION")

        proxyHost = field("Proxy Host", "proxy_host")

        proxyPort = field(
            "Proxy Port",
            "proxy_port",
            "80",
            InputType.TYPE_CLASS_NUMBER
        )

        target = field("Target", "target")

        title("PAYLOAD CONFIGURATION")

        payload = field(
            "Custom Payload",
            "payload",
            "GET / HTTP/1.1[crlf]Host: [host][crlf]Connection: Upgrade[crlf]User-Agent: [ua][crlf]Upgrade: websocket[crlf][crlf]",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE,
            true
        )

        userAgent = field(
            "User-Agent",
            "user_agent",
            "Mozilla/5.0"
        )

        wsPath = field(
            "WebSocket Path",
            "ws_path",
            "/"
        )

        title("CONNECTION MODE")

        val mode = TextView(this).apply {
            text = "Performance Mode  •  Proxy → Target"
            textSize = 16f
            setTextColor(Color.WHITE)
        }

        form.addView(mode)

        val saveButton = Button(this).apply {
            text = "SAVE CONFIGURATION"

            setOnClickListener {

                prefs.edit()
                    .putString("host", sshHost.text.toString())
                    .putString("port", sshPort.text.toString())
                    .putString("username", username.text.toString())
                    .putString("password", password.text.toString())
                    .putString("proxy_host", proxyHost.text.toString())
                    .putString("proxy_port", proxyPort.text.toString())
                    .putString("target", target.text.toString())
                    .putString("payload", payload.text.toString())
                    .putString("user_agent", userAgent.text.toString())
                    .putString("ws_path", wsPath.text.toString())
                    .apply()

                Toast.makeText(
                    this@SshConfigActivity,
                    "Configuration Saved!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        form.addView(saveButton)

        scroll.addView(form)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
    }
}
