package com.example.overlay_sample

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log
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

        // ウィンドウマネージャーを取得
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // オーバーレイのビューを作成
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        // ImageViewに画像を設定
        val imageView = overlayView.findViewById<ImageView>(R.id.imageView)
        imageView.setImageResource(R.drawable.battery_mark1)

        // TextViewにテキストを設定
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = this.registerReceiver(null, intentFilter)

        val batteryLevel = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val textView = overlayView.findViewById<TextView>(R.id.textView)
        textView.text = "電池残量" + batteryLevel.toString() + "%"

        // WindowManager.LayoutParamsを設定
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        // 画面右上に表示するための調整
        params.x = 500  // 画面の左端からの距離
        params.y = -800  // 画面の上端からの距離

        // ウィンドウにオーバーレイを追加
        windowManager.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        // オーバーレイのビューを削除
        windowManager.removeView(overlayView)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
