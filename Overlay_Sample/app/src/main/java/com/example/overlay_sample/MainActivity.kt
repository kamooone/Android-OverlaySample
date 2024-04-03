package com.example.overlay_sample

import android.graphics.PixelFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ToDo:オーバーレイのパーミッションがないためクラッシュする模様

        // WindowManagerのインスタンスを取得
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 端末の画面の設定(オーバーレイ時に画面をどのような状態にするか)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,//ウィンドウの幅と高さをコンテンツに合わせる
            WindowManager.LayoutParams.WRAP_CONTENT,//ウィンドウの幅と高さをコンテンツに合わせる
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,//オーバーレイウィンドウが他のアプリの上に表示されることを示します。
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,//このウィンドウはユーザーからの操作を受け付けないことを意味します。
            PixelFormat.TRANSLUCENT//ウィンドウのピクセルフォーマットを透明に設定します。これにより、ウィンドウが透明な背景を持つことができます。
        )

        // オーバーレイで表示させる画面を取得
        val contentView: View = findViewById(android.R.id.content)

        // オーバーレイで表示する画面を追加
        windowManager.addView(contentView, params)
    }
}