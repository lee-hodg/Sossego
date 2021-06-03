package com.example.android.sossego.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import com.example.android.sossego.R
import timber.log.Timber


/**
 * Don't forget to add to manifest
 */
class MeditationAlarmReceiver: BroadcastReceiver() {

    companion object {
        private const val TAG = "MeditationAlarmRec"
    }

    private var mMediaPlayer: MediaPlayer? = null

    private fun playSound(context: Context) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(context, R.raw.tibetan_bell)
            mMediaPlayer!!.isLooping = false
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }


//    private fun playRingtone(context: Context) {
//        try {
//            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//            val r = RingtoneManager.getRingtone(context, notification)
//            r.play()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    override fun onReceive(context: Context, intent: Intent) {

        Timber.tag(TAG).d("Meditation over")
        // playRingtone(context)
        playSound(context)

    }

}