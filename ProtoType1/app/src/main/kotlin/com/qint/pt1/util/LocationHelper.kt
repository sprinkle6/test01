/*
 * Author: Matthew Zhang
 * Created on: 4/12/19 3:52 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.qint.pt1.features.login.Login
import com.qint.pt1.features.users.UsersRepository
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class LocationHelper @Inject constructor(private val login: Login,
                                         private val usersRepository: UsersRepository) {
    private lateinit var activity: FragmentActivity

    private lateinit var locationManager: LocationManager

    fun init(activity: FragmentActivity) {
        this.activity = activity
        this.locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun requestAndReportLocation() {
        val rxPermissions = RxPermissions(activity)
        rxPermissions.request(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .subscribe { granted ->
                if (granted) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val address = getAddress()
                        if (address != null) {
                            reportAddress(address)
                        }
                    }
                } else {
                    Log.e(LOG_TAG, "location permission denied.")
                }
            }
    }

    private fun getLocationProviders(): List<String> {
        val enabledOnly = false
        return locationManager.getProviders(enabledOnly)
    }

    @SuppressLint("MissingPermission")
    private fun getAddress(): Address? {
        try {
            val providers = getLocationProviders()
            Log.d(LOG_TAG, "get ${providers.size} providers: ${providers}")
            val geocoder = Geocoder(activity, Locale.getDefault())
            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider) ?: continue
                val address =
                    geocoder.getFromLocation(location.latitude, location.longitude, 1).firstOrNull()
                if (address != null) {
                    Log.d(LOG_TAG, "location provider $provider returns address: ${address}")
                    return address
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "cannot get location: ${e.message}")
        }
        return null
    }

    private fun reportAddress(address: Address) {

        val provice = address.adminArea //省/自治区/直辖市
        val city = address.locality //直辖市/地级市
        val county = address.subLocality //区/县
        val location = com.qint.pt1.domain.Location(provice, city, county)
        login.location = location //副作用，保存location信息以便筛选等处使用

        Log.d(LOG_TAG, "reporting location: $location")

        if (!login.isLogined) return

        login.user?.info?.currentLocation = location //副作用，更新当前登录用户的本地location信息
        usersRepository.updateLocation(login.account!!, location).either(
            { failure -> Log.e(LOG_TAG, "update location failed: $failure") },
            { isSuccess -> Log.d(LOG_TAG, "update location success: $isSuccess") }
        )
    }
}