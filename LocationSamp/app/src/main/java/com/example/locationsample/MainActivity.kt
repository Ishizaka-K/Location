package com.example.locationsample

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startUpdatingLocation()
    }

    override fun onStart() {
        super.onStart()
        // 権限のリクエスト（権限がなかったら）
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), 0)
        }

        lifecycleScope.launch {
            getLastKnownLocation()
        }
    }

    private suspend fun getLastKnownLocation() {
        try {
            val lastLocation = fusedLocationClient.awaitLastLocation()
            findViewById<TextView>(R.id.textView).text = lastLocation.asString(Location.FORMAT_MINUTES)
        } catch (e: Exception) {
            findViewById<TextView>(R.id.textView).text = "Unable to get location."
            Log.d(TAG, "Unable to get location", e)
        }
    }

    private fun startUpdatingLocation() {
        fusedLocationClient.locationFlow()
            .conflate()
            .catch { e ->
                findViewById<TextView>(R.id.textView).text = "Unable to get location."
                Log.d(TAG, "Unable to get location", e)
            }
            .asLiveData()
            .observe(this, Observer { location ->
                if (location != null) {
                    findViewById<TextView>(R.id.textView).text = location.asString(Location.FORMAT_MINUTES)
                } else {
                    findViewById<TextView>(R.id.textView).text = "Location unknown"
                }
                Log.d(TAG, location.toString())
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recreate()
        }
    }
}

const val TAG = "KTXCODELAB"
