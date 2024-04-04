package net.osmand.osmandapidemo.HudCanvasViews

import android.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.ContextCompat
import net.osmand.osmandapidemo.HudCanvasView
import net.osmand.osmandapidemo.Page

class HudCanvasArrived(private val context:Context,) : Page {
    private val paintText = Paint().apply {
        color = Color.argb(200, 100, 250, 250) // Set paint color to semi transparent red
        textSize = 100f // Increase text size to 60
        //style = Paint.Style.STROKE // Set paint style to STROKE
    }

    var navigationData = HashMap<Any, Any>()


    override fun onDraw(canvas: Canvas, data: HashMap<Any, Any>?, isLandscape: Boolean, isVolumeOn: Boolean) {
        navigationData = data!!

        canvas.drawArrived(isLandscape)
    }

    private fun Canvas.drawArrived( isLandscape: Boolean){

        val canvasWidth = if (isLandscape) height.toFloat() else width.toFloat()

        var pos = if (isLandscape) PointF(60f, 550f) else PointF(50f, 900f)

        var f = 3
        var iconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_menu_myplaces)
        val drawableWidth = iconDrawable?.intrinsicWidth!!*f ?: 0
        val drawableHeight = iconDrawable?.intrinsicHeight!!*f ?: 0

        // Define the position where you want the drawable to be drawn
        val drawableX = pos.x + 50f //canvasWidth/2-drawableWidth/2
        val drawableY = pos.y-300f

        // Set bounds for the drawable based on its position and size
        val drawableLeft = drawableX.toInt()
        val drawableTop = drawableY.toInt()
        val drawableRight = drawableLeft + drawableWidth
        val drawableBottom = drawableTop + drawableHeight
        iconDrawable?.setBounds(drawableLeft, drawableTop, drawableRight, drawableBottom)
        iconDrawable?.draw(this)

        var paint = TextPaint(paintText)
        paint.color = Color.WHITE
        paint.textSize = 80f
        pos.x += 100;
        drawTextWithLayout("Congratulations", paint, pos, canvasWidth.toInt())
        paint.textSize = 60f
        pos.y += 130
        drawTextWithLayout("You have arrived at your selected target. Thank you for using OsmAnd HUD. Have a nice stay.", paint, pos, canvasWidth.toInt()-300)
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