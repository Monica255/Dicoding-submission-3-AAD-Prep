package com.dicoding.habitapp.ui.countdown

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.HABIT
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import com.dicoding.habitapp.utils.NOTIF_UNIQUE_WORK

class CountDownActivity : AppCompatActivity() {
    private lateinit var workManager: WorkManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"
        workManager = WorkManager.getInstance(this)

        val habit = intent.getParcelableExtra<Habit>(HABIT) as Habit

        findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

        val viewModel = ViewModelProvider(this)[CountDownViewModel::class.java]

        //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished

        viewModel.setInitialTime(habit.minutesFocus)
        viewModel.currentTimeString.observe(this){
            findViewById<TextView>(R.id.tv_count_down).text=it
        }

        viewModel.eventCountDownFinish.observe(this){
            updateButtonState(!it)
            if(it)startOneTimeTask(habit)

        }

        //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            viewModel.startTimer()
            updateButtonState(true)

        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            viewModel.resetTimer()
            cancelOneTimeTask()
        }
    }

    private fun startOneTimeTask(habit:Habit) {
        val data = Data.Builder()
            .putInt(HABIT_ID, habit.id)
            .putString(HABIT_TITLE,habit.title)
            .build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        val oneTimeWorkRequest =
            OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInputData(data)
                .addTag(NOTIF_UNIQUE_WORK)
                .setConstraints(constraints)
                .build()
        workManager.enqueueUniqueWork(NOTIF_UNIQUE_WORK,ExistingWorkPolicy.KEEP,oneTimeWorkRequest)
    }

    private fun cancelOneTimeTask(){
        workManager.cancelUniqueWork(NOTIF_UNIQUE_WORK)
    }

    private fun updateButtonState(isRunning: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }
}