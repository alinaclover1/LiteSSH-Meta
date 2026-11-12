package id.litessh.meta

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.ViewGroup
import android.widget.*

class SshConfigActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.rgb(15, 20, 32))
        }

        fun field(label: String, hint: String): EditText {
            val title = TextView(this).apply {
                text = label
                setTextColor(Color.WHITE)
                textSize = 15f
            }
            layout.addView(title)

            val input = EditText(this).apply {
                this.hint = hint
                setTextColor(Color.WHITE)
                setHintTextColor(Color.GRAY)
                setSingleLine(true)
            }
            layout.addView(input)
            return input
        }

        val title = TextView(this).apply {
            text = "SSH Configuration"
            textSize = 24f
            setTextColor(Color.WHITE)
        }
        layout.addView(title)

        val host = field("SSH Host", "example.com")
        val port = field("SSH Port", "22")
        val username = field("Username", "username")
        val password = field("Password", "password")
        val wsPath = field("WebSocket Path", "/")
        val sni = field("SNI", "example.com")
        val dns = field("SlowDNS Nameserver", "ns.example.com")
        val key = field("SlowDNS Public Key", "Public key")

        val save = Button(this).apply {
            text = "SAVE CONFIGURATION"
            setOnClickListener {
                val prefs = getSharedPreferences("ssh_config", MODE_PRIVATE)
                prefs.edit()
                    .putString("host", host.text.toString())
                    .putString("port", port.text.toString())
                    .putString("username", username.text.toString())
                    .putString("password", password.text.toString())
                    .putString("wsPath", wsPath.text.toString())
                    .putString("sni", sni.text.toString())
                    .putString("dns", dns.text.toString())
                    .putString("key", key.text.toString())
                    .apply()

                Toast.makeText(
                    this@SshConfigActivity,
                    "Configuration saved",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        layout.addView(save)
        
        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }
}
