package net.osmand.osmandapidemo;

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class LocationHelper(private val context: Context) {
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    private lateinit var locationCallback: LocationCallback

    fun startLocationUpdates(intervalMillis: Long = 2000, fastestIntervalMillis: Long = 2000, onLocationResult: (Location) -> Unit) {
        val locationRequest = LocationRequest.create().apply {
            interval = intervalMillis
            fastestInterval = fastestIntervalMillis
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let {
                    onLocationResult(it)
                }
            }
        }

        if (checkLocationPermission()) {
            ToneGenerator(AudioManager.STREAM_MUSIC, 10).startTone(ToneGenerator.TONE_CDMA_ANSWER, 200)
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            ToneGenerator(AudioManager.STREAM_MUSIC, 10).startTone(ToneGenerator.TONE_CDMA_ANSWER, 200)
            return false
        }
        return true
    }
/*
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                // You may want to check coarse location permission as well if your app requires it
                // This check is simplified here for demonstration purposes
                startLocationUpdates()
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

 */

    fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
