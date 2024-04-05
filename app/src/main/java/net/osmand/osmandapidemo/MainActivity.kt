package net.osmand.osmandapidemo;

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import main.java.net.osmand.osmandapidemo.dialogs.*
import net.osmand.aidlapi.navigation.ADirectionInfo

class MainActivity : AppCompatActivity(), OsmAndHelper.OnOsmandMissingListener {

    companion object {
        private const val TAG = "MainActivity"
        const val REQUEST_OSMAND_API = 1001
        const val KEY_NAV_INFO_LISTENER = "on_nav_info_update"
        }

    var mOsmAndHelper: OsmAndHelper? = null
    private var mAidlHelper: OsmAndAidlHelper? = null
    private val callbackKeys = mutableMapOf<String, Long>()
    private var isOsmandInitialzed = false
    private var isOsmandInProcess = false

    private val coroutineScopeCustom1 = CoroutineScope(newSingleThreadContext("CustomThread1"))
    private val coroutineScopeCustom2 = CoroutineScope(newSingleThreadContext("CustomThread2"))

    private lateinit var locationHelper: LocationHelper
    private var hudData: HudData? = null
    private var hudCanvasView: HudCanvasView? = null

    private var isCanvasRotated = false
    private var isCanvasFlipped = false
    private var isVolumeOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isOsmandInitialzed) {
            mOsmAndHelper = OsmAndHelper(this, REQUEST_OSMAND_API, this)
            mAidlHelper = OsmAndAidlHelper(this.application, this)
            mAidlHelper!!.setOsmandInitializedListener {
                Toast.makeText(this, "OsmAnd HUD now initialized.", Toast.LENGTH_SHORT).show()
                isOsmandInitialzed = true
            }
        }

        locationHelper = LocationHelper(this)
        requestLocationUpdates()

        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)


        val sharedPreferences: SharedPreferences = this.getSharedPreferences("test", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        isCanvasRotated = sharedPreferences.getBoolean("isCanvasRotated", false)
        isCanvasFlipped = sharedPreferences.getBoolean("isCanvasFlipped", false)
        isVolumeOn = sharedPreferences.getBoolean("isVolumeOn", true)

        hudData = HudData(this)
        hudCanvasView = findViewById(R.id.hudCanvasView)
        hudCanvasView?.setOrientation(isCanvasRotated, isCanvasFlipped, isVolumeOn)

        val switchOrientationButton: ImageButton = findViewById(R.id.switchOrientationButton)
        switchOrientationButton.setOnClickListener {
            isCanvasRotated = !isCanvasRotated
            editor.putBoolean("isCanvasRotated", isCanvasRotated)
            editor.apply()
            hudCanvasView?.setOrientation(isCanvasRotated, isCanvasFlipped, isVolumeOn)
        }

        val switchFlipButton: ImageButton = findViewById(R.id.switchFlipButton)
        switchFlipButton.setOnClickListener {
            isCanvasFlipped = !isCanvasFlipped
            editor.putBoolean("isCanvasFlipped", isCanvasFlipped)
            editor.apply()
            hudCanvasView?.setOrientation(isCanvasRotated, isCanvasFlipped, isVolumeOn)
        }

        val volumeOnButton: ImageButton =  findViewById(R.id.volumeOnButton)
        volumeOnButton.setImageResource(if (isVolumeOn) R.drawable.volume_on else R.drawable.volume_off);
        volumeOnButton.setOnClickListener {
            isVolumeOn = !isVolumeOn
            volumeOnButton.setImageResource(if (isVolumeOn) R.drawable.volume_on else R.drawable.volume_off);
            editor.putBoolean("isVolumeOn", isVolumeOn)
            editor.apply()
            hudCanvasView?.setOrientation(isCanvasRotated, isCanvasFlipped, isVolumeOn)
        }
    }

    private fun requestLocationUpdates() {
        locationHelper.startLocationUpdates { location ->
            //ToneGenerator(AudioManager.STREAM_MUSIC, 50).startTone(ToneGenerator.TONE_PROP_BEEP, 200)
            Log.i("mf", "Location: ${location.latitude}, ${location.longitude}")
            Log.i("mf", "Speed: ${location.speed*3.6f}")
            coroutineScopeCustom1.launch {
                hudData?.updateLocationData(location.latitude, location.longitude, location.speed)
                if (isOsmandInitialzed && !isOsmandInProcess) {
                    isOsmandInProcess = true
                    mOsmAndHelper!!.getInfo()
                }
            }
            hudCanvasView?.setData(hudData?.getData())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        requestLocationUpdates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_OSMAND_API) {
            if (data != null) {
                val extras = data.extras
                if (extras != null && extras.size() > 0) {
                    coroutineScopeCustom2.launch {
                        for (key in extras.keySet()) {
                            val value = extras.get(key)
                            hudData?.updateNavigationData(key, value.toString())
                        }
                    }
                }
            }
            isOsmandInProcess = false
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        coroutineScopeCustom1.cancel()
        coroutineScopeCustom2.cancel()
        mAidlHelper!!.cleanupResources()
        locationHelper.stopLocationUpdates()
        super.onDestroy()
    }

    override fun osmandMissing() {
        OsmAndMissingDialogFragment().show(supportFragmentManager, null)
    }

}