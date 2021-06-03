package com.example.android.sossego.ui.meditation

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.example.android.sossego.receiver.MeditationAlarmReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


private const val MINUTE: Long = 60_000L
private const val SECOND: Long = 1_000L
private const val requestCode = 1101
private const val triggerAtTime = "TRIGGER_AT"
private const val timeSelectionKey = "SELECTED_TIME"


class MeditationTimerViewModel(val app: Application) : AndroidViewModel(app) {

    companion object{
        private const val TAG = "MediationViewModel"
    }

    private var notifyPendingIntent: PendingIntent? = null

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val prefs = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    private val notifyIntent = Intent(app, MeditationAlarmReceiver::class.java)
    private lateinit var timer: CountDownTimer


    /*
     *      Variables to do with alarm and countdown timer
     */
    // Is the alarm set to on/off
    private var existingAlarm: Boolean = false
    private var _alarmOn = MutableLiveData<Boolean>()
    val isAlarmOn: LiveData<Boolean>
        get() = _alarmOn

    // How much time is remaining in milliseconds
    private val _remainingTimeMilliseconds = MutableLiveData(MINUTE)
    val remainingTimeMilliseconds: LiveData<Long>
        get() = _remainingTimeMilliseconds

    val timeSelection = MutableLiveData(1)

//    // How many minutes did the user select
//    val timeSelection: MutableLiveData<Int>
//        get() = _timeSelection

    // The interval in milliseconds (a simple transformation of the minutes count selected)
    private val intervalMilliseconds = Transformations.map(timeSelection) { it * MINUTE }


    private val _fractionRemaining = MediatorLiveData<Float>()
    val fractionRemaining: LiveData<Float>
        get() = _fractionRemaining


    /**
     * This method is responsible for dynamically computing the fraction remaining
     */
    private fun computeFractionRemaining(){
        if(_alarmOn.value == false){
            _fractionRemaining.value = 1.0f
        }else {
            val intervalMillisecondsVal = intervalMilliseconds.value?.toFloat()
            val remainingTimeMillisecondsVal = _remainingTimeMilliseconds.value?.toFloat()
            if (intervalMillisecondsVal != null && intervalMillisecondsVal != 0.0f && remainingTimeMillisecondsVal != null) {
                _fractionRemaining.value = remainingTimeMillisecondsVal / intervalMillisecondsVal
                Timber.tag(TAG).d("Computed fraction remaining as ${_fractionRemaining.value} using intervalMillisecondsVal $intervalMillisecondsVal and remainingTimeMillisecondsVal $remainingTimeMillisecondsVal")
            } else {
                _fractionRemaining.value = 1.0f
            }
        }
        Timber.tag(TAG).d("Computed fraction remaining as ${_fractionRemaining.value}")
    }

    init {

        // Add the sources for the fraction remaining mediator live data
        _fractionRemaining.addSource(intervalMilliseconds) { computeFractionRemaining() }
        _fractionRemaining.addSource(_remainingTimeMilliseconds) { computeFractionRemaining() }

        // Determine if alarm state is on/off based on if there is a pending intent
        // This doesn't create a PendingIntent (FLAG_NO_CREATE), it just checks if one exists
        // If one does not, getBroadcast returns null, and _alarmOn.value set to false
        // else if one does exist _alarmOn.value set to true
        // Let's say we navigate away and come back. The existence of the pending intent
        // tells us the alarm is on, then we can reload triggerTime and interval selected
        // to do setup again and act accordingly
        existingAlarm = PendingIntent.getBroadcast(
            app,
            requestCode,
            notifyIntent,
            PendingIntent.FLAG_NO_CREATE
        ) != null

        _alarmOn.value = existingAlarm

        if (existingAlarm) {
            // We don't create another PendingIntent/setAlarm/saveTimes because
            // they already exist, but we do call createTimer, which will
            // load any saved params and start the countdown which
            // continues the UI
            createTimer()
        } else {
            // So that when user returns we get same as before
            viewModelScope.launch {
                timeSelection.value= loadTimeSelection()
            }
        }

        Timber.tag(TAG).d("init: we determined that existingAlarm $existingAlarm")

    }

