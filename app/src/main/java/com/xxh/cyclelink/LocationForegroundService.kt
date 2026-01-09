package com.xxh.cyclelink

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.*
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import java.util.*
import androidx.core.content.edit


class LocationForegroundService : Service() {

    companion object {
        const val ACTION_STOP_RIDE = "ACTION_STOP_RIDE"
    }

    object TrackSession {

        private const val PREF = "track_session"
        private const val KEY_TRACK_ID = "track_id"

        fun getOrCreate(context: Context): UUID {
            val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            val saved = sp.getString(KEY_TRACK_ID, null)

            return if (saved != null) {
                UUID.fromString(saved)
            } else {
                val newId = UUID.randomUUID()
                sp.edit { putString(KEY_TRACK_ID, newId.toString()) }
                newId
            }
        }

        fun clear(context: Context) {
            context
                .getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit {
                    remove(KEY_TRACK_ID)
                }
        }
    }


    private lateinit var locationManager: LocationManager
    private lateinit var locationThread: HandlerThread
    private lateinit var locationHandler: Handler
    private lateinit var trackId: UUID

    private var lastUploadTime = 0L

    override fun onCreate() {
        super.onCreate()
        Log.e("cycle_link", "🔥 Service CREATED")

        // 初始化 trackId
        // ✅ 从缓存取，没才生成
        trackId = TrackSession.getOrCreate(this)

        // 前台通知
        LocationNotification.createChannel(this)
        startForeground(1001, LocationNotification.build(this))

        // 初始化 LocationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationThread = HandlerThread("location_thread").also { it.start() }
        locationHandler = Handler(locationThread.looper)

        startLocationUpdates()
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.e("cycle_link", "📍 Location: ${location.latitude}, ${location.longitude}")

                sendBroadcast(
                    Intent("CYCLE_LOCATION").apply {
                        putExtra("lat", location.latitude)
                        putExtra("lon", location.longitude)
                        putExtra("speed", location.speed)
                        putExtra("accuracy", location.accuracy)
                        putExtra("track_id", trackId.toString())
                    }
                )
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L,
                1f,
                listener,
                locationHandler.looper
            )
        } catch (e: Exception) {
            Log.e("cycle_link", "GPS_PROVIDER error: ${e.message}")
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000L,
                1f,
                listener,
                locationHandler.looper
            )
        } catch (e: Exception) {
            Log.e("cycle_link", "NETWORK_PROVIDER error: ${e.message}")
        }
    }

    override fun onDestroy() {
        locationManager.removeUpdates {  }
        locationThread.quitSafely()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            ACTION_STOP_RIDE -> {
                stopRide()
                stopSelf()
                return START_NOT_STICKY
            }
        }

        return START_STICKY
    }


    private fun stopRide(){
        Log.e("cycle_link", "🟥 Stop ride")

        // ✅ 清空 TrackSession（Service 内部）
        TrackSession.clear(this)

        locationManager.removeUpdates {  }
        locationThread.quitSafely()

    }
}
