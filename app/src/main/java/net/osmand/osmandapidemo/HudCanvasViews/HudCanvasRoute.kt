package net.osmand.osmandapidemo.HudCanvasViews

import android.R
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import net.osmand.osmandapidemo.HudCanvasView
import net.osmand.osmandapidemo.HudPainter
import net.osmand.osmandapidemo.Page
import kotlin.math.cos
import kotlin.math.sin

class HudCanvasRoute(private val context:Context, private val parentView: HudCanvasView): Page {

    private val paintText = Paint().apply {
        color = Color.argb(200, 100, 250, 250) // Set paint color to semi transparent red
        textSize = 100f // Increase text size to 60
        //style = Paint.Style.STROKE // Set paint style to STROKE
    }

    private val paintIcon = Paint().apply {
        color = Color.argb(255, 100, 250, 250) // Set paint color to semi transparent red
        textSize = 70f // Increase text size to 60
        strokeWidth = 100f // Reduce stroke width
        style = Paint.Style.STROKE // Set paint style to STROKE
        //strokeCap = Paint.Cap.ROUND // Set paint cap to ROUND
        //strokeJoin = Paint.Join.ROUND // Set paint join to ROUND
        pathEffect = CornerPathEffect(.2f) // Add rounded corners with a radius of 10f
    }

    var navigationData = HashMap<Any, Any>()

    var warning = "NaN"
    var warningAnimation = HudCircleAnimation(parentView)


    var iconSize = 250f

    var arrowLength = 170f
    var arrowHead = 70f

