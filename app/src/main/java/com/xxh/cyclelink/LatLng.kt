package com.xxh.cyclelink


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.ticofab.androidgpxparser.parser.GPXParser
import io.ticofab.androidgpxparser.parser.domain.Gpx
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.tan
import androidx.core.graphics.createBitmap


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
    val fileName_2 = "20250907户外骑行.gpx"
    val inputStream = context.assets.open(fileName_2) // assets/your_file.gpx
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

/**
 * 小组件：显示骑行数据项
 * @param label 标题，例如 "距离"
 * @param value 数值，例如 "150 km"
 * @param labelColor 标题颜色（默认黑色）
 * @param valueColor 数值颜色（默认深灰）
 * @param labelFontSize 标题字体大小
 * @param valueFontSize 数值字体大小
 */
@Composable
fun RideStatItem(
    label: String,
    value: String,
    labelColor: Color = Color.White,
    valueColor: Color = Color.White,
    labelFontSize: Int = 14,
    valueFontSize: Int = 27
) {
    Column(
        modifier = Modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally // 居中对齐
    ) {
        Text(
            text = label,
            fontSize = labelFontSize.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor
        )
        Text(
            text = value,
            fontSize = valueFontSize.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun RideStats(trackData: TrackData) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally // 居中对齐
    ) {
        RideStatItem(
            label = "距离",
            value = "${trackData.extensions.totalDistance / 1000} km"  // 米转公里
        )

        RideStatItem(
            label = "总时间",
            value = "${(trackData.extensions.totalTime / 3600).toInt()}h ${(trackData.extensions.totalTime / 60 % 60).toInt()}m"
        )

        RideStatItem(
            label = "累计爬升",
            value = "${trackData.extensions.cumulativeClimb.toInt()} m"
        )

//        RideStatItem(
//            label = "累计下降",
//            value = "${trackData.extensions.cumulativeDecrease.toInt()} m"
//        )
    }
}

@Composable
fun GPXExtensions(trackDataState: MutableState<TrackData?>, modifier: Modifier = Modifier) {


    trackDataState.value?.let { trackData ->
        RideStats(trackData)

        } ?: run {
            // 数据未加载时显示加载状态
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("加载中...")
            }
        }

}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GPXTrackCanvas(gpxPoints: List<LatLng>, modifier: Modifier = Modifier) {
    if (gpxPoints.size < 2) return

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        val quarter = maxWidth / 2

        Box(
            modifier = Modifier
                .size(quarter) // 宽高都设置为屏幕宽度的四分之一
        ){
            val DarkOrange = Color(0xFFFF8C00)

            Canvas(modifier = Modifier
                .fillMaxSize()
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // 转换为墨卡托坐标
                val mercatorPoints = gpxPoints.map {
                    latLngToMercator(it.lat, it.lon)
                }

                // 归一化到 [0,1]
                val normalizedPoints = normalizePoints(
                    mercatorPoints,
                    canvasWidth,
                    canvasHeight
                )

                // 计算轨迹包围盒的中心
                val minX = normalizedPoints.minOf { it.x }
                val maxX = normalizedPoints.maxOf { it.x }
                val minY = normalizedPoints.minOf { it.y }
                val maxY = normalizedPoints.maxOf { it.y }

                val trackCenterX = (minX + maxX) / 2
                val trackCenterY = (minY + maxY) / 2

                val canvasCenter = Offset(canvasWidth / 2, canvasHeight / 2)

                // 平移后的点
                val centeredPoints = normalizedPoints.map {
                    Offset(
                        x = it.x - trackCenterX + canvasCenter.x,
                        y = it.y - trackCenterY + canvasCenter.y
                    )
                }

                // 画轨迹
                for (i in 0 until centeredPoints.size - 1) {
                    drawLine(
                        color = DarkOrange,
                        start = centeredPoints[i],
                        end = centeredPoints[i + 1],
                        strokeWidth = 10f
                    )
                }
            }
        }

    }
}


@Composable
fun GpxViewer() {
    val context = LocalContext.current
    val trackPoints = remember { mutableStateListOf<LatLng>() }

    val assetManager = context.assets
    //    val fileName_1 = "20250622户外骑行.gpx"
    val fileName_2 = "20250907户外骑行.gpx"

    val trackDataState  = remember { mutableStateOf<TrackData?>(null) }

    LaunchedEffect(Unit) {

        val inputStream = assetManager.open(fileName_2) // assets/your_file.gpx
        val data = HuaweiGPXParser.parseGpxInputStream(inputStream)
        trackDataState.value = data

        trackPoints.clear()
        trackPoints.addAll(parseGpx(context))


    }
    if (trackPoints.isNotEmpty()) {

        //运动数据
        GPXExtensions(trackDataState = trackDataState)
        //轨迹图
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
            ScreenshotUtil.saveBitmapToGallery(context, bitmap, "compose_screen5")
        }) {
            Text("截取整个Compose界面")
        }
    }
}

@Composable
fun CaptureComposable2() {
    val context = LocalContext.current
    val gpxComposeView = remember { ComposeView(context) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 把 GpxViewer 放到 ComposeView
        AndroidView(
            factory = {
                gpxComposeView.apply {
                    setContent {
                        GpxViewer()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
                .fillMaxHeight(0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            val bitmap = ScreenshotUtil.getTransparentBitmapFromView(gpxComposeView)
            ScreenshotUtil.saveBitmapToGallery(context, bitmap, "compose_only_gpx1")
        }) {
            Text("只截取 GpxViewer")
        }
    }
}













