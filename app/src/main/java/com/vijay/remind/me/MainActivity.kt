package com.vijay.remind.me

import android.annotation.SuppressLint
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.greysonparrelli.permiso.Permiso
import com.vijay.androidutils.Logger
import io.reactivex.Single


class MainActivity : AppCompatActivity() {
    lateinit var mGoogleMap: GoogleMap
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Permiso.getInstance().setActivity(this)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            mGoogleMap = it
        }

        checkLocationPermission().subscribe({ hasPermission ->
            if (hasPermission) {

            }
        }, {
            it.printStackTrace()
        })
    }

    override fun onResume() {
        super.onResume()
        Permiso.getInstance().setActivity(this)
        checkLocationPermission()
    }

    private fun checkLocationPermission(): Single<Boolean> {
        return Single.create {
            if (!checkIfGPSEnabled()) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                it.onSuccess(false)
                return@create
            }


            Permiso.getInstance().requestPermissions(
                object : Permiso.IOnPermissionResult {
                    override fun onRationaleRequested(
                        callback: Permiso.IOnRationaleProvided?,
                        vararg permissions: String?
                    ) {

                    }

                    override fun onPermissionResult(resultSet: Permiso.ResultSet) {
                        if (resultSet.areAllPermissionsGranted()) {
                            Logger.i(TAG, "Location permission granted")
                            it.onSuccess(true)
                        } else {
                            Logger.i(TAG, "Location permission denied")
                            showPermissionError()
                            it.onSuccess(false)
                        }
                    }
                },
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    private fun checkIfGPSEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showPermissionError() {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }
}
