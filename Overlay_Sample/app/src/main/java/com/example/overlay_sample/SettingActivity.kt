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

    // OverlayServiceとの接続を管理するためのserviceConnection変数が定義
    private var overlayService: OverlayService? = null

    // ServiceConnectionを使用してOverlayServiceとのバインドを処理
    // ServiceConnectionは、Serviceとの接続状態の変化を監視し、それに応じて適切な処理を行うためのインタフェース
    private val serviceConnection = object : ServiceConnection {

        // onServiceConnected()およびonServiceDisconnected()メソッドをオーバーライドして、
        // Serviceとの通信が確立または切断されたときに適切な処理を行います。
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // Serviceとの接続が確立されたときに呼び出されます。引数として、Serviceの名前とIBinderが渡されます。
            // serviceパラメーターには、Serviceが返すBinderオブジェクトが渡されます。
            // ここでは、このBinderをOverlayService.OverlayBinder型にキャストし、
            // getService()メソッドを使用してOverlayServiceのインスタンスを取得し、overlayService変数に格納
            val binder = service as OverlayService.OverlayBinder
            overlayService = binder.getService()
        }

        // Serviceとの接続が切断されたときに呼び出されます。
        // 引数として、Serviceの名前が渡されます。このメソッドでは、overlayService変数をnullに設定
        override fun onServiceDisconnected(name: ComponentName?) {
            overlayService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // 現在のアクティビティを終了し、スタック内の前のアクティビティに戻ります。
        val backButton: Button = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        // OverlayServiceをバインド
        val intent = Intent(this, OverlayService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        // オーバーレイのテキストの色の変更をsharedPreferencesに保存
        val colorChangeButton: Button = findViewById(R.id.color_button)
        colorChangeButton.setOnClickListener {
            val selectedColor = Color.RED
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("textColor", selectedColor)
            editor.apply()

            // updateOverlayを呼び出して変更を反映
            overlayService?.updateOverlayTextColor()
        }

    }
}