package eduard.zaripov.productivitytimer

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
    private var isNotificationSent = false

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
                currentTime.reset()
                progressBar.visibility = View.VISIBLE
                resetButton.isEnabled = true
                startButton.isEnabled = false
                settingsButton.isEnabled = false
                progressBar.indeterminateTintList = ColorStateList.valueOf(Color.GRAY)

                CoroutineScope(IO).launch {
                    isTimerRunning = true

                    createTimerFlow()
                        .takeWhile {
                            isTimerRunning
                        }
                        .onEach {
                            if (it.isUp && isTimerRunning) {
                                withContext(Dispatchers.Main) {
                                    timeView.setTextColor(Color.RED)
                                    progressBar.indeterminateTintList =
                                        ColorStateList.valueOf(Color.RED)
                                }
                                if (!isNotificationSent) {
                                    NotificationSender.showNotification(
                                        applicationContext,
                                        Intent(applicationContext, MainActivity::class.java),
                                        1
                                    )
                                    isNotificationSent = true
                                }
                            }
                            timeView.text = it.toString()
                        }
                        .collect { }
                }
            }
        }

        resetButton = findViewById<Button>(R.id.resetButton).also {
            it.isEnabled = false
            it.setOnClickListener {
                isTimerRunning = false
                isNotificationSent = false
                progressBar.visibility = View.INVISIBLE

                startButton.isEnabled = true
                settingsButton.isEnabled = true
                resetButton.isEnabled = false

                currentTime.reset()
                timeView.text = currentTime.toString()
                timeView.setTextColor(Color.GRAY)
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

}