    override fun onDraw(canvas: Canvas, data: HashMap<Any, Any>?, isLandscape: Boolean, isVolumeOn: Boolean) {

        navigationData = data!!

        var posLatLon = PointF(250f, 200f)
        val posEta = if (isLandscape) PointF(250f, 250f) else PointF(220f, 400f)
        val posTimeDistanceLeft = if (isLandscape) PointF(600f, 250f) else PointF(600f, 400f)
        val posSpeedLimit = if (isLandscape) PointF(250f, 600f) else PointF(200f, 750f)
        val posSpeedCamera = if (isLandscape) PointF(280f, 850f) else PointF(230f, 990f)
        val posMaxLimits = if (isLandscape) PointF(200f, 970f) else PointF(150f, 1100f)
        val posSpeed = if (isLandscape) PointF(600f, 600f) else PointF(600f, 750f)
        val posTurnType = if (isLandscape) PointF(1550f, 550f) else PointF(550f, 1600f)

        canvas.apply {
            warning = "NaN"

            navigationData["cameraWarning"]?.toString()?.let { speedCameraWarning ->
                if (speedCameraWarning == "true") {
                    warning = "cameraWarning"
                    navigationData["cameraSpeedLimit"]?.toString()?.let { cameraSpeedLimit ->
                        drawSpeedCamera(cameraSpeedLimit, posSpeedCamera)
                    } ?: drawSpeedCamera("NaN", posSpeedCamera)
                }
            }

            navigationData["speed"]?.toString()?.let { speed->
                if (speed != "NaN") {
                    val strSpeed = speed + "km/h"
                    drawText(strSpeed, posSpeed.x, posSpeed.y, paintText)
                }
            }

            navigationData["speedLimit"]?.toString()?.let {speedLimit ->
                if (speedLimit != "NaN") drawSpeedLimit( speedLimit, posSpeedLimit)
            }

            navigationData["speedLimit"]?.toString()?.let {speedLimit ->
                navigationData["speed"]?.toString()?.let {speed ->
                    if (speed != "NaN" && speedLimit != "NaN" ){
                        if (speed.toFloat() >= speedLimit.toFloat()*1.1f) {
                            if (warning != "speedWarning" ){
                                warning = "speedWarning"
                                navigationData["speedWarning"] = "true"
                            }
                        } else navigationData["speedWarning"] = "false"
                    }
                }
            }

            val maxheight = navigationData["maxheight"]?.toString()
            val maxwidth = navigationData["maxwidth"]?.toString()
            val maxweight = navigationData["maxweight"]?.toString()

            if (maxheight != "NaN" || maxwidth != "NaN" || maxweight != "NaN") {
                val paint = Paint(paintText)
                paint.textSize = 80f
                drawText("(w:${maxwidth} h:${maxheight} m:${maxweight})", posMaxLimits.x, posMaxLimits.y, paint)
            }

            navigationData["eta"]?.toString()?.let {eta ->
                drawEta(eta, posEta)
            }

            navigationData["time_distance_left"]?.toString()?.let {time_distance_left->
                drawText(time_distance_left, posTimeDistanceLeft.x, posTimeDistanceLeft.y, paintText)
            }

            navigationData["next_turn_distance"]?.toString()?.let {distance->
                drawTurnDistance(distance,posTurnType,iconSize)

                val pattern = "(\\d+)([km|m])".toRegex()
                val matchResult = pattern.matchEntire(distance)
                val number = matchResult?.groupValues?.get(1) ?: ""
                val unit = matchResult?.groupValues?.get(2) ?: ""
                if (unit=="m" && number.toInt() < 100){
                    warning = "turnWarning"
                }
            }

            navigationData["next_turn_type"]?.toString()?.let { nextTurnType ->
                navigationData["next_turn_angle"]?.toString()?.toFloat()?.let {
                    var nextTurnAngle = it
                    if (nextTurnAngle < 0) nextTurnAngle = 180 - nextTurnAngle
                    else nextTurnAngle = 180 - nextTurnAngle

                    //drawDebug(navigationData["next_turn_type"].toString(),navigationData["next_turn_angle"].toString().toFloat(), posTurnType,iconSize)
                    when (nextTurnType) {
                        // continue (go straight)
                        "C" -> {
                            drawStraightForward(posTurnType, iconSize, arrowLength, arrowHead)
                        }
                        // turn left
                        "TL" -> {
                            drawTurnLeftRight(
                                nextTurnType,
                                nextTurnAngle,
                                posTurnType,
                                iconSize,
                                arrowLength,
                                arrowHead
                            )
                        }
                        // turn slightly left
                        "TSLL" -> {
                            drawTurnSlightLeftRight(
                                nextTurnType,
                                nextTurnAngle,
                                posTurnType,
                                iconSize,
                                arrowLength,
                                arrowHead
                            )
                        }
                        // turn sharply left
                        "TSHL" -> {
                            drawDebug(navigationData["next_turn_type"].toString(),navigationData["next_turn_angle"].toString().toFloat(), posTurnType,iconSize)
                            // TODO
                        }
                        // turn right
                        "TR" -> {
                            drawTurnLeftRight(
                                nextTurnType,
                                nextTurnAngle,
                                posTurnType,
                                iconSize,
                                arrowLength,
                                arrowHead
                            )
                        }
                        // turn slightly right
                        "TSLR" -> {
                            drawTurnSlightLeftRight(
                                nextTurnType,
                                nextTurnAngle,
                                posTurnType,
                                iconSize,
                                arrowLength,
                                arrowHead
                            )
                        }
                        // turn sharply right
                        "TSHR" -> {
                            drawDebug(navigationData["next_turn_type"].toString(),navigationData["next_turn_angle"].toString().toFloat(), posTurnType,iconSize)
                            // TODO
                        }
                        // keep left
                        "KL" -> {
                            drawDebug(navigationData["next_turn_type"].toString(),navigationData["next_turn_angle"].toString().toFloat(), posTurnType,iconSize)
                            // TODO
                        }
                        // keep right
                        "KR" -> {
                            drawDebug(navigationData["next_turn_type"].toString(),navigationData["next_turn_angle"].toString().toFloat(), posTurnType,iconSize)
                            //drawStraightForward(posTurnType, iconSize, arrowLength, arrowHead);
                        }
                        // U-turn
                        "TU" -> {
                            drawTurnLeftRight(
                                nextTurnType,
                                nextTurnAngle,
                                posTurnType,
                                iconSize,
                                arrowLength,
                                arrowHead
                            )
                        }
                        // U-turn (right)
                        "TRU" -> {
                            drawDebug(navigationData["next_turn_type"].toString(),navigationData["next_turn_angle"].toString().toFloat(), posTurnType,iconSize)
                            // TODO
                        }
                        // Off route
                        "OFFR" -> {
                            drawDebug(navigationData["next_turn_type"].toString(),navigationData["next_turn_angle"].toString().toFloat(), posTurnType,iconSize)
                            // TODO
                        }
                    }

                    var exit = 0
                    // Roundabout (left)
                    if (nextTurnType.startsWith("RNDB")) {
                        if (nextTurnType.last().isDigit()) {
                            exit = nextTurnType.substring(nextTurnType.length - 1).toInt()
                        }
                            drawRoundabout(
                                posTurnType,
                                exit,
                                nextTurnAngle,
                                iconSize,
                                arrowLength,
                                arrowHead
                            )
                    }
                    // Roundabout (right)
                    if (nextTurnType.startsWith("RNLB")) {
                        // TODO
                    }
                }
            }

            if (warning != "NaN"){
                warningAnimation.start(isVolumeOn)
                warningAnimation.drawWarning(canvas, isLandscape, warning)
                warning="NaN"
            }
        }
    }

