package com.xxh.cyclelink


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.xxh.cyclelink.ui.theme.CyclelinkTheme

class MainActivity : ComponentActivity() {

    private val vm: LocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 启动前台 Service
//        ContextCompat.startForegroundService(
//            this,
//            Intent(this, LocationForegroundService::class.java)
//        )

        setContent {
            CyclelinkTheme {
//                CaptureComposable()
//                AssetFileList()
//                GpxFileSelectorScreen()
//                LocationScreen(vm)
            }
        }
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

@Composable
fun LocationScreen(vm: LocationViewModel) {
    val location by vm.currentLocation.collectAsState()
    val uploadCount by vm.uploadCount.collectAsState()

    val gpsLost by vm.gpsLost.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (location != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Lat: ${location!!.latitude}\nLon: ${location!!.longitude}\nSpeed: ${location!!.speed} m/s",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "已上传点数: $uploadCount",
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "track id is: ${location!!.track_id.slice(IntRange(0,8))}",
                    textAlign = TextAlign.Center
                )
                if (gpsLost){
                    Text(
                        text = "⚠ GPS 信号弱，轨迹暂停记录",
                        textAlign = TextAlign.Center
                    )
                }

                if(location!!.accuracy>50f){
                    Text(
                        text = "半径大于100,是在地铁还是汽车上？",
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                /** 👇 清空骑行按钮 */
                Button(
                    onClick = { vm.clearRide() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("结束并清空骑行")
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("等待定位中…")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "已上传点数: $uploadCount",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


