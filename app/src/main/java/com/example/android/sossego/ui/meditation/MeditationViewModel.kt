package com.example.android.sossego.ui.meditation

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.floor
import com.example.android.sossego.R

private const val MINUTE: Long = 60_000L
private const val SECOND: Long = 1_000L
private const val triggerAtTime = "TRIGGER_AT"
private const val timeSelectionKey = "SELECTED_TIME"
private const val alarmStateKey = "ALARM_STATE"

class MeditationTimerViewModel(val app: Application) : AndroidViewModel(app) {

    companion object{
        private const val TAG = "MediationViewModel"
    }


    private val prefs = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    private lateinit var timer: CountDownTimer

    /*
     *      Variables to do with alarm and countdown timer
     */
    // Is the alarm set to on/off
    private var _alarmOn = MutableLiveData(false)
    val isAlarmOn: LiveData<Boolean>
        get() = _alarmOn

    // How much time is remaining in milliseconds
    private val _remainingTimeMilliseconds = MutableLiveData(MINUTE)
    val remainingTimeMilliseconds: LiveData<Long>
        get() = _remainingTimeMilliseconds

    val timeSelection = MutableLiveData(1)

    // The interval in milliseconds (a simple transformation of the minutes count selected)
    private val intervalMilliseconds = Transformations.map(timeSelection) { it * MINUTE }

    private val _fractionRemaining = MediatorLiveData<Float>()
    val fractionRemaining: LiveData<Float>
        get() = _fractionRemaining

    /**
     * Play the meditation interval sounds
     */
//    private var mMediaPlayer: MediaPlayer? = null

    private lateinit var audioAttributes: AudioAttributes

    private var soundPool: SoundPool

    private var bellSound: Int


    private fun playSound() {
        soundPool.play(
            bellSound, 1F, 1F, 0, 0, 1F
        )

//        if (mMediaPlayer == null) {
//            mMediaPlayer = MediaPlayer.create(app.applicationContext, R.raw.bell)
//            mMediaPlayer!!.isLooping = false
//            mMediaPlayer!!.start()
//        } else mMediaPlayer!!.start()
    }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(2)
                .build()
        } else {
            soundPool = SoundPool(
                3,
                AudioManager.STREAM_MUSIC,
                0)
        }

        bellSound = soundPool.load(
            app.applicationContext,
            R.raw.bell,
            1
        )

        // Add the sources for the fraction remaining mediator live data
        _fractionRemaining.addSource(intervalMilliseconds) { computeFractionRemaining() }
        _fractionRemaining.addSource(_remainingTimeMilliseconds) { computeFractionRemaining() }
        viewModelScope.launch {
            createOrResumeTimer()
        }

    }

    private fun onFinishedTimer(){
        // Alarm is off so make sure we cancel the pendingIntent
        Timber.tag(TAG).d("onFinishedTimer: cancel timer")
        timer.cancel()
        _remainingTimeMilliseconds.value = 0
        _alarmOn.value = false

        viewModelScope.launch {
            // if we navigate away and then back saving these let us resume
            saveTriggerTime(-1L)
            saveAlarmState(false)
        }
    }

    /*
    Allows a change in alarm state from the UI
     */
    fun toggleAlarm() {
        Timber.tag(TAG).d("toggleAlarm called: w/ _alarmOn.value ${_alarmOn.value}")
        when(_alarmOn.value) {
            true-> {
                onFinishedTimer()
            }
            else-> {
                // Alarm was off now set it to on
                _alarmOn.value = true
                val triggerTime = computeTriggerTime()
                // Save alarm state, trigger time and selected time interval
                // so be can resume timer when minimized or nav away and back etc
                viewModelScope.launch {
                    // if we navigate away and then back saving these let us resume
                    saveAlarmState(true)
                    saveTriggerTime(triggerTime)
                    timeSelection.value?.let { it -> savedTimeSelection(it) }

                    createOrResumeTimer()

                }

            }}
    }

    /**
     * Use the intervalMilliseconds to compute the computeTriggerTime for the counter
     */
    private fun computeTriggerTime(): Long {
        val triggerTime = SystemClock.elapsedRealtime() + intervalMilliseconds.value!!
        Timber.tag(TAG).d("computeTriggerTime: We have _intervalMilliseconds ${intervalMilliseconds.value} and timeSelection ${timeSelection.value}")
        Timber.tag(TAG).d("computeTriggerTime: computed triggerTime $triggerTime")
        return triggerTime
    }

    /**
     * Creates a new timer
     * This is the bit that updates the elapsed time counter we will display in the
     * UI as a countdown
     */
    private suspend fun createOrResumeTimer() {

            val alarmState = loadAlarmState()
            _alarmOn.value = alarmState

            val triggerTime = loadTriggerTime()
            val timeSelectionValue = loadTimeSelection().toFloat()
            timeSelection.value = timeSelectionValue.toInt()

            // Check if something to resume
            if(triggerTime == -1L || !alarmState ){
                Timber.tag(TAG).d("Got -1L triggerTime or false alarmState. No timer to resume. Return")
                return
            }

            Timber.tag(TAG).d("createTimer loaded time $triggerTime and timeSelection $timeSelectionValue")
            timer = object : CountDownTimer(triggerTime, SECOND) {
                override fun onTick(millisUntilFinished: Long) {
                    _remainingTimeMilliseconds.value = triggerTime - SystemClock.elapsedRealtime()

                    // Play a sound at each minute
                    val remainingTimeVal = _remainingTimeMilliseconds.value
                    if(remainingTimeVal != null) {
                        val modVal = floor((remainingTimeVal.toFloat() / 1000.0f) % 60.0f)
                        // Each minute play bell
                        Timber.tag(TAG).d("Computed modVal $modVal from $remainingTimeVal")
                        if(modVal == 0.0f) {
                            Timber.tag(TAG).d("Hit that cymbal")
                            playSound()
                        }
                    }

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

    /**
     * Functions to do w/ save/load of state (e.g. so we can continue upon nav away
     * or minimize app)
     */
    private suspend fun saveTriggerTime(triggerTime: Long) =
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("saveTriggerTime w/ triggerTime $triggerTime")
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
            prefs.getLong(triggerAtTime, -1L)
        }

    private suspend fun loadTimeSelection(): Int =
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("loadTimeSelection")
            prefs.getInt(timeSelectionKey, 1)
        }


    private suspend fun saveAlarmState(alarmState: Boolean) =
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("saveAlarmState w/ alarmState $alarmState")
            prefs.edit().putBoolean(alarmStateKey, alarmState).apply()

        }

    private suspend fun loadAlarmState(): Boolean =
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("loadAlarmState")
            prefs.getBoolean(alarmStateKey, false)
        }
}