package com.example.android.sossego.ui.meditation

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.example.android.sossego.R
import com.example.android.sossego.receiver.MeditationAlarmReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


class MeditationTimerViewModel(app: Application) : AndroidViewModel(app) {

    companion object{
        private const val TAG = "MediationViewModel"
    }
    private val requestCode = 1101
    private val triggerAtTime = "TRIGGER_AT"

    private val minute: Long = 60_000L
    private val second: Long = 1_000L

    private val timerLengthOptions: IntArray
    private val notifyPendingIntent: PendingIntent

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val prefs = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    private val notifyIntent = Intent(app, MeditationAlarmReceiver::class.java)

    private val _timeSelection = MutableLiveData<Int>()
    val timeSelection: LiveData<Int>
        get() = _timeSelection

    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long>
        get() = _elapsedTime

    private var _alarmOn = MutableLiveData<Boolean>()
    val isAlarmOn: LiveData<Boolean>
        get() = _alarmOn


    private lateinit var timer: CountDownTimer

    init {
        // determine if alarm state is on/off based on if there is a pending intent
        _alarmOn.value = PendingIntent.getBroadcast(
            app,
            requestCode,
            notifyIntent,
            PendingIntent.FLAG_NO_CREATE
        ) != null

        // One uses PendingIntent.getBroadcast() to call a broadcast receiver when the alarm
        // goes off and inside that receiver the service to do the real work is started.
        // this in turn calls the notifyIntent for the real work which is the AlarmReceiver doing
        // the Toast
        notifyPendingIntent = PendingIntent.getBroadcast(
            app,
            requestCode,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        timerLengthOptions = app.resources.getIntArray(
            R.array.mediation_minutes_array)

        //If alarm is not null, resume the timer back for this alarm
        if (_alarmOn.value!!) {
            createTimer()
        }

    }

    /**
     * Turns on or off the alarm
     *
     * @param isChecked, alarm status to be set.
     */
    fun setAlarm(isChecked: Boolean) {
        when (isChecked) {
            true -> timeSelection.value?.let { startTimer(it) }
            false -> cancelNotification()
        }
    }

    /**
     * Sets the desired interval for the alarm
     *
     * @param timerLengthSelection, interval timerLengthSelection value.
     */
    fun setTimeSelected(timerLengthSelection: Int) {
        Timber.tag(TAG).d("setTimeSelected got timerLengthSelection $timerLengthSelection")
        _timeSelection.value = timerLengthSelection
    }

    /**
     * Creates a new alarm, notification and timer
     */
    private fun startTimer(timerLengthSelection: Int) {
        Timber.tag(TAG).d("startTimer...alarmOn.value is ${_alarmOn.value}")
        _alarmOn.value?.let {
            if (!it) {
                // block runs when _alarmOn is false (can't start if already started)
                // now set alarmOn

                _alarmOn.value = true
                // val selectedInterval = timerLengthOptions[timerLengthSelection] * minute
                val selectedInterval = 10 * second
                Timber.tag(TAG).d("startTimer: We set selectedInterval $selectedInterval")

                val triggerTime = SystemClock.elapsedRealtime() + selectedInterval

                Timber.tag(TAG).d("set Alarm with triggerTime $triggerTime")
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    notifyPendingIntent
                )

                viewModelScope.launch {
                    saveTime(triggerTime)
                }
            }
        }
        createTimer()
    }

    /**
     * Creates a new timer
     * This is the bit that updates the elapsed time counter we will display in the
     * UI as a countdown
     */
    private fun createTimer() {
        viewModelScope.launch {
            val triggerTime = loadTime()
            timer = object : CountDownTimer(triggerTime, second) {
                override fun onTick(millisUntilFinished: Long) {
                    _elapsedTime.value = triggerTime - SystemClock.elapsedRealtime()
                    if (_elapsedTime.value!! <= 0) {
                        resetTimer()
                    }
                }

                override fun onFinish() {
                    resetTimer()
                }
            }
            timer.start()
        }
    }

    /**
     * Cancels the alarm, notification and resets the timer
     */
    private fun cancelNotification() {
        resetTimer()
        alarmManager.cancel(notifyPendingIntent)
    }

    /**
     * Resets the timer on screen and sets alarm value false
     */
    private fun resetTimer() {
        timer.cancel()
        _elapsedTime.value = 0
        _alarmOn.value = false
    }

    private suspend fun saveTime(triggerTime: Long) =
        withContext(Dispatchers.IO) {
            prefs.edit().putLong(triggerAtTime, triggerTime).apply()
        }

    private suspend fun loadTime(): Long =
        withContext(Dispatchers.IO) {
            prefs.getLong(triggerAtTime, 0)
        }
}