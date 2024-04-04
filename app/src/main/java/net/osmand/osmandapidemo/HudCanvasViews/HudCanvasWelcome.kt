package net.osmand.osmandapidemo.HudCanvasViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import net.osmand.osmandapidemo.Page

class HudCanvasWelcome() : Page {
    private val paintText = Paint().apply {
        color = Color.argb(200, 100, 250, 250) // Set paint color to semi transparent red
        textSize = 100f // Increase text size to 60
        //style = Paint.Style.STROKE // Set paint style to STROKE
    }

    var navigationData = HashMap<Any, Any>()

    override fun onDraw(canvas: Canvas, data: HashMap<Any, Any>?, isLandscape: Boolean, isVolumeOn: Boolean) {
        navigationData = data!!
        canvas?.apply {
            drawWelcome(isLandscape)
        }
    }

    private fun Canvas.drawWelcome(isLandscape: Boolean){
        var paint = TextPaint(paintText)
        paint.textSize =150f

        val canvasWidth = if (isLandscape) height.toFloat() else width.toFloat()

        var pos = if (isLandscape) PointF(120f, 200f) else PointF(100f, 500f)
        drawText("OsmAnd HUD", pos.x, pos.y,paint)

        paint.textSize = 80f
        paint.color = Color.WHITE

        pos = if (isLandscape) PointF(120f, 300f) else PointF(100f, 600f)
        drawTextWithLayout("Head Up Display for OsmAnd", paint, pos, canvasWidth.toInt())

        pos = if (isLandscape) PointF(120f, 550f) else PointF(100f, 950f)
        drawTextWithLayout("You need to install and start navigation with OsmAnd to use this application...", paint, pos, canvasWidth.toInt()-300)

        paint = TextPaint(paintText)
        paint.textSize = 40f
        pos = if (isLandscape) PointF(120f, 850f) else PointF(100f, 2000f)
        drawTextWithLayout("written 2024 by markus.freyt@gmail.com", paint, pos, canvasWidth.toInt()-50)
    }

    fun Canvas.drawTextWithLayout(
        text: String,
        paint: TextPaint,
        position: PointF,
        pageWidth: Int,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    ) {
        val textLayout = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            paint,
            (pageWidth - position.x).toInt()
        )
            .setAlignment(alignment)
            .build()

        save()
        translate(position.x, position.y)
        textLayout.draw(this)
        restore()
    }
}