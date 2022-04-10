package eduard.zaripov.productivitytimer

import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*


class MainActivity : AppCompatActivity() {
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private lateinit var timeView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var settingsButton: Button

    private val currentTime = Timer(0, 5)
    private var isTimerRunning = false
    private var isTimerUp = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById<ProgressBar>(R.id.progressBar).also {
            it.visibility = View.INVISIBLE
            it.indeterminateTintList = ColorStateList.valueOf(Color.GRAY)
        }

        timeView = findViewById<TextView>(R.id.textView).also {
            it.text = currentTime.toString()
        }

        startButton = findViewById<Button>(R.id.startButton).also { button ->
            button.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                resetButton.isEnabled = true
                startButton.isEnabled = false
                settingsButton.isEnabled = false

                CoroutineScope(IO).launch {
                    isTimerRunning = true

                    createTimerFlow()
                        .takeWhile {
                            if (it.isUp) {
                                withContext(Dispatchers.Main) {
                                    timeView.setTextColor(Color.RED)
                                }
                                showNotification(applicationContext, Intent(applicationContext, MainActivity::class.java), 1)

                            } else {
                                timeView.text = it.toString()
                            }
                            isTimerRunning && !it.isUp
                        }
                        .collect { }
                }
            }
        }

        resetButton = findViewById<Button>(R.id.resetButton).also {
            it.isEnabled = false
            it.setOnClickListener {
                isTimerRunning = false
                progressBar.visibility = View.INVISIBLE

                currentTime.reset()
                timeView.text = currentTime.toString()
                timeView.setTextColor(Color.GRAY)

                isTimerUp = false

                startButton.isEnabled = true
                settingsButton.isEnabled = true
                resetButton.isEnabled = false
            }
        }

        settingsButton = findViewById<Button>(R.id.settingsButton).also { button ->
            button.setOnClickListener {
                val view =
                    LayoutInflater.from(this).inflate(R.layout.alert_dialog_layout, null, false)
                view.findViewById<TextInputEditText>(R.id.upperLimitSecondsEditText).also {
                    it.filters = arrayOf<InputFilter>(InputSecondsFilter())
                }

                AlertDialog.Builder(this)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val newMinutes = view.findViewById<TextInputEditText>(R.id.upperLimitMinutesEditText).text.toString().toInt()
                        val newSeconds = view.findViewById<TextInputEditText>(R.id.upperLimitSecondsEditText).text.toString().toInt()
                        currentTime.updateTimer(newMinutes, newSeconds)

                        timeView.text = currentTime.toString()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
    }

    private fun createTimerFlow(): Flow<Timer> = flow {
        while (true) {
            currentTime.decrease()
            emit(currentTime)
            delay(1000)
        }
    }

    private fun showNotification(context: Context, intent: Intent?, reqCode: Int) {
        val title = "Timer!"
        val message = "Time is up!"

        val pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = "timeExceededChannel"
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(reqCode, notificationBuilder.build())
    }
}

