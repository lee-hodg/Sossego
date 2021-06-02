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
    private val selectedIntervalKey = "SELECTED_INTERVAL"

    private val minute: Long = 60_000L
    private val second: Long = 1_000L

    private val timerLengthOptions: IntArray
    private val notifyPendingIntent: PendingIntent

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val prefs = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    private val notifyIntent = Intent(app, MeditationAlarmReceiver::class.java)

    private val _timeSelection = MutableLiveData<Int>()
    private val timeSelection: LiveData<Int>
        get() = _timeSelection

    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long>
        get() = _elapsedTime

    private val _fractionRemaining = MutableLiveData<Float>()
    val fractionRemaining: LiveData<Float>
        get() = _fractionRemaining

    private var _alarmOn = MutableLiveData<Boolean>()
    val isAlarmOn: LiveData<Boolean>
        get() = _alarmOn


    private val _selectedInterval = MutableLiveData<Long>()
    val selectedInterval: LiveData<Long>
        get() = _selectedInterval



    private lateinit var timer: CountDownTimer

    init {
        if(fractionRemaining.value === null) {
            Timber.tag(TAG).d("Init set _fractionRemaining to 1")
            _fractionRemaining.value = 1.0f
        }
        if(_selectedInterval.value === null) {
            Timber.tag(TAG).d("Init set _selectedInterval to 1")
            _selectedInterval.value = 1 * minute
        }
        if(_timeSelection.value === null) {
            Timber.tag(TAG).d("Init set _timeSelection to 1")
            _timeSelection.value = 1
        }
        if(_elapsedTime.value === null) {
            Timber.tag(TAG).d("Init set _elapsedTime to 0")
            _elapsedTime.value = 0L
        }

        // determine if alarm state is on/off based on if there is a pending intent
        _alarmOn.value = PendingIntent.getBroadcast(
            app,
            requestCode,
            notifyIntent,
            PendingIntent.FLAG_NO_CREATE
        ) != null

        Timber.tag(TAG).d("In init we determined that _alarmOn has value ${_alarmOn.value}")

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
            Timber.tag(TAG).d("In init we launch with createTimer()...")
            createTimer()
        }

    }

    fun toggleAlarm() {
        Timber.tag(TAG).d("toggleAlarm called")
        when (_alarmOn.value) {
            true -> {
                // Switch to off if already on
                Timber.tag(TAG).d("turn alarmOn to false and cancelNotifications")
                cancelNotification()
                _alarmOn.value = false
            }
            false -> {
                Timber.tag(TAG).d("startTimer with timeSelection ${timeSelection.value}")
                // switch to on if off
                timeSelection.value?.let { startTimer(it) }
            }
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
//                _selectedInterval.value = timerLengthOptions[timerLengthSelection] * minute
                _selectedInterval.value = timerLengthSelection * minute
                //_selectedInterval.value = 10 * second
                Timber.tag(TAG).d("startTimer: We set selectedInterval ${_selectedInterval.value}")

                val triggerTime = SystemClock.elapsedRealtime() + _selectedInterval.value!!

                Timber.tag(TAG).d("set Alarm with triggerTime $triggerTime")
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    notifyPendingIntent
                )

                viewModelScope.launch {
                    // if we navigate away and then back saving these let us resume
                    saveTime(triggerTime)
                    _selectedInterval.value?.let { it1 -> savedSelectedInterval(it1) }
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
            val selectedIntervalValue = loadSelectedInterval().toFloat()
            Timber.tag(TAG).d("createTimer loaded time $triggerTime")
            timer = object : CountDownTimer(triggerTime, second) {
                override fun onTick(millisUntilFinished: Long) {
                    _elapsedTime.value = triggerTime - SystemClock.elapsedRealtime()
                    val elapsedTimeValue = _elapsedTime.value?.toFloat()
                    if(selectedIntervalValue != null && selectedIntervalValue!= 0.0f && elapsedTimeValue != null) {
                        _fractionRemaining.value = elapsedTimeValue/selectedIntervalValue
                    }

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
        _fractionRemaining.value = 0.0f
        Timber.tag(TAG).d("resetTimer called. Timer cancelled, _elapsedTime set to 0, _alarmOn false, _fractionRemaining 0")

    }

    private suspend fun saveTime(triggerTime: Long) =
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("saveTime w/ triggerTime $triggerTime")
            prefs.edit().putLong(triggerAtTime, triggerTime).apply()

        }

    private suspend fun savedSelectedInterval(selectedInterval: Long) =
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("savedSelectedInterval w/ selectedInterval $selectedInterval")
            prefs.edit().putLong(selectedIntervalKey, selectedInterval).apply()

        }

    private suspend fun loadTime(): Long =
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("loadTime")
            prefs.getLong(triggerAtTime, 0)
        }

    private suspend fun loadSelectedInterval(): Long =
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("loadSelectedInterval")
            prefs.getLong(selectedIntervalKey, 0)
        }
}