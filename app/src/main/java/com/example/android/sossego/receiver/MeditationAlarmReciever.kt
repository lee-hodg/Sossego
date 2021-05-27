package com.example.android.sossego.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber


class MeditationAlarmReceiver: BroadcastReceiver() {

    companion object {
        private const val TAG = "MeditationAlarmRec"
    }

    override fun onReceive(context: Context, intent: Intent) {

        Timber.tag(TAG).d("Meditation over")
    }

}