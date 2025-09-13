package com.xxh.cyclelink

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import io.ticofab.androidgpxparser.parser.GPXParser
import io.ticofab.androidgpxparser.parser.domain.Gpx
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.tan


data class LatLng(val lat: Double, val lon: Double)

fun parseGpxFromAssets(context: Context): Gpx? {
    val parser = GPXParser()
    val inputStream = context.assets.open("4994735.gpx")
    return parser.parse(inputStream)
}

fun extractLatLngList(gpx: Gpx): List<LatLng> {
    val list = mutableListOf<LatLng>()
    gpx.tracks.forEach { track ->
        track.trackSegments.forEach { segment ->
            segment.trackPoints.forEach { point ->
                list.add(LatLng(point.latitude, point.longitude))
            }
        }
    }
    return list
}

fun parseGpx(context: Context): List<LatLng> {
//    val fileName_1 = "20250622户外骑行.gpx"
    val fileName_2 = "20250907户外骑行.gpx"

    val inputStream = context.assets.open(fileName_2)
    val parser = GPXParser() // io.ticofab:gpxparser
    val gpx = parser.parse(inputStream)

    val result = mutableListOf<LatLng>()
    gpx.tracks.forEach { track ->
        track.trackSegments.forEach { segment ->
            segment.trackPoints.forEach { pt ->
                result.add(LatLng(pt.latitude, pt.longitude))
            }
        }
    }
    return result
}


//墨卡托投影
//将经纬度转换为平面坐标（x, y），使轨迹更真实、等比例。
fun latLngToMercator(lat: Double, lon: Double): Pair<Double, Double> {
    val R = 6378137.0 // 地球半径（米）
    val x = R * Math.toRadians(lon)
    val y = R * ln(tan(Math.PI / 4 + Math.toRadians(lat) / 2))
    return Pair(x, y)
}


// 归一化墨卡托坐标到画布屏幕
fun normalizePoints(
    points: List<Pair<Double, Double>>,
    canvasWidth: Float,
    canvasHeight: Float
): List<Offset> {
    val xs = points.map { it.first }
    val ys = points.map { it.second }

    val minX = xs.minOrNull() ?: 0.0
    val maxX = xs.maxOrNull() ?: 1.0
    val minY = ys.minOrNull() ?: 0.0
    val maxY = ys.maxOrNull() ?: 1.0

    val scaleX = canvasWidth / (maxX - minX)
    val scaleY = canvasHeight / (maxY - minY)

    // 保持长宽比例一致（等比缩放）
    val scale = min(scaleX, scaleY)

    return points.map { (x, y) ->
        val offsetX = (x - minX) * scale
        val offsetY = canvasHeight - (y - minY) * scale
        Offset(offsetX.toFloat(), offsetY.toFloat())
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GPXTrackCanvas(gpxPoints: List<LatLng>, modifier: Modifier = Modifier) {
    if (gpxPoints.size < 2) return

    BoxWithConstraints(modifier = modifier
        .padding(40.dp)
        .fillMaxWidth()
        .aspectRatio(1.0f)

    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxWidth.toFloat()

        // 转换为墨卡托坐标
        val mercatorPoints = gpxPoints.map {
            latLngToMercator(it.lat, it.lon)
        }

        // 归一化为屏幕坐标
        val screenPoints = normalizePoints(
            mercatorPoints, widthPx, heightPx
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0 until screenPoints.size - 1) {
                drawLine(
                    color = Color.Red,
                    start = screenPoints[i],
                    end = screenPoints[i + 1],
                    strokeWidth = 4f
                )
            }
        }
    }
}

@Composable
fun GpxViewer() {
    val context = LocalContext.current
    val trackPoints = remember { mutableStateListOf<LatLng>() }

    LaunchedEffect(Unit) {
        trackPoints.clear()
        trackPoints.addAll(parseGpx(context))
    }
    if (trackPoints.isNotEmpty()) {
        GPXTrackCanvas(gpxPoints = trackPoints)
    }

}

@Composable
fun CaptureComposable() {
    val context = LocalContext.current
    val view = LocalView.current

    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        // 需要截取的部分
        GpxViewer()

        Spacer(modifier = Modifier.height(24.dp))

        // 截图按钮
        Button(onClick = {
            val bitmap = ScreenshotUtil.getTransparentBitmapFromView(view)
//            ScreenshotUtil.saveBitmapToFile(context, bitmap, "compose_screen3")
            ScreenshotUtil.saveBitmapToGallery(context, bitmap, "compose_screen3")
        }) {
            Text("截取整个Compose界面")
        }
    }
}

