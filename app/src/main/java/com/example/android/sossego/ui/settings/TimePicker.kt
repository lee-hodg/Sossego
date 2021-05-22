 package com.example.android.sossego.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.text.format.DateFormat.is24HourFormat
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import androidx.preference.PreferenceManager.*
import androidx.preference.PreferenceViewHolder
import com.example.android.sossego.MainActivity
import com.example.android.sossego.R
import com.example.android.sossego.databinding.TimePickerRowBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


// A custom preference to show a time picker
class TimePickerPreference(context: Context?, attrs: AttributeSet?) : Preference(context, attrs),
    View.OnClickListener {
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var currentHour: String
    private lateinit var currentMinute: String
    private lateinit var binding: TimePickerRowBinding

    @RequiresApi(Build.VERSION_CODES.O)
    val formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        sharedPrefs = getDefaultSharedPreferences(context)
        currentHour = sharedPrefs.getString("reminder_hour", "9").toString()
        currentMinute = sharedPrefs.getString("reminder_minute", "0").toString()
        super.onBindViewHolder(holder)
        binding = TimePickerRowBinding.bind(holder.itemView)

        // Format the time correctly
        val currentTime = LocalTime.of(currentHour.toInt(), currentMinute.toInt())

        binding.timePickerDescription.text =
            String.format(
                context.getString(R.string.notification_hour_description),
                "~${formatter.format(currentTime)}"
            )

        binding.root.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View) {
        val act = context as MainActivity
        val reminderHourKey = act.getString(R.string.reminder_hour)
        val reminderMinuteKey = act.getString(R.string.reminder_minute)

        currentHour = sharedPrefs.getString(reminderHourKey, "9").toString()
        currentMinute = sharedPrefs.getString(reminderMinuteKey, "0").toString()

        // Show the time picker
        val isSystem24Hour = is24HourFormat(context)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(currentHour.toInt())
                .setMinute(currentMinute.toInt())
                .setTitleText(context.getString(R.string.notification_hour_name))
                .build()

        picker.addOnPositiveButtonClickListener {
            val editor = sharedPrefs.edit()
            editor.putString(reminderHourKey, "${picker.hour}")
            editor.putString(reminderMinuteKey, "${picker.minute}")
            editor.apply()

            // Format the selected hour and update the text
            val currentTime = LocalTime.of(picker.hour, picker.minute)

            binding.timePickerDescription.text =
                String.format(
                    context.getString(R.string.notification_hour_description),
                    "~${formatter.format(currentTime)}"
                )
        }

        picker.show(act.supportFragmentManager, "timepicker")
    }

}