package top.niunaijun.blackboxa.view.log

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.util.toast
import kotlin.concurrent.thread

/**
 * Shows recent log output so a crash can be copied and shared.
 *
 * Virtual apps run under this app's own uid, and Android lets an app read the
 * log lines of its own uid, so a game started inside the container shows up
 * here, including native crash summaries from the "crash" buffer.
 */
class CrashLogActivity : AppCompatActivity() {

    private lateinit var logView: TextView
    private lateinit var scroll: ScrollView
    private var logText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.crash_log)

        val pad = (12 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        val hint = TextView(this).apply {
            text = getString(R.string.crash_log_hint)
            setPadding(0, 0, 0, pad)
        }
        root.addView(hint)

        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val refresh = Button(this).apply {
            text = getString(R.string.crash_log_refresh)
            setOnClickListener { load() }
        }
        val copy = Button(this).apply {
            text = getString(R.string.crash_log_copy)
            setOnClickListener { copyLog() }
        }
        val share = Button(this).apply {
            text = getString(R.string.crash_log_share)
            setOnClickListener { shareLog() }
        }
        val weight = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        buttons.addView(refresh, weight)
        buttons.addView(copy, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
        buttons.addView(share, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
        root.addView(buttons)

        logView = TextView(this).apply {
            typeface = Typeface.MONOSPACE
            textSize = 10f
            setTextIsSelectable(true)
        }
        val hScroll = HorizontalScrollView(this).apply { addView(logView) }
        scroll = ScrollView(this).apply { addView(hScroll) }
        root.addView(scroll, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))

        setContentView(root)
        load()
    }

    private fun load() {
        logView.text = getString(R.string.crash_log_loading)
        thread {
            val text = readLog()
            runOnUiThread {
                logText = text
                logView.text = text
                scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }
    }

    private fun readLog(): String {
        val header = buildString {
            append("Device: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
            append(" | Android ").append(Build.VERSION.RELEASE)
            append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n")
            append("ABI: ").append(Build.SUPPORTED_ABIS.joinToString()).append("\n\n")
        }
        return try {
            val process = ProcessBuilder(
                "logcat", "-d", "-v", "time",
                "-b", "main", "-b", "system", "-b", "crash",
                "-t", "4000", "*:W"
            ).redirectErrorStream(true).start()
            val lines = process.inputStream.bufferedReader().readLines()
            process.waitFor()
            if (lines.isEmpty()) return header + getString(R.string.crash_log_empty)

            // Lines that never explain a crash but fill the log on OPPO/ColorOS
            val noise = listOf(
                "avc:", "VRI[", "OplusBracketLog", "OpenGLRenderer", "OplusCustomize",
                "WindowOnBackDispatcher", "/FA ", "GCloudVoice", "/INTL ", "SchedAssist",
                "ApkAssets", "hiddenapi", "BootstrapClass", "/QT ", "FeatureFlags"
            )
            val useful = lines.filter { l -> noise.none { l.contains(it) } }

            // Put fatal errors first so they survive copy/paste size limits
            val fatal = useful.filter {
                it.contains("FATAL") || it.contains("/AndroidRuntime") || it.contains("/DEBUG") ||
                    it.contains("Fatal signal") || it.contains("/libc ") || it.contains(" F/") ||
                    it.contains("ActionBarCrashGuard") || it.contains("DeviceSpoofer") ||
                    it.contains("Abort message") || it.contains("backtrace") || it.contains("#0")
            }
            buildString {
                append(header)
                append("=== FATAL / KEY LINES (").append(fatal.size).append(") ===\n")
                fatal.takeLast(300).forEach { append(it).append('\n') }
                append("\n=== RECENT LOG (last 400 filtered lines) ===\n")
                useful.takeLast(400).forEach { append(it).append('\n') }
            }
        } catch (t: Throwable) {
            header + "logcat failed: " + t
        }
    }

    private fun copyLog() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // Keep the newest part: a clipboard entry that is too large can be rejected
        cm.setPrimaryClip(ClipData.newPlainText("crash log", logText.takeLast(60_000)))
        toast(R.string.crash_log_copied)
    }

    private fun shareLog() {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, logText.takeLast(60_000))
        }
        startActivity(Intent.createChooser(send, getString(R.string.crash_log)))
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, CrashLogActivity::class.java))
        }
    }
}