    private fun Canvas.drawLatLon(value:String, center:PointF){
        val paint = Paint(paintText)
        paint.textSize = 60f
        drawText(value, center.x, center.y, paint)
    }

    private fun Canvas.drawEta(eta:String, center:PointF){
        val iconDrawable = context.resources.getDrawable(R.drawable.ic_menu_myplaces, null)
        val drawableWidth = iconDrawable?.intrinsicWidth ?: 0
        val drawableHeight = iconDrawable?.intrinsicHeight ?: 0

        // Define the position where you want the drawable to be drawn
        val drawableX = center.x-120f
        val drawableY = center.y-80f

        // Set bounds for the drawable based on its position and size
        val drawableLeft = drawableX.toInt()
        val drawableTop = drawableY.toInt()
        val drawableRight = drawableLeft + drawableWidth
        val drawableBottom = drawableTop + drawableHeight
        iconDrawable?.setBounds(drawableLeft, drawableTop, drawableRight, drawableBottom)
        iconDrawable?.draw(this)

        drawText(eta, center.x, center.y, paintText)
    }

    private fun Canvas.drawTurnDistance(type:String, center: PointF, size:Float){
        val paint = TextPaint(paintText)
        paint.textAlign = Paint.Align.CENTER
        val f = 1.6f
        val debugText = type
        drawText(debugText,center.x, center.y+size*f+100f,paint)
    }

    private fun Canvas.drawSpeedCamera(speedLimit:String, center: PointF){
        val iconDrawable = context.resources.getDrawable(R.drawable.ic_menu_camera, null)
        val drawableWidth = iconDrawable?.intrinsicWidth ?: 0
        val drawableHeight = iconDrawable?.intrinsicHeight ?: 0

        // Define the position where you want the drawable to be drawn
        val drawableX = center.x-100f
        val drawableY = center.y-75f

        // Set bounds for the drawable based on its position and size
        val drawableLeft = drawableX.toInt()
        val drawableTop = drawableY.toInt()
        val drawableRight = drawableLeft + drawableWidth
        val drawableBottom = drawableTop + drawableHeight
        iconDrawable?.setBounds(drawableLeft, drawableTop, drawableRight, drawableBottom)
        iconDrawable?.alpha = 200
        iconDrawable?.draw(this)

        val paint = Paint(paintText)
        paint.textSize = 100f
        paint.color = Color.RED
        drawText(speedLimit, center.x+20f, center.y+5, paint)
    }

    private fun Canvas.drawSpeedLimit(speedLimit:String, center: PointF){
        if (speedLimit=="NaN" || speedLimit == "") return

        val offset = PointF(0f,0f)
        offset.x = center.x + 55f
        offset.y = center.y - 33

        val radius = 185f
        var paint = Paint(paintText)
        paint.color = Color.DKGRAY
        drawCircle(offset.x, offset.y, radius, paint)
        paint.color = Color.BLACK
        drawCircle(offset.x, offset.y, radius-5f, paint)

        paint = Paint(paintIcon)
        paint.strokeWidth = 40f
        paint.color = Color.RED
        drawCircle(offset.x, offset.y, radius-35f, paint)

        paint = Paint(paintText)
        paint.color = Color.WHITE
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 125f
        drawText(speedLimit, offset.x-5f, center.y+10f, paint)
    }

