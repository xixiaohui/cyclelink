package com.xxh.cyclelink

import android.content.Context
import android.util.Log
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationService(context: Context) {

    private val mContext = context.applicationContext
    private val _trackPoints = mutableListOf<LatLng>()
    private val _locationFlow = MutableStateFlow<AMapLocation?>(null)
    val locationFlow: StateFlow<AMapLocation?> = _locationFlow
    val track: List<LatLng> get() = _trackPoints

    private val locationClient = AMapLocationClient(mContext)
    private val locationOption = AMapLocationClientOption().apply {
        locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        isNeedAddress = false
        isOnceLocation = false
        interval = 2000
    }

    init {
        locationClient.setLocationOption(locationOption)
        locationClient.setLocationListener { location ->
            if (location.errorCode == 0) {
                _locationFlow.value = location
                _trackPoints.add(LatLng(location.latitude, location.longitude))
            } else {
                Log.e("LocationService", "定位失败: ${location.errorCode} - ${location.errorInfo}")
            }
        }
    }

    fun start() = locationClient.startLocation()
    fun stop() = locationClient.stopLocation()
    fun onDestroy() = locationClient.onDestroy()
}
