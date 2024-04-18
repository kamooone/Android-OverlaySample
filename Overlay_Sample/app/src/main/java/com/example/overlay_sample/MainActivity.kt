package com.example.overlay_sample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // オーバーレイ許可の結果を受け取る処理
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // パーミッションがまだ許可されていない場合はOverlayServiceを開始する
                startService(Intent(this, OverlayService::class.java))
            }
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                // バッテリー状態の変化をOverlayServiceに通知
                val serviceIntent = Intent(context, OverlayService::class.java)
                serviceIntent.putExtra("batteryLevel", batteryLevel)
                serviceIntent.putExtra("batteryStatus", batteryStatus)
                context?.startService(serviceIntent)
            }
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

        // バッテリー状態の変化を監視するBroadcastReceiverを登録
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onDestroy() {
        super.onDestroy()
        // BroadcastReceiverの登録を解除
        unregisterReceiver(batteryReceiver)
    }
}