    private fun onFinishedTimer(){
        // Alarm is off so make sure we cancel the pendingIntent
        Timber.tag(TAG).d("cancel timer")
        timer.cancel()
        _remainingTimeMilliseconds.value = 0
        _alarmOn.value = false
    }

    private fun onAlarmOff(){
        // Get the PI and kill it if exists (use FLAG NO CREATE so we do not create if no there)
        val killPendingIntent = PendingIntent.getBroadcast(
            app,
            requestCode,
            notifyIntent,
            PendingIntent.FLAG_NO_CREATE
        )

        if(killPendingIntent != null) {
            Timber.tag(TAG).d("Killing pending intent")
            killPendingIntent.cancel()
            alarmManager.cancel(killPendingIntent)
        }else{
            Timber.tag(TAG).d("No pending intent found to kill")
        }

        Timber.tag(TAG).d("Set alarmOn to false")

        onFinishedTimer()
    }

    /*
    Allows a change in alarm state from the UI
     */
    fun toggleAlarm() {
        Timber.tag(TAG).d("toggleAlarm called: w/ _alarmOn.value ${_alarmOn.value}")
        when(_alarmOn.value) {
            true-> {
                onAlarmOff()
            }
            else-> {
                // Alarm was off
                setAlarmAndStartTimer()
            }}
    }

    /**
     * Creates a new alarm, notification and timer
     */
    private fun setAlarmAndStartTimer() {
        Timber.tag(TAG).d("startTimer..")

        _alarmOn.value = true

        notifyPendingIntent = PendingIntent.getBroadcast(
            app,
            requestCode,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val triggerTime = SystemClock.elapsedRealtime() + intervalMilliseconds.value!!
        Timber.tag(TAG).d("startTimer: We have _intervalMilliseconds ${intervalMilliseconds.value} and timeSelection ${timeSelection.value}")
        Timber.tag(TAG).d("set Alarm with triggerTime $triggerTime")

        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerTime,
            notifyPendingIntent!!
        )


        viewModelScope.launch {
            // if we navigate away and then back saving these let us resume
            saveTime(triggerTime)
            timeSelection.value?.let { it -> savedTimeSelection(it) }
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
            val triggerTime = loadTriggerTime()
            val timeSelectionValue = loadTimeSelection().toFloat()
            timeSelection.value = timeSelectionValue.toInt()

            Timber.tag(TAG).d("createTimer loaded time $triggerTime and timeSelection $timeSelectionValue")
            timer = object : CountDownTimer(triggerTime, SECOND) {
                override fun onTick(millisUntilFinished: Long) {
                    _remainingTimeMilliseconds.value = triggerTime - SystemClock.elapsedRealtime()
                    // If it's over already then resetTimer
                    if (_remainingTimeMilliseconds.value!! <= 0) {
                        Timber.tag(TAG).d("no remaining milliseconds (${_remainingTimeMilliseconds.value}): turn alarm off")
                        onFinishedTimer()
                    }
                }

                override fun onFinish() {
                    Timber.tag(TAG).d("timer finished: turn alarm off")
                    onFinishedTimer()
                }
            }
            timer.start()
        }
    }

    /**
     * Functions to do w/ save/load of state (e.g. so we can continue upon nav away
     * or minimize app)
     */
    private suspend fun saveTime(triggerTime: Long) =
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("saveTime w/ triggerTime $triggerTime")
            prefs.edit().putLong(triggerAtTime, triggerTime).apply()

        }

    private suspend fun savedTimeSelection(timeSelection: Int) =
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("savedTimeSelection w/ timeSelection $timeSelection")
            prefs.edit().putInt(timeSelectionKey, timeSelection).apply()

        }

    private suspend fun loadTriggerTime(): Long =
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("loadTriggerTime")
            prefs.getLong(triggerAtTime, 0)
        }

    private suspend fun loadTimeSelection(): Int =
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("loadTimeSelection")
            prefs.getInt(timeSelectionKey, 1)
        }
}