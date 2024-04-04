package net.osmand.osmandapidemo;

import android.graphics.Path
import android.graphics.PointF

class HudPainter {
    val commands: MutableList<Map<String, Any>> = ArrayList()

    public fun add(point: PointF, command:String){
        val paintCommand = HashMap<String, Any>().apply {
            this["point"] = point
            this["command"] = command
        }
        commands.add(paintCommand.toMap())
    }

    public fun getPath(): Path {
        var path = Path()

        commands.forEachIndexed { index, data ->
            var point : PointF;
            point = data["point"] as PointF
            if (data["command"] == "moveTo")
                path.moveTo(point.x, point.y)
            else
                path.lineTo(point.x, point.y)
        }
        return path
    }

    public fun clear(){
        commands.clear()
    }
}