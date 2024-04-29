package com.example.overlay_sample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button

class SettingActivity : AppCompatActivity() {
    private var overlayService: OverlayService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as OverlayService.OverlayBinder
            overlayService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            overlayService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val backButton: Button = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            // 現在のアクティビティを終了し、スタック内の前のアクティビティに戻ります。
            finish()
        }

        // OverlayServiceをバインド
        val intent = Intent(this, OverlayService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        // オーバーレイのテキストの色の変更をsharedPreferencesに保存
        val colorChangeButton: Button = findViewById(R.id.color_button)
        colorChangeButton.setOnClickListener {
            val selectedColor = Color.BLACK
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("textColor", selectedColor)
            editor.apply()

            // ToDo:テキスト色変更メソッドと残量表示更新メソッドは分けたほうがいいかも
            val batteryLevel = sharedPreferences.getInt("batteryLevel", 0)
            val batteryStatus = sharedPreferences.getInt("batteryStatus", 0)

            // updateOverlayを呼び出して変更を反映
            overlayService?.updateOverlay(batteryLevel, batteryStatus)
        }

    }
}