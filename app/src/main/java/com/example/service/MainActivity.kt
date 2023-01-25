package com.example.service

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class MainActivity : AppCompatActivity() {

    private var service: Intent? = null
    private var locationEvent: LocationEvent? = null
    private val backgroundLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {

            }
        }


    private val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            when {
                it.get(Manifest.permission.ACCESS_COARSE_LOCATION) ?: false -> {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {

                        backgroundLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

                    }


                }
                it.get(Manifest.permission.ACCESS_FINE_LOCATION) ?: false -> {

                }


            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        service = Intent(this, LocationService::class.java)
        startService(service)

        button_UpdateLocation.setOnClickListener {
            checkPermission()

        }

    }

    override fun onStart() {
        super.onStart()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)

        }


    }

    override fun onDestroy() {
        super.onDestroy()
//        stopService(service)
//        if (EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().unregister(this)
//        }


    }


    fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

            )
        } else {
            startService(service)

        }


    }

    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent) {

    }

    override fun onResume() {
        super.onResume()
    }


}