package com.example.android.sossego.customviews

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.example.android.sossego.R
import timber.log.Timber


private const val waveSurfaceHeight = 20

class WaterLevelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object{
        private const val TAG = "WaterLevelView"
    }

    private var waterHeight = 0.0f

    private val waterDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_wave2,
        context.theme)
    private val waterColor = ResourcesCompat.getColor(resources, R.color.water_blue,
        context.theme)

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap =
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private val destRect = Rect(0, 0, 0, 0)


    private var _fractionRemaining: Float = 0.0f
    var fractionRemaining: Float
        get() = _fractionRemaining
        set(value) {
            _fractionRemaining = value
            invalidate()
        }


    private val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = waterColor
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }


    init {
        // otherwise top controllers don't work
        isClickable = false

    }

    private fun shower() {

        val container = this.parent as ViewGroup
        val containerW = container.width
        val containerH = container.height


        var dropletW = 9.0f
        var dropletH = 12.0f


        for (n in 1..20) {

            val newDroplet = AppCompatImageView(this.context)

            newDroplet.setImageResource(R.drawable.ic_droplet)
            newDroplet.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )

            newDroplet.scaleX = Math.random().toFloat() * 1.5f + .1f
            newDroplet.scaleY = newDroplet.scaleX
            val tempDropletW = newDroplet.drawable.intrinsicWidth.toFloat()
            val tempDropletH = newDroplet.drawable.intrinsicHeight.toFloat()

            newDroplet.translationX = Math.random().toFloat() *
                    containerW - tempDropletW / 2
            newDroplet.translationY = -tempDropletH

            container.addView(newDroplet)

            val fader = ObjectAnimator.ofFloat(newDroplet, ALPHA, 1f, 0f)
            fader.interpolator = LinearInterpolator()

            val moverY = ObjectAnimator.ofFloat(
                newDroplet, TRANSLATION_Y,
                newDroplet.translationY, containerH - waterHeight + tempDropletH
            )
            moverY.interpolator = AccelerateInterpolator(1f)

            val set = AnimatorSet()
            set.playTogether(fader, moverY)
            set.duration = (Math.random() * 1500 + 500).toLong()

            set.addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationStart(animation: Animator?) {
//                   container.addView(newStar)
//                }

                override fun onAnimationEnd(animation: Animator?) {
                    container.removeView(newDroplet)
                }
            })
            set.startDelay = (n * 100L + Math.random() * 100L).toLong()
            set.start()
            //animations.add(set)
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        Timber.tag(TAG).d("Got fraction $fractionRemaining")

        if(fractionRemaining != 1.0f) {
                waterHeight = fractionRemaining * (this.height - waveSurfaceHeight)
                Timber.tag(TAG).d("draw w/ fractionRemaining $fractionRemaining and waterHeight $waterHeight")
                canvas.drawRect(0.0f, waterHeight, width.toFloat(), height.toFloat(),
                    waterPaint)

                val waterBitmap = waterDrawable?.let { drawableToBitmap(it) }
                if (waterBitmap != null) {
                    destRect.set(0, (waterHeight - waveSurfaceHeight).toInt(), width, (waterHeight+1).toInt())
                    Timber.tag(TAG).d("Draw bitmap with ${destRect.left} x ${destRect.top} and ${destRect.right}x${destRect.bottom}")
                    canvas.drawBitmap(waterBitmap, null, destRect, null)
                }

                shower()

        }else if(fractionRemaining == 0.0f){
            //clear
            canvas.drawColor(Color.WHITE)
        }


    }

}

