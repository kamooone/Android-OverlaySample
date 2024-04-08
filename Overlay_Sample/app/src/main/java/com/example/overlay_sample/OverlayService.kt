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

        // ウィンドウマネージャーを取得
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // オーバーレイのビューを作成
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        // ImageViewに画像を設定
        val imageView = overlayView.findViewById<ImageView>(R.id.imageView)
        imageView.setImageResource(R.drawable.battery_mark1)

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