    private fun Canvas.drawTurnLeftRight(type:String, direction:Float, center: PointF, size:Float, arrowLength:Float, arrowHead: Float){
        save()
        translate(center.x, center.y)

        rotate(270-direction)


        val painter = HudPainter()

        // set Arrow Head
        painter.add(PointF(0f-size - arrowLength + arrowHead/2, 0f - arrowHead/2),"moveTo")
        painter.add(PointF(0f-size - arrowLength, 0f),"lineTo")
        painter.add(PointF(0f-size - arrowLength + arrowHead, 0f - arrowHead),"lineTo")
        painter.add(PointF(0f-size - arrowLength, 0f),"lineTo")
        painter.add(PointF(0f-size - arrowLength + arrowHead, 0f + arrowHead),"lineTo")
        painter.add(PointF(0f-size-arrowLength, 0f),"lineTo")

        painter.add(PointF(0f, 0f),"lineTo")


        var angle:Float
        if (type =="TU"){
            val point : PointF
            point = painter.commands.last()["point"] as PointF
            if (direction <180) angle = -90f+direction
            else angle = 90f+direction

            val endPoint = PointF(point.x+size* cos(Math.toRadians(angle.toDouble())).toFloat(), point.y+size* sin(Math.toRadians(angle.toDouble())).toFloat())
            painter.add(PointF(endPoint.x, endPoint.y),"lineTo")
            if (direction <180) angle -= 90f
            else angle +=90f
        } else {
            angle = 180f+direction
        }

        val point : PointF
        point = painter.commands.last()["point"] as PointF

        val endPoint = PointF(point.x+size* cos(Math.toRadians(angle.toDouble())).toFloat(), point.y+size* sin(Math.toRadians(angle.toDouble())).toFloat())
        painter.add(PointF(endPoint.x, endPoint.y),"lineTo")

        val path = painter.getPath()
        drawPath(path, paintIcon)
        painter.clear()
        restore()
    }

    private fun Canvas.drawTurnSlightLeftRight(type:String, direction:Float, center: PointF, size:Float, arrowLength:Float, arrowHead: Float){
        save()

        translate(center.x, center.y)
        rotate(90f+180f-direction)

        val painter = HudPainter()

        // set Arrow Head
        painter.add(PointF(0f-size - arrowLength + arrowHead/2, 0f - arrowHead/2),"moveTo")
        painter.add(PointF(0f-size - arrowLength, 0f),"lineTo")
        painter.add(PointF(0f-size - arrowLength + arrowHead, 0f - arrowHead),"lineTo")
        painter.add(PointF(0f-size - arrowLength, 0f),"lineTo")
        painter.add(PointF(0f-size - arrowLength + arrowHead, 0f + arrowHead),"lineTo")
        painter.add(PointF(0f-size-arrowLength, 0f),"lineTo")

        painter.add(PointF(0f, 0f),"lineTo")

        val point : PointF
        point = painter.commands.last()["point"] as PointF

        var angle = 180-direction

        var endPoint = PointF(point.x+size* cos(Math.toRadians(angle.toDouble())).toFloat(), point.y+size* sin(Math.toRadians(angle.toDouble())).toFloat())
        painter.add(PointF(endPoint.x, endPoint.y),"lineTo")

        angle = 180-direction
        endPoint = PointF(arrowLength+size* cos(Math.toRadians(angle.toDouble())).toFloat(), arrowLength* sin(Math.toRadians(angle.toDouble())).toFloat())
        painter.add(PointF(endPoint.x, endPoint.y),"lineTo")

        //painter.add(PointF(0f+size + arrowLength, endPoint.y),"lineTo")

        val path = painter.getPath()
        drawPath(path, paintIcon)
        painter.clear()
        restore()
    }

