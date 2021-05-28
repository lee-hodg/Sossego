package com.example.android.sossego.customviews

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.example.android.sossego.R
import timber.log.Timber
import java.lang.Long.getLong
import kotlin.math.cos
import kotlin.math.sin



class TimerClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object{
        private const val TAG = "TimerClockView"
    }

    private var radius = 0.0f                   // Radius of the circle.
    // position variable which will be used to draw label and indicator circle position
    private val startTickPosition: PointF = PointF(0.0f, 0.0f)
    private val endTickPosition: PointF = PointF(0.0f, 0.0f)
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

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.RED
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    init {
        isClickable = false

    }

//    override fun performClick(): Boolean {
//        if (super.performClick()) return true
//
//        fanSpeed = fanSpeed.next()
//        contentDescription = resources.getString(fanSpeed.label)
//
//        invalidate()
//        return true
//    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        // circle radius will be 80% of smallest height or width dimension (half)
        // called also first time app launched and this is when radius computed depending on
        // width/height of view
        radius = (kotlin.math.min(width, height) / 2.0 * 0.8).toFloat()
    }

//    private fun PointF.computeXYForTick(pos: Int, radius: Float) {
//        // Angles are in radians.
//        val angle = (pos.toFloat()/totalMinutes.toFloat()) * (2*Math.PI)
//        x = (radius * cos(angle)).toFloat() + width / 2
//        y = (radius * sin(angle)).toFloat() + height / 2
//        Timber.tag(TAG).d("computed angle $angle, x: $x, y: $y for pos $pos and radius $radius")
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw the dial.
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)


        Timber.tag(TAG).d("Got elapsedSeconds $elapsedSeconds and selectedInt $selectedInterval")

        if(elapsedSeconds != 0L && selectedInterval != 0L) {
            val elapsedSecondsTemp = elapsedSeconds/1000
            // Draw inner shrinking circle
            val innerRadius = (elapsedSeconds.toFloat()/selectedInterval.toFloat()) * radius
            Timber.tag(TAG).d("draw w/ innerRadius $innerRadius")

            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), innerRadius, tickPaint)

            val timeText =
                if (elapsedSecondsTemp < 60) elapsedSecondsTemp.toString() else DateUtils.formatElapsedTime(
                    elapsedSecondsTemp
                )
            Timber.tag(TAG).d("Got elapsedSecondsTemp $elapsedSecondsTemp so set timeText $timeText")
            canvas.drawText(timeText, (width / 2).toFloat(), (height / 2).toFloat(), textPaint)
        }

        // Draw the ticks.
//        val tickStartRadius = (0.80 * radius).toFloat()
//        val tickEndRadius = radius
//
//        for (i in 1..totalMinutes) {
//            startTickPosition.computeXYForTick(i, tickStartRadius)
//            endTickPosition.computeXYForTick(i, tickEndRadius)
//            Timber.tag(TAG).d("$i Draw tick with start (x: ${startTickPosition.x}, y: ${startTickPosition.y}) and end (x: ${endTickPosition.x} , y: ${endTickPosition.y})")
//            canvas.drawLine(startTickPosition.x, startTickPosition.y, endTickPosition.x,
//                endTickPosition.y, tickPaint)
//        }
    }
}
