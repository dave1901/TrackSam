package com.robinsonindustries.samtracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import android.util.Log.d
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.robinsonindustries.samtracker.other.Constants.ACTION_START_SERVICE
import com.robinsonindustries.samtracker.other.Constants.ACTION_STOP_SERVICE
import com.robinsonindustries.samtracker.other.Constants.FASTEST_LOCATION_INTERVAL
import com.robinsonindustries.samtracker.other.Constants.LOCATION_UPDATE_INTERVAL
import com.robinsonindustries.samtracker.other.Constants.NOTIFICATION_CHANNEL_ID
import com.robinsonindustries.samtracker.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.robinsonindustries.samtracker.other.Constants.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService: LifecycleService() {

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var queue:RequestQueue

    companion object {
        val mostRecentLocation = MutableLiveData<LocationResult>()
        val isTracking = MutableLiveData<Boolean>()

    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = FusedLocationProviderClient(this)
        queue = Volley.newRequestQueue(this)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action) {
                ACTION_START_SERVICE -> startForegroundService()
                ACTION_STOP_SERVICE -> killService()
            }


        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {

        isTracking.postValue(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())


        startLocationTracking()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationTracking() {

        val request = LocationRequest().apply {
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = FASTEST_LOCATION_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {

            if (result == null) return

            mostRecentLocation.postValue(result)

            val time = result.lastLocation?.time
            val lat = result.lastLocation?.latitude
            val long = result.lastLocation?.longitude
            val alt = result.lastLocation?.altitude
            val speed = result.lastLocation?.speed

            d("dan", "New Location Received")
            d("dan", "UTC Time: ${time}")
            d("dan", "Lat: ${lat}")
            d("dan", "Long: ${long}")
            d("dan", "Alt: ${alt}")
            d("dan", "Speed: ${speed}")


            sendToResultToServer(time!!, lat!!, long!!, alt!!, speed!!)



            super.onLocationResult(result)
        }
    }

    private fun sendToResultToServer(time:Long, lat:Double, long:Double, alt:Double, speed:Float) {

        val samBaseURL = "https://track-sam.sjfdean.com/3vQnc55cwWp412AcLmqM.php?Fi6OGvNOszmkkOLP4ZxT=lvPCjmSMFqS3kDnCITbg"

        val finalUrl = "$samBaseURL&time=$time&latitude=$lat&longitude=$long&altitude=$alt&speed=$speed"

        d("dan", finalUrl)

        val stringRequest = StringRequest(
            Request.Method.POST, finalUrl,
            { response ->
                d("dan", "Response is: ${response}")
            },
            {
                d("dan", it.toString())
            })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun killService() {
        isTracking.postValue(false)
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }


}