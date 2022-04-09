package eduard.zaripov.productivitytimer

import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*

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

        startButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            resetButton.isEnabled = true
            startButton.isEnabled = false
            settingsButton.isEnabled = false

            CoroutineScope(IO).launch {
                isTimerRunning = true

                startTimer()
                    .takeWhile {
                        isTimerRunning
                    }
                    .onEach {
                        timeView.text = it.toString()
                        if (currentTime.isExceededLimit() && !isNotificationSend) {
                            withContext(Dispatchers.Main) {
                                timeView.setTextColor(Color.RED)
                            }
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            showNotification(applicationContext, "Timer!", "Time is up! ", intent, 1)
                            isNotificationSend = true
                        }

                    }
                    .collect { }
            }
        }

        resetButton.isEnabled = false
        resetButton.setOnClickListener {
            isTimerRunning = false
            progressBar.visibility = View.INVISIBLE

            currentTime.reset()
            timeView.text = currentTime.toString()
            timeView.setTextColor(Color.GRAY)

            isNotificationSend = false

            startButton.isEnabled = true
            settingsButton.isEnabled = true
            resetButton.isEnabled = false
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
    }


    fun showNotification(context: Context, title: String?, message: String?, intent: Intent?, reqCode: Int) {
        val pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = "timeExceededChannel" // The id of the channel.
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            reqCode,
            notificationBuilder.build()
        ) // 0 is the request code, it should be unique id
        Log.d("showNotification", "showNotification: $reqCode")
    }
}

