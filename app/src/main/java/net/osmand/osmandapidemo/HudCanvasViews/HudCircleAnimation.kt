package net.osmand.osmandapidemo.HudCanvasViews

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import net.osmand.osmandapidemo.HudCanvasView
import kotlin.math.min

class HudCircleAnimation( private val parentView: HudCanvasView) {
    private var circleAnimator: ValueAnimator? = null
    private var circleRadius = 0f
    private val handler = Handler(Looper.getMainLooper())
    private var isAnimationDelayed = false

    init {
        circleAnimator = ValueAnimator.ofFloat(0f, 3f).apply {
            duration = 1600
            repeatCount = 0
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { animation ->
                circleRadius = animation.animatedValue as Float
                parentView.triggerInvalidation()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {
                }

                override fun onAnimationEnd(p0: Animator) {
                    isAnimationDelayed = true
                    handler.postDelayed({
                        isAnimationDelayed = false
                    }, 4000)
                }

                override fun onAnimationCancel(p0: Animator) {
                    TODO("Not yet implemented")
                }

                override fun onAnimationRepeat(p0: Animator) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    fun start(voice:Boolean) {
        if (circleAnimator?.isRunning == true || isAnimationDelayed) {
            return
        }
        if (voice) ToneGenerator(AudioManager.STREAM_MUSIC, 50).startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
        circleAnimator?.start()
    }

    fun drawWarning(canvas:Canvas, isLandscape:Boolean, warning:String){
        var canvasWidth = if (isLandscape) canvas.height else canvas.width
        var canvasHeight = if (isLandscape)  canvas.width else canvas.height
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f
        val radius = min(centerX, centerY) * circleRadius
        val paint = Paint().apply {
            color = Color.argb(150, 255, 255, 255) // Set paint color to semi transparent red
            strokeWidth = 100f // Reduce stroke width
            style = Paint.Style.STROKE // Set paint style to STROKE
            strokeCap = Paint.Cap.ROUND // Set paint cap to ROUND
            pathEffect = CornerPathEffect(.2f) // Add rounded corners with a radius of 10f
        }
        if (warning=="speedWarning") paint.color = Color.argb(150, 255, 0, 0)
        if (warning=="cameraWarning") paint.color = Color.argb(150, 255, 255, 0)
        canvas.drawCircle(centerX, centerY, radius, paint)
    }
}