package net.osmand.osmandapidemo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import net.osmand.osmandapidemo.HudCanvasViews.HudCanvasArrived
import net.osmand.osmandapidemo.HudCanvasViews.HudCanvasRoute
import net.osmand.osmandapidemo.HudCanvasViews.HudCanvasWelcome

class HudCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr){

    private val pageWelcome: HudCanvasWelcome = HudCanvasWelcome()
    private val pageRoute: HudCanvasRoute = HudCanvasRoute(context, this)
    private val pageArrived: HudCanvasArrived = HudCanvasArrived(context)
    private var currentPage: Page = pageWelcome

    var navigationData = HashMap<Any, Any>()
    var isLandscape = false
    var isCanvasRotated = false
    var isCanvasFlipped = false
    var isVolumeOn = true

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            drawColor(Color.BLACK)

            var distance_left = "NaN"
            navigationData["time_distance_left"]?.toString()?.let {time_distance_left->
                distance_left = time_distance_left
            }

            if (navigationData.size < 10){
                switchToWelcome()
            } else if (distance_left == "0m" ) {
                switchToArrived()
            } else {
                switchToRoute()
            }

            //------------------------------------------------
            // used for DEBUGGING
            //------------------------------------------------
            //switchToRoute()
            //navigationData["speedLimit"]="10"
            //navigationData["speed"]="30"
            /*
            switchToWelcome()
            switchToRoute()
            switchToArrived()
            rotateScreen = true
            flipScreen = true
            switchToRoute()
            navigationData["maxheight"] = "3.9"
            navigationData["maxwidth"] = "2.3"
            navigationData["maxweight"] = "20"
            navigationData["eta"] = "20:00"
            navigationData["cameraWarning"]="true"
            navigationData["cameraSpeedLimit"]="30"
            navigationData["eta"] = "20:00"
            navigationData["time_distance_left"] = "300m"
            navigationData["next_turn_distance"]="180m"
            navigationData["speedLimit"]="30"
            navigationData["speed"]="30"
            navigationData["next_turn_type"] = "RNDB4";
            navigationData["next_turn_angle"] = "-100";
             */
            //------------------------------------------------

            if (isCanvasFlipped || isCanvasRotated) canvas.save()

            if (isCanvasFlipped) {
                canvas.scale(1f, -1f, width / 2f, height / 2f)
            }
            if (isCanvasRotated) {
                var angle = -90f
                isLandscape = true
                canvas.rotate(angle, width / 2f, height / 2f)
                canvas.translate(0 - width / 2f, height / 4f)
            } else {
                isLandscape = false
            }

            currentPage.onDraw(canvas, navigationData, isLandscape, isVolumeOn)

            if (isCanvasFlipped || isCanvasRotated) canvas.restore()
        }
    }

    fun setOrientation(isCanvasRotated:Boolean, isCanvasFlipped:Boolean, isVolumeOn: Boolean){
        this.isCanvasRotated = isCanvasRotated
        this.isCanvasFlipped = isCanvasFlipped
        this.isVolumeOn = isVolumeOn
    }

    fun triggerInvalidation() {
        invalidate()
    }

    fun switchToWelcome() {
        currentPage = pageWelcome
        invalidate()
    }

    fun switchToRoute() {
        currentPage = pageRoute
        invalidate()
    }

    fun switchToArrived() {
        currentPage = pageArrived
        invalidate()
    }

    fun setData(data: HashMap<Any, Any>?) {
        if (data != null) {
            navigationData = data
        }
        invalidate()
    }
}

// Page interface
interface Page {
    fun onDraw(canvas: Canvas, data: HashMap<Any, Any>?, isLandscape:Boolean, isVolumeOn: Boolean)
}
