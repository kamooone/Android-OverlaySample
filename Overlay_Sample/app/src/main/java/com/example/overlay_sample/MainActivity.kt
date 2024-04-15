package com.example.overlay_sample

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // オーバーレイ許可の結果を受け取る処理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            // パーミッションがまだ許可されていない場合はOverlayServiceを開始する
            startService(Intent(this, OverlayService::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // オーバーレイ表示のためのパーミッションを確認する
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            launcher.launch(intent)
        } else {
            // パーミッションが既に許可されている場合はOverlayServiceを開始する
            startService(Intent(this, OverlayService::class.java))
        }
    }
}