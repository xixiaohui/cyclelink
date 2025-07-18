package com.xxh.cyclelink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.xxh.cyclelink.ui.AMapView
import com.xxh.cyclelink.ui.AMapViewComposable
import com.xxh.cyclelink.ui.AMapViewLocationComposable
import com.xxh.cyclelink.ui.LocationPermissionScreen
import com.xxh.cyclelink.ui.RideMapScreen
import com.xxh.cyclelink.ui.RideTrackerScreen
import com.xxh.cyclelink.ui.RideTrackingMapScreen
import com.xxh.cyclelink.ui.theme.CyclelinkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 设置高德地图隐私合规
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)

        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)


        setContent {
            CyclelinkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )

                    MainScreen()
//                    AMapViewComposable()
//                    RideMapScreen()
//                    RideTrackingMapScreen()
                }
            }
        }
    }
}


@Composable
fun MainScreen() {
    var granted by remember { mutableStateOf(false) }

    if (!granted) {
        LocationPermissionScreen(onGranted = {
            granted = true
        })
    } else {
        // 地图模块或定位功能启用
        AMapViewComposable() // 显示地图组件
//        AMapView()
//        RideTrackerScreen()
//        RideMapScreen()

    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CyclelinkTheme {
        Greeting("Android")
    }
}