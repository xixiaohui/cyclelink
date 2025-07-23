package com.xxh.cyclelink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.MapsInitializer

import com.xxh.cyclelink.ui.theme.CyclelinkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 设置高德地图隐私合规
//        MapsInitializer.updatePrivacyShow(this, true, true)
//        MapsInitializer.updatePrivacyAgree(this, true)
//
//        AMapLocationClient.updatePrivacyShow(this, true, true)
//        AMapLocationClient.updatePrivacyAgree(this, true)
//

        setContent {
            CyclelinkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    Column {
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        )

                        GpxViewer()
                    }


                }
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