package com.example.android.sossego.customviews

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import timber.log.Timber

private const val DISPLAY_THRESHOLD = 0.0001f

class TimerClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object{
        private const val TAG = "TimerClockView"
    }

    private var radius = 0.0f                   // Radius of the circle.

    // Override the setters so that we invalidate the view and call onDraw if the live data
    // changed
    private var _elapsedSeconds: Long = 0L
    var elapsedSeconds: Long
        get() = _elapsedSeconds
        set(value) {
            _elapsedSeconds = value
            invalidate()
        }
    private var _selectedInterval: Long = 0L
    var selectedInterval: Long
        get() = _selectedInterval
        set(value) {
            _selectedInterval = value
            invalidate()
        }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    private val donutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLUE
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.LTGRAY
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    init {
        // otherwise top controllers don't work
        isClickable = false

    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        // circle radius will be 80% of smallest height or width dimension (half)
        // called also first time app launched and this is when radius computed depending on
        // width/height of view
        radius = (kotlin.math.min(width, height) / 2.0 * 0.8).toFloat()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val arcThickness = radius * 0.10f
        val outerRadius = radius + arcThickness

        Timber.tag(TAG).d("Got elapsedSeconds $elapsedSeconds and selectedInterval $selectedInterval")

        if(elapsedSeconds != 0L && selectedInterval != 0L) {
            val elapsedSecondsTemp = elapsedSeconds/1000
            // fraction of the interval elapsed determines the sweepAngle (degrees)
            val fractionComplete = 1.0f - (elapsedSeconds.toFloat()/selectedInterval.toFloat())
            Timber.tag(TAG).d("fractionComplete is $fractionComplete")
            if(fractionComplete == 0.0f){
                canvas.drawColor(Color.WHITE)
            }else {
                // Draw the donut.
                canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), outerRadius, donutPaint)
                canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, circlePaint)

                val sweepAngle = 360.0f * fractionComplete
                Timber.tag(TAG)
                    .d("draw w/ fractionComplete $fractionComplete and sweepAngle $sweepAngle")
                canvas.drawArc(
                    (width / 2).toFloat() - outerRadius,
                    (height / 2).toFloat() - outerRadius,
                    (width / 2).toFloat() + outerRadius,
                    (height / 2).toFloat() + outerRadius,
                    -90.0f, -sweepAngle, true, textPaint
                )

                canvas.drawCircle(
                    (width / 2).toFloat(),
                    (height / 2).toFloat(),
                    radius,
                    circlePaint
                )

                // Draw timer text at center
                val timeText =
                    if (elapsedSecondsTemp < 60) elapsedSecondsTemp.toString() else DateUtils.formatElapsedTime(
                        elapsedSecondsTemp
                    )
                Timber.tag(TAG)
                    .d("Got elapsedSecondsTemp $elapsedSecondsTemp so set timeText $timeText")
                canvas.drawText(timeText, (width / 2).toFloat(), (height / 2).toFloat(), textPaint)
            }
        }

    }
}
