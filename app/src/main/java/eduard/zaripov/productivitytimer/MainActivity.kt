package eduard.zaripov.productivitytimer

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.*

val CHANNEL_ID = "timeExceededChannel"

class MainActivity : AppCompatActivity() {
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private lateinit var timeView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var settingsButton: Button

    val currentTime = Time()
    var isTimerRunning = false
    var isNotificationSend = false

    val handler = Handler(Looper.getMainLooper())

    private val addSecond: Runnable = object : Runnable {
        override fun run() {
            if (isTimerRunning) {
                currentTime.addOneSecond()
                if (currentTime.isExceededLimit() && !isNotificationSend) {
                    timeView.setTextColor(Color.RED)
                    showNotification(applicationContext)
                    isNotificationSend = true
                }

                if (progressBar.indeterminateTintList == ColorStateList.valueOf(Color.RED)) {
                    progressBar.indeterminateTintList = ColorStateList.valueOf(Color.BLUE)
                } else {
                    progressBar.indeterminateTintList = ColorStateList.valueOf(Color.RED)
                }
            }
            timeView.text = currentTime.toString()

            handler.postDelayed(this, 1000)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)
        timeView = findViewById(R.id.textView)
        settingsButton = findViewById(R.id.settingsButton)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE

        startButton.setOnClickListener {
            runTimer()
            settingsButton.isEnabled = false
        }

        resetButton.setOnClickListener {
            resetTimer()
            isNotificationSend = false
            settingsButton.isEnabled = true
        }

        settingsButton.setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.alert_dialog_layout, null, false)
            AlertDialog.Builder(this)
                .setTitle("Set upper limit in seconds")
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    currentTime.limitSeconds =
                        view.findViewById<EditText>(R.id.upperLimitEditText).text.toString().toInt()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun runTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            progressBar.visibility = View.VISIBLE
            handler.postDelayed(addSecond, 1000)
        }
    }

    private fun resetTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            handler.removeCallbacks(addSecond)

            progressBar.visibility = View.INVISIBLE

            currentTime.reset()
            timeView.setTextColor(Color.GRAY)
            timeView.text = currentTime.toString()
        }
    }

    private fun showNotification(context: Context) {
        val name = "Notification"
        val descriptionText = "Time exceeded"
        val importance = NotificationManager.IMPORTANCE_HIGH
        NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Notification")
            .setContentText("Time exceeded")
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
    }
}

