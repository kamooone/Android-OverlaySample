package com.example.overlay_sample

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        sharedPreferences = getSharedPreferences("OverlayPrefs", Context.MODE_PRIVATE)

        // 新しいオーバーレイを追加する前に、古いオーバーレイを削除
        removeOverlay()

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

        // 保存されたバッテリーレベルを読み込み、表示を更新
        val batteryLevel = sharedPreferences.getInt("batteryLevel", 0)
        val batteryStatus = sharedPreferences.getInt("batteryStatus", 0)
        updateOverlay(batteryLevel, batteryStatus)
    }

    private fun removeOverlay() {
        windowManager.removeView(overlayView)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // バッテリー状態の変化に応じてオーバーレイの表示を更新し、最新の状態を保存
        val batteryLevel = intent?.getIntExtra("batteryLevel", 0) ?: 0
        val batteryStatus = intent?.getIntExtra("batteryStatus", 0) ?: 0
        updateOverlay(batteryLevel, batteryStatus)
        saveBatteryLevel(batteryLevel, batteryStatus)
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

    private fun saveBatteryLevel(batteryLevel: Int, batteryStatus: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("batteryLevel", batteryLevel)
        editor.putInt("batteryStatus", batteryStatus)
        editor.apply()
    }

    // アプリを起動していない時でもオーバーレイの表示の更新を行うため
//    override fun onDestroy() {
//        super.onDestroy()
//        windowManager.removeView(overlayView)
//    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
