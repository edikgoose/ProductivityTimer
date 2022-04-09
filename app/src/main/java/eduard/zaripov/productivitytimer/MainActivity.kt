package eduard.zaripov.productivitytimer

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)
        timeView = findViewById(R.id.textView)
        settingsButton = findViewById(R.id.settingsButton)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE

        var flowOfTimer: Flow<Time>? = null

        startButton.setOnClickListener {
            CoroutineScope(IO).launch {
                isTimerRunning = true
                flowOfTimer = startTimer()
                flowOfTimer!!
                    .takeWhile {
                        isTimerRunning
                    }
                    .onEach {
                        timeView.text = it.toString()
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

                    .collect {  }
            }
            settingsButton.isEnabled = false
        }

        resetButton.setOnClickListener {
            isTimerRunning = false

            currentTime.reset()
            timeView.text = currentTime.toString()
            timeView.setTextColor(Color.GRAY)

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

    private fun startTimer(): Flow<Time> = flow {
        while (true) {
            currentTime.addOneSecond()
            delay(1000)
            emit(currentTime)
        }
    }.flowOn(IO)


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

