package com.xxh.cyclelink.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.amap.api.location.*
import com.amap.api.maps.*
import com.amap.api.maps.model.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.xxh.cyclelink.LocationPermissionHelper
import com.xxh.cyclelink.LocationService



@Composable
fun AMapViewComposable() {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    // 生命周期绑定
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                mapView.onCreate(Bundle())
            }

            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    AndroidView(factory = { mapView }) {
        // 可选：设置地图属性
        mapView.map.uiSettings.isZoomControlsEnabled = true
        mapView.map.uiSettings.isMyLocationButtonEnabled = true

        val locationStyle = MyLocationStyle().apply {
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
            interval(2000)
        }
        it.map.myLocationStyle = locationStyle
        it.map.isMyLocationEnabled = true


    }
}

@Composable
fun AMapViewLocationComposable(
    modifier: Modifier = Modifier,
    showMyLocation: Boolean = true,
    onMapReady: ((AMap) -> Unit)? = null
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    // 生命周期绑定，确保不崩溃
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                mapView.onCreate(Bundle())
            }

            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    // ✅ 显示地图视图
    AndroidView(
        modifier = modifier,
        factory = { mapView }
    ) {
        val aMap = it.map

        // 默认 UI 设置
        aMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
            isCompassEnabled = true
        }

        if (showMyLocation) {
            // 设置定位样式（可自定义图标、精度圆颜色等）
            val locationStyle = MyLocationStyle().apply {
                myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
                interval(2000)
                showMyLocation(true)
            }
            aMap.myLocationStyle = locationStyle
            aMap.isMyLocationEnabled = true
        } else {
            aMap.isMyLocationEnabled = false
        }

        // 初始化完成后的回调
        onMapReady?.invoke(aMap)
    }
}

@Composable
fun RideMapScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val locationService = remember { LocationService(context) }

    val location by locationService.locationFlow.collectAsState()
    val mapView = remember { MapView(context) }

    // 地图对象和轨迹
    var aMap by remember { mutableStateOf<AMap?>(null) }
    val trackPoints = remember { mutableStateListOf<LatLng>() }

    // 生命周期管理
    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                mapView.onCreate(Bundle())
                locationService.start()
            }

            override fun onResume(owner: LifecycleOwner) = mapView.onResume()
            override fun onPause(owner: LifecycleOwner) = mapView.onPause()
            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
                locationService.stop()
                locationService.onDestroy()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 当定位变化，更新轨迹并在地图上画线
    LaunchedEffect(location) {
        location?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            trackPoints.add(latLng)
            aMap?.clear()
            val polyline = PolylineOptions()
                .addAll(trackPoints)
                .width(10f)
                .color(Color.BLUE)
            aMap?.addPolyline(polyline)

            // 移动摄像头
            aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { mapView }
    ) {
        aMap = it.map
        it.map.uiSettings.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
            isCompassEnabled = true
        }

        // 显示蓝点
        val locationStyle = MyLocationStyle().apply {
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
            interval(10000)
            showMyLocation(true)
        }
        it.map.myLocationStyle = locationStyle
        it.map.isMyLocationEnabled = true
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WithLocationPermission(
    onGranted: @Composable () -> Unit,
    onDenied: () -> Unit
) {
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    when {
        permissionState.status.isGranted -> onGranted()
        permissionState.status.shouldShowRationale -> onDenied()
        else -> {} // 等待用户响应
    }
}



@Composable
fun RideMapContent() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val locationService = remember { LocationService(context) }
    val location by locationService.locationFlow.collectAsState()

    val mapView = remember { MapView(context) }
    var aMap by remember { mutableStateOf<AMap?>(null) }
    val trackPoints = remember { mutableStateListOf<LatLng>() }

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                mapView.onCreate(Bundle())
                locationService.start()
            }

            override fun onResume(owner: LifecycleOwner) = mapView.onResume()
            override fun onPause(owner: LifecycleOwner) = mapView.onPause()
            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
                locationService.stop()
                locationService.onDestroy()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(location) {
        location?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            trackPoints.add(latLng)
            aMap?.clear()
            aMap?.addPolyline(
                PolylineOptions()
                    .addAll(trackPoints)
                    .width(10f)
                    .color(Color.BLUE)
            )
            aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { mapView }
    ) {
        aMap = it.map
        it.map.uiSettings.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
        }
        val style = MyLocationStyle().apply {
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
            interval(2000)
        }
        it.map.myLocationStyle = style
        it.map.isMyLocationEnabled = true
    }
}


@Composable
fun RideTrackingMapScreen() {
    val context = LocalContext.current
    var showDeniedDialog by remember { mutableStateOf(false) }

    WithLocationPermission(
        onGranted = {
            RideMapContent()
        },
        onDenied = {
            showDeniedDialog = true
        }
    )

    if (showDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showDeniedDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("前往设置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeniedDialog = false }) {
                    Text("取消")
                }
            },
            title = { Text("权限说明") },
            text = { Text("为了记录骑行轨迹，请允许定位权限。") }
        )
    }
}




