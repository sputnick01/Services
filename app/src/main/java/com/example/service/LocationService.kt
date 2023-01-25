package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class LocationService : Service() {


    companion object {
        const val CHANNEL_ID = "12345"
        const val NOTIFICATION_ID = 12345
    }

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null


    private var notificationManager: NotificationManager? = null
    private var location: Location? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setIntervalMillis(5000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult)

            }
        }

        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, "location",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager?.createNotificationChannel(notificationChannel)

        }

    }


    @SuppressLint("MissingPermission")
    fun createLocationRequest() {
        try {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest!!,
                locationCallback!!,
                null
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private fun removeLocationUpdates() {
        locationCallback?.let { fusedLocationProviderClient?.removeLocationUpdates(it) }
        stopForeground(true)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onNewLocation(locationResult: LocationResult) {
        location = locationResult.lastLocation
        EventBus.getDefault().post(LocationEvent(location?.latitude, location?.longitude))
        startForeground(NOTIFICATION_ID, getNotification())
    }


//       Display data in the notification

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotification(): Notification {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Updates").setContentText(
                "Latitude-->${location?.latitude}\n longitude-->${location?.longitude}"
            )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(false)

        saveToFile("Latitude-->${location?.latitude}  longitude-->${location?.longitude}\t ${getTime()} \n")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.setChannelId(CHANNEL_ID)
        }

        return notification.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTime(): String {
        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val formatted = current.format(formatter)

        return formatted
    }


    private fun saveToFile(s: String) {
        try {
            val file: File = File(getExternalFilesDir("/")?.absolutePath + "/myLocation.txt")

            FileWriter(file, true).use {
                it.write(s)
                it.close()

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createLocationRequest()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeLocationUpdates()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}