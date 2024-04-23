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

    // lateinitはプロパティを宣言した時点では初期化されませんが、後でプロパティを初期化することができます
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        // WINDOW_SERVICEを取得、その際にWindowManagerインスタンスにキャスト
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // LayoutInflater クラスの inflate() メソッドを呼び出すことで、指定されたXMLファイルを読み込み、それを表すビューオブジェクトを作成します。
        // このビューオブジェクトは、アプリケーションのUIに表示されたり、プログラムで操作されたりします。
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        // OverlayPrefs という名前のプリファレンスファイルを取得し、sharedPreferences という名前の SharedPreferences インスタンスに代入
        // SharedPreferences は、アプリケーションのデータを保存・読み込みするための仕組みであり、キーと値のペアを格納することができる
        sharedPreferences = getSharedPreferences("OverlayPrefs", Context.MODE_PRIVATE)

        // ウィンドウのパラメータを設定
        val params = WindowManager.LayoutParams(
            // コンテンツのサイズに合わせてウィンドウのサイズを調整
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // プリケーションの他の要素の上に表示されるオーバーレイウィンドウを示します。
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                // 電話アプリなどの通常のアプリケーションウィンドウを示します。
                WindowManager.LayoutParams.TYPE_PHONE
            },

            // ウィンドウがフォーカスを受け取らないことを示します。他にも、ウィンドウの動作や表示方法を制御するためのフラグがあります。
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

            //ウィンドウが透明または半透明であることを示します。
            PixelFormat.TRANSLUCENT
        )

        // ウィンドウの表示位置を指定します。
        params.x = 500
        params.y = -800

        // WindowManagerにoverlayViewを追加
        windowManager.addView(overlayView, params)

        // 保存されたバッテリーレベルを読み込み
        val batteryLevel = sharedPreferences.getInt("batteryLevel", 0)
        val batteryStatus = sharedPreferences.getInt("batteryStatus", 0)

        // 表示を更新
        updateOverlay(batteryLevel, batteryStatus)
    }

    /*
     * startService実行後に呼ばれるメソッドの一つ(Serviceが開始されるたびに呼び出されるメソッド)
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 保存していたバッテリーの状態を取得
        val batteryLevel = intent?.getIntExtra("batteryLevel", 0) ?: 0
        val batteryStatus = intent?.getIntExtra("batteryStatus", 0) ?: 0

        // 表示を更新
        updateOverlay(batteryLevel, batteryStatus)

        saveBatteryLevel(batteryLevel, batteryStatus)

        // 個別にカスタマイズする必要はないため、特に気にする必要はなし
        return super.onStartCommand(intent, flags, startId)
    }

    /*
     * オーバーレイで表示している内容を更新する処理
     */
    private fun updateOverlay(batteryLevel: Int, batteryStatus: Int) {
        // オーバーレイで表示しているバッテリーメーターの画像を更新
        // (ToDo:batteryStatusなどの情報を使って適切な画像を表示する処理を追加する)
        val imageView = overlayView.findViewById<ImageView>(R.id.imageView)
        imageView.setImageResource(R.drawable.battery_mark1)

        // オーバーレイで表示しているバッテリー残量テキストの表示を更新
        val textView = overlayView.findViewById<TextView>(R.id.textView)
        textView.text = "電池残量: $batteryLevel%"
    }

    /*
     * バッテリーレベルとバッテリー状態を SharedPreferences に保存するためのメソッド
     */
    private fun saveBatteryLevel(batteryLevel: Int, batteryStatus: Int) {
        // SharedPreferencesの編集用インスタンスを取得
        val editor = sharedPreferences.edit()

        // putInt メソッドを使用してSharedPreferencesにバッテリー情報を保存
        // 第一引数がラベルで、第二引数が値
        editor.putInt("batteryLevel", batteryLevel)
        editor.putInt("batteryStatus", batteryStatus)

        // applyメソッドで変更内容を適用
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
