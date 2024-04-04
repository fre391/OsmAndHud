package net.osmand.osmandapidemo

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import java.util.concurrent.*
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.core.content.ContextCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.SocketTimeoutException

interface OverpassAPIQueryCallback {
    fun onTaskCompleted(result: String)
    fun onTaskFailed(error: String)
}

class OverpassAPIQuery(
    private val context: Context,
    private val query: String,
    private val callback: OverpassAPIQueryCallback
) {
    var networkTaskBusy = false

    private val PERMISSION_REQUEST_CODE = 123
    private val client = OkHttpClient()
    private val TIMEOUT_LIMIT: Long = 5000
    private val TIMEOUT_ERROR_MESSAGE = "Timeout"

    fun execute() {
        if (networkTaskBusy) {
            Log.i("OverpassAPIQueryCallback", "networkTaskBusy")
            return
        }
        val executor = ThreadPoolExecutor(
            1, 1, TIMEOUT_LIMIT, TimeUnit.MILLISECONDS, LinkedBlockingQueue()
        )
        val future = executor.submit(Callable<String> {
            doInBackground()
        })

        try {
            val result = future.get(TIMEOUT_LIMIT, TimeUnit.MILLISECONDS)
            onPostExecute(result)
        } catch (e: TimeoutException) {
            Log.i("OverpassAPIQueryCallback", "timeoutException")
            onPostExecute("-1") // Timeout occurred
        } catch (e: Exception) {
            Log.i("OverpassAPIQueryCallback", "generalException")
            onPostExecute("-1") // Other exceptions
        } finally {
            executor.shutdownNow()
            networkTaskBusy = false
        }
    }

    private fun doInBackground(): String {
        if (!isPermissionGranted()) {
            return "-1"
        }

        if (!isNetworkAvailable()) {
            callback.onTaskFailed("No internet connection")
            return "-1"
        }

        val request = Request.Builder()
            .url("https://overpass-api.de/api/interpreter?data=${query.replace(" ", "%20")}")
            .build()

        try {
            val response: okhttp3.Response
            try {
                response = client.newCall(request).execute()
            } catch (e: SocketTimeoutException) {
                callback.onTaskFailed("Timeout")
                return "-1"
            }

            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: ${response.code}")
            }
            return response.body?.string() ?: "-1"
        } catch (e: IOException) {
            e.printStackTrace()
            return "-1"
        }
    }

    private fun onPostExecute(result: String) {
        if (result.isNotEmpty()) {
            callback.onTaskCompleted(result)
        } else {
            callback.onTaskFailed(TIMEOUT_ERROR_MESSAGE)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities =
            connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_CELLULAR
        ))
    }

    private fun isPermissionGranted(): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_NETWORK_STATE
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }
}