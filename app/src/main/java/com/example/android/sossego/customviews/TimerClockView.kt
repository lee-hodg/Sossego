package com.example.android.sossego.customviews

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.example.android.sossego.R
import timber.log.Timber


class TimerClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object{
        private const val TAG = "TimerClockView"
    }

    private var radius = 0.0f                   // Radius of the circle.


    var remainingTimeMilliseconds: Long = 0L
        set(value) {
            field = value
            invalidate()
        }

    var fractionRemaining: Float = 1.0f
        set(value) {
            field = value
            invalidate()
        }

    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.customViewBackground,
        context.theme)

    private val waterColor = ResourcesCompat.getColor(resources, R.color.primaryColor,
        context.theme)

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    private val donutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = waterColor
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


        if(fractionRemaining == 0.0f || fractionRemaining == 1.0f){
            Timber.tag(TAG).d("fractionRemaining $fractionRemaining clear canvas")
            canvas.drawColor(backgroundColor)
        }else {
            val remainingTimeSeconds = remainingTimeMilliseconds/1000
            // Draw the donut.
            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), outerRadius, donutPaint)
            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, circlePaint)
            val sweepAngle = 360.0f * (1-fractionRemaining)
            Timber.tag(TAG).d("fractionRemaining $fractionRemaining, remainingTimeMilliseconds $remainingTimeMilliseconds: draw donut w/ sweepAngle $sweepAngle ")
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
                if (remainingTimeSeconds < 60) remainingTimeSeconds.toString() else DateUtils.formatElapsedTime(
                    remainingTimeSeconds
                )
            Timber.tag(TAG).d("From remainingTimeSeconds $remainingTimeSeconds so set timeText $timeText")
            canvas.drawText(timeText, (width / 2).toFloat(), (height / 2).toFloat(), textPaint)
            }
    }

}

