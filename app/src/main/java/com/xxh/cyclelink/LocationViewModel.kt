package com.xxh.cyclelink

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


class LocationViewModel(app: Application) : AndroidViewModel(app) {



    private val _currentLocation = MutableStateFlow<LocationDTO?>(null)
    val currentLocation: StateFlow<LocationDTO?> = _currentLocation

    // 上传点计数
    private val _uploadCount = MutableStateFlow(0)
    val uploadCount: StateFlow<Int> = _uploadCount

    private val _gpsLost = MutableStateFlow(false)
    val gpsLost: StateFlow<Boolean> = _gpsLost

    // 上一次有效上传点
    private var lastUploadedLocation: LocationDTO? = null
    private var lastUploadTime: Long = 0L

    private val MIN_DISTANCE_METERS = 5f
    private val MIN_INTERVAL_MS = 5_000L

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val latitude = it.getDoubleExtra("lat", 0.0)
                val longitude = it.getDoubleExtra("lon", 0.0)
                val speed = it.getFloatExtra("speed", 0f)
                val accuracy = it.getFloatExtra("accuracy", 0f)
                val track_Id = it.getStringExtra("track_id") ?: UUID.randomUUID().toString()
//                val track_Id ="79824e56-2836-4bce-aa79-fe36b7ccb9b7"

                val dto = LocationDTO(track_Id, latitude, longitude, accuracy, speed)
                _currentLocation.value = dto

                val now = System.currentTimeMillis()

                // 判断时间间隔
                if (now - lastUploadTime < MIN_INTERVAL_MS) return

                // 判断距离
                val last = lastUploadedLocation
                if (last != null) {
                    val distance = distanceBetween(
                        last.latitude, last.longitude,
                        dto.latitude, dto.longitude
                    )
                    if (distance < MIN_DISTANCE_METERS) return
                }

                if (accuracy > 50f) {
                    Log.d("GPS", "Skip point, poor accuracy: $accuracy m")
                    _gpsLost.value = true
                    return
                } else {
                    _gpsLost.value = false
                }

                // 更新上次上传信息
                lastUploadedLocation = dto
                lastUploadTime = now

                // 上传到 Supabase
                viewModelScope.launch {
                    try {
                        uploadLocation(dto)

                        // 上传成功，计数 +1
                        _uploadCount.value += 1

                    } catch (e: Exception) {
                        Log.e("UPLOAD", e.message ?: "error")
                    }
                }
            }
        }
    }

    init {
        // 注册广播
        val filter = IntentFilter("CYCLE_LOCATION")
        ContextCompat.registerReceiver(
            app,
            locationReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onCleared() {
        getApplication<Application>().unregisterReceiver(locationReceiver)
        super.onCleared()
    }


    private suspend fun uploadLocation(location: LocationDTO) {

        Log.d("LocationViewModel", "uploadLocation")
        withContext(Dispatchers.IO) {

            try {
                SupabaseManager.client
                    .from("track_points")   // 表名
                    .insert(location)

                Log.d("LocationViewModel", "Location uploaded: $location")
            } catch (e: Exception) {
                Log.e("LocationViewModel", "Upload failed", e)
            }
        }

    }

    // 计算两点间距离（米）
    private fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    fun clearRide() {
        val ctx = getApplication<Application>()

        val intent = Intent(ctx, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.ACTION_STOP_RIDE
        }

        ctx.startService(intent)

        // 仅重置 UI 状态
        _uploadCount.value = 0
        _currentLocation.value = null
        _gpsLost.value = false
    }
}





