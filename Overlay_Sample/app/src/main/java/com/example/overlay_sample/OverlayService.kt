package com.example.overlay_sample

import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

class OverlayService : Service() {

    // lateinitはプロパティを宣言した時点では初期化されませんが、後でプロパティを初期化することができます
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var sharedPreferences: SharedPreferences

    // Binderは、ServiceとActivity間で通信を行うための仕組み
    private val binder = OverlayBinder()

    // OverlayBinderはOverlayServiceクラス内部で定義されており、これによりOverlayServiceのプライベートなメンバーにアクセスすることができる
    // inner classは、Kotlinで内部クラスを定義するためのキーワードです。内部クラスは、外部クラス内にネストされたクラスであり、外部クラスのメンバーにアクセスできます。
    inner class OverlayBinder : Binder() {
        // そのインスタンスが属しているOverlayServiceクラスのインスタンスを返す
        fun getService(): OverlayService {
            return this@OverlayService
        }
    }

    override fun onCreate() {
        super.onCreate()

        // WINDOW_SERVICEを取得、その際にWindowManagerインスタンスにキャスト
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // LayoutInflater クラスの inflate() メソッドを呼び出すことで、指定されたXMLファイルを読み込み、それを表すビューオブジェクトを作成します。
        // このビューオブジェクトは、アプリケーションのUIに表示されたり、プログラムで操作されたりします。
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        // クリックイベントを追加(オーバーレイ全体)
//        overlayView.setOnClickListener {
//            Log.d("OverlayService", "オーバーレイクリックイベント")
//        }

        // オーバーレイで表示している画像に個別にクリックイベントを設定する場合は、その画像のidを取得する
        val imageView1: ImageView = overlayView.findViewById(R.id.imageView)
        imageView1.setOnClickListener {
            Log.d("OverlayService", "オーバーレイ個別画像クリックイベント")

            val intent = Intent(this, SettingActivity::class.java)
            // FLAG_ACTIVITY_NEW_TASKは、通常、サービスやブロードキャストレシーバなどのコンポーネントからアクティビティを開始する場合に使用されます。
            // これにより、アクティビティがアプリケーションの中で別のコンテキストで開始され、そのアクティビティが新しいタスク内で独立して実行されます。
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        // OverlayPrefs という名前のプリファレンスファイルを取得し、sharedPreferences という名前の SharedPreferences インスタンスに代入
        // SharedPreferences は、アプリケーションのデータを保存・読み込みするための仕組みであり、キーと値のペアを格納することができる
        sharedPreferences = getSharedPreferences("OverlayPrefs", Context.MODE_PRIVATE)

        // 保存されたxの値を取得
        val savedX = sharedPreferences.getInt("overlay_position_x", 500)

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
        params.x = savedX
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
    fun updateOverlay(batteryLevel: Int, batteryStatus: Int) {
        // オーバーレイで表示しているバッテリーメーターの画像を更新
        updateOverlayImage()

        // オーバーレイで表示しているバッテリー残量テキストの表示を更新
        val textView = overlayView.findViewById<TextView>(R.id.textView)
        textView.text = "電池残量: $batteryLevel%"

        // SharedPreferencesに保存されているテキストの色に変更
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        // "textColor"キーに対応する整数値を取得し、デフォルトの色はColor.BLACKとする
        val textColor = sharedPreferences.getInt("textColor", Color.BLACK)
        // テキストビューのテキスト色を設定
        textView.setTextColor(textColor)
    }

    /*
     * オーバーレイで表示しているテキストの色の変更を更新する処理
     */
    fun updateOverlayTextColor() {
        // オーバーレイで表示しているバッテリー残量テキストの色を更新
        val textView = overlayView.findViewById<TextView>(R.id.textView)

        // SharedPreferencesに保存されているテキストの色に変更
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        // "textColor"キーに対応する整数値を取得し、デフォルトの色はColor.BLACKとする
        val textColor = sharedPreferences.getInt("textColor", Color.BLACK)
        // テキストビューのテキスト色を設定
        textView.setTextColor(textColor)
    }

    /*
     * オーバーレイで表示している画像の変更を更新する処理
     */
    fun updateOverlayImage() {
        val imageView = overlayView.findViewById<ImageView>(R.id.imageView)

        // SharedPreferencesに保存されている画像リソースIDを取得
        val sharedPreferences = getSharedPreferences("Image", Context.MODE_PRIVATE)
        val imageResId = sharedPreferences.getInt("imageResId", R.drawable.battery_mark1)

        // ImageViewに取得した画像リソースIDをセット
        imageView.setImageResource(imageResId)
    }

    /*
     * バッテリーメータの画像とテキストの位置を移動させる処理
     */
    fun moveImage(x: Int) {
        val layoutParams = overlayView.layoutParams as WindowManager.LayoutParams
        layoutParams.x = x
        windowManager.updateViewLayout(overlayView, layoutParams)

        // SharedPreferencesにxの値を保存
        val editor = sharedPreferences.edit()
        editor.putInt("overlay_position_x", x)
        editor.apply()
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

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)
    }

    // onBind()メソッドは、Serviceがバインドされたときに呼び出されます。
    // バインドされると、クライアントはこのメソッドからIBinderオブジェクトを受け取り、そのオブジェクトを使用してServiceとの通信を行います。
    override fun onBind(intent: Intent): IBinder? {
        return binder
    }
}
