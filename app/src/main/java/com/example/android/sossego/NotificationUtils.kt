package com.example.android.sossego

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.android.sossego.receiver.SnoozeReceiver


// Notification ID.
private const val NOTIFICATION_ID = 0
private const val REQUEST_CODE = 0
private const val FLAGS = 0


/**
 * Builds and delivers the notification.
 *
 */
fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {
    // Create the content intent for the notification, which launches
    // this activity
    val contentIntent = Intent(applicationContext, MainActivity::class.java)

    // Create PendingIntent
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    //  add style
//    val eggImage = BitmapFactory.decodeResource(
//        applicationContext.resources,
//        R.drawable.cooked_egg
//    )
//    val bigPicStyle = NotificationCompat.BigPictureStyle()
//        .bigPicture(eggImage)
//        .bigLargeIcon(null)


    // Add snooze action
    val snoozeIntent = Intent(applicationContext, SnoozeReceiver::class.java)
    val snoozePendingIntent: PendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        REQUEST_CODE,
        snoozeIntent,
        FLAGS
    )

    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.periodic_reminder_channel_id)
    )

        // Set title, text and icon to builder
        .setSmallIcon(R.drawable.outline_event_24)
        .setContentTitle(applicationContext.getString(R.string.periodic_reminder_title))
        .setContentText(messageBody)
        // Set content intent
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)

        // Add style to builder
//        .setStyle(bigPicStyle)
//        .setLargeIcon(eggImage)

        // Add snooze action
        .addAction(
            R.drawable.outline_snooze_24,
            applicationContext.getString(R.string.snooze),
            snoozePendingIntent
        )

        // Set priority
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    // Call notify
    notify(NOTIFICATION_ID, builder.build())
}

/**
 * Cancels all notifications.
 *
 */
fun NotificationManager.cancelNotifications() {
    cancelAll()
}
