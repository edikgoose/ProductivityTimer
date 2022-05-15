package eduard.zaripov.productivitytimer

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import eduard.zaripov.productivitytimer.databinding.ActivityMainBinding
import java.lang.NumberFormatException


class MainActivity : AppCompatActivity() {
    private var isNotificationSent = false

    private lateinit var binding: ActivityMainBinding
    private val timerViewModel: TimerViewModel = TimerViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }
        setContentView(binding.root)

        with(binding.progressBar) {
            visibility = View.INVISIBLE
            indeterminateTintList = ColorStateList.valueOf(Color.GRAY)
        }

        binding.startButton.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            changeStateOfButtons(startState = false, resetState = true, settingsState = false)
            binding.progressBar.indeterminateTintList = ColorStateList.valueOf(Color.GRAY)

            timerViewModel.startTimer()
        }

        binding.resetButton.also {
            it.isEnabled = false
            it.setOnClickListener {
                setDefaultStateOfUI()
                timerViewModel.stopTimer()
            }
        }

        binding.settingsButton.setOnClickListener {
            createAlertDialog()
        }

        timerViewModel.timer.observe(this) { timer ->
            if (timer >= 0) {
                binding.timeView.text = secondsToString(timer)
            }
            else {
                if (!isNotificationSent) {
                    NotificationSender.showNotification(
                        applicationContext,
                        Intent(applicationContext, MainActivity::class.java),
                        1
                    )
                    isNotificationSent = true
                }
                setTimerIsUpStateOfUI()
            }
        }

    }

    private fun createAlertDialog() {
        val view =
            LayoutInflater.from(this).inflate(R.layout.alert_dialog_layout, null, false)
        view.findViewById<TextInputEditText>(R.id.upperLimitSecondsEditText).also {
            it.filters = arrayOf<InputFilter>(InputTimeFilter())
        }
        view.findViewById<TextInputEditText>(R.id.upperLimitMinutesEditText).also {
            it.filters = arrayOf<InputFilter>(InputTimeFilter(max = 240)) // max 4 hours
        }

        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                try {
                    val newMinutes =
                        view.findViewById<TextInputEditText>(R.id.upperLimitMinutesEditText).text.toString()
                            .toInt()
                    val newSeconds =
                        view.findViewById<TextInputEditText>(R.id.upperLimitSecondsEditText).text.toString()
                            .toInt()
                    timerViewModel.initialTime = newMinutes * 60 + newSeconds
                    timerViewModel.updateTimer(newMinutes * 60 + newSeconds)
                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Please, input values!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun setTimerIsUpStateOfUI() {
        binding.timeView.setTextColor(Color.RED)
        binding.progressBar.indeterminateTintList =
            ColorStateList.valueOf(Color.RED)
    }

    private fun setDefaultStateOfUI() {
        isNotificationSent = false
        binding.timeView.setTextColor(Color.GRAY)
        binding.progressBar.visibility = View.INVISIBLE
        changeStateOfButtons(startState = true, resetState = false, settingsState = true)
    }

    private fun changeStateOfButtons(startState: Boolean, resetState: Boolean, settingsState: Boolean) {
        binding.startButton.isEnabled = startState
        binding.resetButton.isEnabled = resetState
        binding.settingsButton.isEnabled = settingsState
    }

    private fun secondsToString(seconds: Int) = String.format("%02d:%02d", seconds / 60, seconds % 60)
}