    private fun Canvas.drawStraightForward(center: PointF, size:Float, arrowLength:Float, arrowHead: Float){
        save()
        translate(center.x, center.y)
        rotate(90f)

        val painter = HudPainter()

        // set Arrow Head
        painter.add(PointF(0f-size - arrowLength + arrowHead/2, 0f - arrowHead/2),"moveTo")
        painter.add(PointF(0f-size - arrowLength, 0f),"lineTo")
        painter.add(PointF(0f-size - arrowLength + arrowHead, 0f - arrowHead),"lineTo")
        painter.add(PointF(0f-size - arrowLength, 0f),"lineTo")
        painter.add(PointF(0f-size - arrowLength + arrowHead, 0f + arrowHead),"lineTo")
        painter.add(PointF(0f-size-arrowLength, 0f),"lineTo")

        painter.add(PointF(0f+size + arrowLength, 0f),"lineTo")

        val path = painter.getPath()
        drawPath(path, paintIcon)
        painter.clear()
        restore()
    }

    private fun Canvas.drawRoundabout(center: PointF, exit :Int, direction:Float, size:Float, arrowLength:Float, arrowHead: Float){
        save()
        translate(center.x, center.y)
        var paint = Paint(paintText)
        paint.textSize = 200f
        drawText(exit.toString(), -60f, 70f, paint)
        rotate(270-direction)

        var f = 0.9f
        paint = Paint(paintIcon)
        paint.color = Color.LTGRAY
        paint.strokeWidth = 20f
        drawCircle(0f, 0f, (size*f).toFloat(), paint)

        var painter = HudPainter()
        painter = setCircle(painter, exit, direction, size*f, arrowLength, arrowHead)

        var path = painter.getPath()
        drawPath(path, paintIcon)
        painter.clear()
        restore()
    }

    private fun setCircle(painter: HudPainter, exit:Int, direction:Float, size:Float, arrowLength: Float, arrowHead: Float): HudPainter {

        val center = PointF(0f, 0f)
        val numPoints = (direction/10).toInt()
        val alpha = (direction/numPoints).toFloat()
        var angle = 180f
        val ticks = (direction/exit)
        val f = 1.3f

        // set Arrow Head
        painter.add(PointF(0f-size - arrowLength*f + arrowHead/2, 0f - arrowHead/2),"moveTo")
        painter.add(PointF(0f-size - arrowLength*f, 0f),"lineTo")
        painter.add(PointF(0f-size - arrowLength*f + arrowHead, 0f - arrowHead),"lineTo")
        painter.add(PointF(0f-size - arrowLength*f, 0f),"lineTo")
        painter.add(PointF(0f-size - arrowLength*f + arrowHead, 0f + arrowHead),"lineTo")
        painter.add(PointF(0f-size-arrowLength*f, 0f),"lineTo")

        // set Circle
        //val points = mutableListOf<PointF>()
        repeat(numPoints) {
            val x = center.x + size * cos(Math.toRadians(angle.toDouble())).toFloat()
            val y = center.y + size * sin(Math.toRadians(angle.toDouble())).toFloat()
            painter.add(PointF(x, y),"lineTo")

            val indicator = (angle-180f) % ticks
            if (indicator < 10f && (angle-180f) > 0f){
                val point = PointF(x+arrowLength/2* cos(Math.toRadians(angle.toDouble())).toFloat(), y+arrowLength/2* sin(Math.toRadians(angle.toDouble())).toFloat())
                painter.add(point, "moveTo" )
                painter.add(PointF(x, y),"lineTo")
            }

            angle += alpha
        }

        // set Arrow Base
        val point : PointF
        point = painter.commands.last()["point"] as PointF

        val endPoint = PointF(point.x+arrowLength* cos(Math.toRadians(angle.toDouble())).toFloat(), point.y+arrowLength* sin(Math.toRadians(angle.toDouble())).toFloat())
        painter.add(PointF(endPoint.x, endPoint.y),"lineTo")

        return painter
    }

    private fun Canvas.drawDebug(type:String, direction:Float, center: PointF, size:Float){
        val paint = TextPaint(paintText)
        paint.textAlign = Paint.Align.CENTER
        paint.style = Paint.Style.STROKE
        paint.color = Color.GRAY

        val f = 1.6f
        drawRect(center.x-size*f, center.y-size*f, center.x+size*f, center.y+size*f, paint)

        val debugText = type + " " + direction.toInt().toString()
        paint.style = Paint.Style.FILL
        paint.textSize = 60f
        drawText(debugText,center.x, center.y-size*f-20f,paint)
    }


}