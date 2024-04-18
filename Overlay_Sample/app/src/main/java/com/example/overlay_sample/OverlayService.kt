package com.example.overlay_sample

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.x = 500
        params.y = -800
        windowManager.addView(overlayView, params)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // バッテリー状態の変化に応じてオーバーレイの表示を更新
        val batteryLevel = intent?.getIntExtra("batteryLevel", 0) ?: 0
        val batteryStatus = intent?.getIntExtra("batteryStatus", 0) ?: 0
        updateOverlay(batteryLevel, batteryStatus)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateOverlay(batteryLevel: Int, batteryStatus: Int) {
        val imageView = overlayView.findViewById<ImageView>(R.id.imageView)
        // バッテリーメーターの画像を更新
        // batteryStatusなどの情報を使って適切な画像を表示する処理を追加する
        imageView.setImageResource(R.drawable.battery_mark1)

        val textView = overlayView.findViewById<TextView>(R.id.textView)
        // バッテリーレベルの表示を更新
        textView.text = "電池残量: $batteryLevel%"
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
