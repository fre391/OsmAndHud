package net.osmand.osmandapidemo

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class HudData(private val activity: AppCompatActivity) : OverpassAPIQueryCallback {
    var networkTaskBusy = false
    private val mutex = Mutex()
    val locationData = HashMap<Any, Any>()
    val navigationData = HashMap<Any, Any>()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun updateData(key:String, value:String ){
        when (key) {
            "eta" -> {
                val seconds = java.lang.Long.parseLong(value)
                val etaMillis = seconds * 1000
                val etaDate = Date(etaMillis)
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                navigationData[key] = formatter.format(etaDate)
            }
            "time_distance_left" -> {
                val distance = java.lang.Float.parseFloat(value)
                var value = ""
                if (distance > 1000.0f) {
                    value = String.format("%.1f", distance / 1000) + "km"
                } else if (distance > 100.0f) {
                    val hundredMeters = (distance / 100).toInt() * 100
                    value = String.format("%.0f", hundredMeters.toFloat()) + "m"
                } else {
                    val tenMeters = (distance / 1000).toInt() * 1000
                    value = String.format("%.0f", tenMeters.toFloat()) + "m"
                }
                navigationData[key] = value
            }
            "next_turn_distance" -> {
                val distance = java.lang.Float.parseFloat(value)
                var value = ""
                if (distance > 1000.0f) {
                    value = String.format("%.1f", distance / 1000) + "km"
                } else if (distance > 100.0f) {
                    val hundredMeters = (distance / 100).toInt() * 100
                    value = String.format("%.0f", hundredMeters.toFloat()) + "m"
                } else {
                    val tenMeters = (distance / 10).toInt() * 10
                    value = String.format("%.0f", tenMeters.toFloat()) + "m"
                }
                navigationData[key] = value
            }
            "next_turn_angle" -> {
                val value = java.lang.Float.parseFloat(value)
                navigationData[key] = String.format("%.0f", value)
            }
            "lat", "lon" -> {
                //var value = java.lang.Float.parseFloat(value.toString())
                //navigationData[key] = String.format("%.6f", value).replace(",",".")
            }
            else -> navigationData[key] = value
        }
    }

    fun updateLocation(lat:Double, lon:Double, s:Float)  {
        if (!networkTaskBusy) {
            networkTaskBusy = true

            val speed = s * 3.6f
            if (speed < 10) locationData["speed"] = "NaN"
            else locationData["speed"] = speed.toInt().toString()
            locationData["lat"] = lat.toFloat().toString()
            locationData["lon"] = lon.toFloat().toString()

            val overpassQuery = """
                    [out:json];
                    (
                        way(around:10, ${lat}, ${lon})["highway"];
                        node(around:500, ${lat}, ${lon})["highway"="speed_camera"];
                        );
                    out qt tags;
                """

            coroutineScope.launch {
                runBackgroundTask(overpassQuery)
            }
        } else {
            ToneGenerator(AudioManager.STREAM_MUSIC, 50).startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 200)
            Log.i("HudData", "networkTaskBusy")
        }
    }

    private suspend fun runBackgroundTask(overpassQuery: String) {
        withContext(Dispatchers.IO) {
            val networkTask = OverpassAPIQuery(activity, overpassQuery, this@HudData)
            networkTask.execute()
        }
    }

    override fun onTaskCompleted(result: String) {
        try {
            locationData["cameraWarning"] = "false"
            locationData["cameraSpeedLimit"] = "NaN"
            locationData["speedLimit"] = "NaN"
            locationData["maxheight"] = "NaN"
            locationData["maxwidth"] = "NaN"
            locationData["maxweight"] = "NaN"

            if (result == "-1") return
            val jsonObject = JSONObject(result)
            val elementsArray = jsonObject.getJSONArray("elements")

            for (i in 0 until elementsArray.length()) {
                val element = elementsArray.getJSONObject(i)
                val type = element.getString("type")
                val tags = element.getJSONObject("tags")

                if (type == "node") {
                    // speed camera
                    if (tags.has("highway") && tags.has("maxspeed") ) {
                        val maxspeed = tags.getString("maxspeed")
                        locationData["cameraWarning"] = "true"
                        locationData["cameraSpeedLimit"] = if (maxspeed != "null") maxspeed else "NaN"
                    }
                    if (tags.has("highway") && tags.has("maxheight") ) {
                        val maxheight = tags.getString("maxheight")
                        locationData["maxheight"] = if (maxheight != "null") maxheight else "NaN"
                    }
                    if (tags.has("highway") && tags.has("maxwidth") ) {
                        val maxwidth = tags.getString("maxwidth")
                        locationData["maxwidth"] = if (maxwidth != "null") maxwidth else "NaN"
                    }
                    if (tags.has("highway") && tags.has("maxweight") ) {
                        val maxweight = tags.getString("maxweight")
                        locationData["maxweight"] = if (maxweight != "null") maxweight else "NaN"
                    }
                } else {
                    // speed limit
                    if (tags.has("highway") && tags.has("maxspeed") ) {
                        val maxspeed = tags.getString("maxspeed")
                        locationData["speedLimit"] = maxspeed
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("HudData", "Error parsing result: ${e.message}")
        } finally {
            networkTaskBusy = false
        }
    }

    override fun onTaskFailed(error: String) {
        Log.e("HudData", "Network task failed: $error")
        networkTaskBusy = false
    }

    //@JvmName("getNavigationData1")
    fun getData(): HashMap<Any, Any> {
        val data = HashMap<Any, Any>()
        data.putAll(locationData)
        data.putAll(navigationData)
        return data
    }
}

