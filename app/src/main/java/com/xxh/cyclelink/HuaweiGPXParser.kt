package com.xxh.cyclelink

import org.w3c.dom.Element
import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

// -------------------------
// 数据类定义
// -------------------------
data class TrackExtensions(
    val totalTime: Double,
    val cumulativeDecrease: Double,
    val cumulativeClimb: Double,
    val totalDistance: Double,
    val routeType: Int
)

data class TrackPoint(
    val lat: Double,
    val lon: Double,
    val ele: Double,
    val time: String
)

data class TrackData(
    val extensions: TrackExtensions,
    val trackPoints: List<TrackPoint>
)

// -------------------------
// GPX 解析函数
// -------------------------
object HuaweiGPXParser {

    /**
     * 解析 GPX 文件
     * @param gpxFile GPX 文件
     * @return TrackData? 返回轨迹扩展信息 + 轨迹点列表，失败返回 null
     */
    fun parseGpxFile(gpxFile: File): TrackData? {
        return try {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(gpxFile)
            doc.documentElement.normalize()

            // 取第一个 <trk>
            val trk = doc.getElementsByTagName("trk").item(0) as? Element ?: return null

            // 解析 <extensions>
            val extElem = trk.getElementsByTagName("extensions").item(0) as Element
            val extensions = TrackExtensions(
                totalTime = extElem.getElementsByTagName("totalTime").item(0).textContent.toDouble(),
                cumulativeDecrease = extElem.getElementsByTagName("cumulativeDecrease").item(0).textContent.toDouble(),
                cumulativeClimb = extElem.getElementsByTagName("cumulativeClimb").item(0).textContent.toDouble(),
                totalDistance = extElem.getElementsByTagName("totalDistance").item(0).textContent.toDouble(),
                routeType = extElem.getElementsByTagName("routeType").item(0).textContent.toInt()
            )

            // 解析 <trkpt> 轨迹点
            val trkptList = mutableListOf<TrackPoint>()
            val trkpts = trk.getElementsByTagName("trkpt")
            for (i in 0 until trkpts.length) {
                val trkpt = trkpts.item(i) as Element
                val lat = trkpt.getAttribute("lat").toDouble()
                val lon = trkpt.getAttribute("lon").toDouble()
                val ele = trkpt.getElementsByTagName("ele").item(0).textContent.toDouble()
                val time = trkpt.getElementsByTagName("time").item(0).textContent
                trkptList.add(TrackPoint(lat, lon, ele, time))
            }

            TrackData(extensions, trkptList)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun parseGpxInputStream(inputStream: InputStream): TrackData? {
        return try {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
            doc.documentElement.normalize()
            // 和 parseGpxFile 一样的解析逻辑
            val trk = doc.getElementsByTagName("trk").item(0) as? Element ?: return null
            val extElem = trk.getElementsByTagName("extensions").item(0) as Element
            val extensions = TrackExtensions(
                totalTime = extElem.getElementsByTagName("totalTime").item(0).textContent.toDouble(),
                cumulativeDecrease = extElem.getElementsByTagName("cumulativeDecrease").item(0).textContent.toDouble(),
                cumulativeClimb = extElem.getElementsByTagName("cumulativeClimb").item(0).textContent.toDouble(),
                totalDistance = extElem.getElementsByTagName("totalDistance").item(0).textContent.toDouble(),
                routeType = extElem.getElementsByTagName("routeType").item(0).textContent.toInt()
            )

            val trkptList = mutableListOf<TrackPoint>()
            val trkpts = trk.getElementsByTagName("trkpt")
            for (i in 0 until trkpts.length) {
                val trkpt = trkpts.item(i) as Element
                val lat = trkpt.getAttribute("lat").toDouble()
                val lon = trkpt.getAttribute("lon").toDouble()
                val ele = trkpt.getElementsByTagName("ele").item(0).textContent.toDouble()
                val time = trkpt.getElementsByTagName("time").item(0).textContent
                trkptList.add(TrackPoint(lat, lon, ele, time))
            }

            TrackData(extensions, trkptList)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

// -------------------------
// 使用示例
// -------------------------
fun main() {

//    val assetManager = context.assets
//    val inputStream = assetManager.open("your_file.gpx") // assets/your_file.gpx
//    val trackData = GPXParser.parseGpxInputStream(inputStream)

    val gpxFile = File("/path/to/your/file.gpx")
    val trackData = HuaweiGPXParser.parseGpxFile(gpxFile)

    trackData?.let {
        println("=== Track Extensions ===")
        println("Total time: ${it.extensions.totalTime}")
        println("Cumulative decrease: ${it.extensions.cumulativeDecrease}")
        println("Cumulative climb: ${it.extensions.cumulativeClimb}")
        println("Total distance: ${it.extensions.totalDistance}")
        println("Route type: ${it.extensions.routeType}")
        println("=== Track Points ===")
        println("Number of points: ${it.trackPoints.size}")
        it.trackPoints.forEachIndexed { index, point ->
            println("${index + 1}: ${point.lat}, ${point.lon}, ${point.ele}, ${point.time}")
        }
    } ?: println("解析失败或文件格式不正确")
}
