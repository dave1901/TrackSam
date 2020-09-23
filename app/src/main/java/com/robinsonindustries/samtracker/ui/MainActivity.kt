package com.robinsonindustries.samtracker.ui

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.android.gms.maps.GoogleMap
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.robinsonindustries.samtracker.R
import com.robinsonindustries.samtracker.other.Constants.ACTION_START_SERVICE
import com.robinsonindustries.samtracker.other.Constants.ACTION_STOP_SERVICE
import com.robinsonindustries.samtracker.service.TrackingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.FOREGROUND_SERVICE) {
                bStart.isEnabled = true
            }
        } else {
            runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION) {
                bStart.isEnabled = true
            }
        }

        bStart.setOnClickListener {
            sendCommandToService(ACTION_START_SERVICE)
        }

        bStop.setOnClickListener {
            sendCommandToService(ACTION_STOP_SERVICE)
        }

        subscribeToObservers()

    }

    private fun subscribeToObservers() {

        TrackingService.isTracking.observe(this, Observer {
            if (it) {
                bStart.isEnabled = false
                bStop.isEnabled = true
            } else {
                bStart.isEnabled = true
                bStop.isEnabled = false
            }

        })

        TrackingService.mostRecentLocation.observe(this, Observer {

            val lastLocation = it.lastLocation

            tvUTCTime.text = lastLocation.time.toString()
            tvLat.text = lastLocation.latitude.toString()
            tvLong.text = lastLocation.longitude.toString()

            val altNum = "%.3f".format(lastLocation.altitude)
            tvAlt.text = altNum.toString()

            val speedNum = "%.3f".format(lastLocation.speed)
            tvSpeed.text = speedNum

        })
    }

    private fun sendCommandToService(action: String) {
        Intent(this, TrackingService::class.java).also {
            it.action = action
            startService(it)
        }
    }


}