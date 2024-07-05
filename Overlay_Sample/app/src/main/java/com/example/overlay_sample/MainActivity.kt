package com.example.overlay_sample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    /*
     * registerForActivityResultは別のアクティビティを起動し結果を取得することができるメソッド
     * { result -> ... } の部分は、アクティビティの起動結果を受け取るためのコールバック関数
     * ランチャーとは機能を保持し、あとは簡単な操作のみで起動させるだけの機能のことです。
     * ActivityResultContracts.StartActivityForResult() は、startActivityForResult を実行するためのコントラクトを提供します。このコントラクトは、アクティビティを起動して結果を受け取るためのものです。
     * ActivityResultContract型
     * ActivityResultContractには色々定義されており、渡すものによって機能が異なります。
     *  ・StartActivityForResult→Activityを起動して結果を取得する
     *  ・RequestPermission→単体のパーミッション許可ダイアログを表示して結果を取得する
     *  ・ RequestMultiplePermissions→複数個のパーミッション許可ダイアログを表示して結果を取得する
     * registerForActivityResultの2つ目の引数ActivityResultCallback(コールバック)で実際の結果を受け取ることが可能になっています。
     */
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // 結果に対する処理 (オーバーレイ許可の結果を受け取る処理)

        /*
         * Settings.canDrawOverlays(this)→端末の設定でオーバーレイ描画が許可されているかどうかを確認するためのメソッド
         * this は、JavaやKotlinのプログラミング言語で、現在のインスタンスを指すキーワード。ここでのthisはSettings.canDrawOverlays()が呼び出されているクラスのインスタンスを指す。
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            startOverlayServiceAndRegisterBroadcastReceiver()
        } else {
            // 他のアプリの上に重ねて表示のパーミッションが許可されていない場合の処理
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        /*
         * Intent.ACTION_BATTERY_CHANGED アクションに関連付けられたブロードキャストが受信されたときに呼ばれるコールバック関数
         */
        override fun onReceive(context: Context?, intent: Intent?) {
            // 受け取ったIntentのアクションが Intent.ACTION_BATTERY_CHANGEDであるかどうか(バッテリーの状態が変化したことを示すブロードキャスト)
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                // バッテリーレベルとバッテリーの状態を取得
                val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                // バッテリー状態の変化をOverlayServiceに通知
                val serviceIntent = Intent(context, OverlayService::class.java)
                serviceIntent.putExtra("batteryLevel", batteryLevel)
                serviceIntent.putExtra("batteryStatus", batteryStatus)
                context?.startService(serviceIntent)
                /*
                 * startService後の流れ
                 * 1.サービスの開始: startService(Intent) メソッドが呼び出されると、指定された Intent に基づいてサービスが開始されます。この場合、OverlayService が開始されます。
                 * 2.サービスの作成と起動: OverlayService の onCreate() メソッドが呼び出され、サービスが作成されます。
                 *   このメソッドでは、ウィンドウマネージャーを初期化し、オーバーレイ用のビューを作成して表示します。その後、保存されたバッテリーレベルを読み込み、オーバーレイの表示を更新します。
                 * 3.ブロードキャストレシーバーの登録: MainActivity で registerReceiver() メソッドが呼び出され、batteryReceiver が Intent.ACTION_BATTERY_CHANGED をフィルタリングするように登録されます。
                 *   これにより、バッテリーの状態が変化したときに batteryReceiver の onReceive() メソッドが呼び出されるようになります。
                 * 4.バッテリー状態の通知: バッテリーの状態が変化すると、システムは Intent.ACTION_BATTERY_CHANGED をブロードキャストします。
                 *   このブロードキャストが受信されると、batteryReceiver の onReceive() メソッドが呼び出されます。
                 * 5.バッテリー状態の処理とオーバーレイの更新: onReceive() メソッド内で、バッテリーレベルとバッテリーの状態が取得されます。
                 *   次に、これらの情報が OverlayService に通知され、新しいインスタンスが作成されます。OverlayService の onStartCommand() メソッドが呼び出され、バッテリーの状態がオーバーレイに反映されます。
                 */

                /*
                 * startService(Intent) メソッドは、既にサービスが実行されている場合には新しいインスタンスを作成しません。
                 * その代わりに、既存のインスタンスが再利用され、onStartCommand() メソッドが呼び出されます。従って、サービスがすでに実行されている場合は、サービスの作成と起動のステップがスキップされます。
                 */
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // オーバーレイ表示のためのパーミッションを確認する
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            // オーバーレイ権限を付与するための設定画面を開く
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            launcher.launch(intent)
        } else {
            // パーミッションが既に許可されている場合はOverlayServiceを開始し、ブロードキャストを登録する
            startOverlayServiceAndRegisterBroadcastReceiver()
        }

        // 10秒後にアプリを終了させてオーバーレイの表示だけにする
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 10000)
    }

    // オーバーレイの表示を開始し、ブロードキャストを登録する関数
    private fun startOverlayServiceAndRegisterBroadcastReceiver() {
        /*
         * startService() メソッドは、Android アプリ内でサービスを開始するために使用されるメソッドです。
         * このメソッドには、開始するサービスを指定するための Intent オブジェクトが必要です。この Intent は、サービスのクラスを指定するために使用されます。
         * startService() メソッドを呼び出すと、Android システムは指定されたサービスを開始します。
         * その後、システムはサービスの onCreate() メソッドを呼び出し、サービスの初期化を行います。
         * サービスが開始されると、サービスはバックグラウンドで実行され、アプリの他の部分と独立して動作します。
         * サービスが不要になった場合は、stopService() メソッドを使用してサービスを停止できます。
         */
        startService(Intent(this, OverlayService::class.java))


        /*
         * registerReceiver() メソッドは、BroadcastReceiver を登録して、指定されたアクションまたはアクションのセットを受信するための方法を提供します。
         * このメソッドには、登録する BroadcastReceiver オブジェクトと、受信したいブロードキャストのフィルターを指定する IntentFilter オブジェクトが必要です。
         * IntentFilter オブジェクトには、受信したいブロードキャストのアクションが含まれます。
         * この場合、Intent.ACTION_BATTERY_CHANGED アクションを使用して、バッテリーの状態が変化したときにブロードキャストを受信します。
         * registerReceiver() メソッドを使用すると、アプリはバックグラウンドで実行されている間にブロードキャストを受信し、それに応じて適切な処理を実行できます。
         * ブロードキャストが受信されると、指定された BroadcastReceiver の onReceive() メソッドが呼び出され、ブロードキャストに関連する情報が提供されます。
         */
        /*
         * 第一引数 (batteryReceiver): 登録する BroadcastReceiver のインスタンスを指定します。これは、ブロードキャストが受信されたときに処理を実行するためのコールバック関数が含まれています。
         * 第二引数 (IntentFilter(Intent.ACTION_BATTERY_CHANGED)): 受信したいブロードキャストのアクションを指定する IntentFilter を作成して渡します。この場合、Intent.ACTION_BATTERY_CHANGED を指定しているので、バッテリーの状態が変化した際に送信されるブロードキャストを受信します。
         */
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        /*
         * Intent を使用することで、アプリケーション内のコンポーネントや他のアプリケーションとの通信を柔軟に行うことができます。
         * Intent.ACTION_BATTERY_CHANGED は、バッテリーの状態が変化したときに送信されるブロードキャストインテントのアクションです。
         * このアクションは、バッテリーの充電状態や残量の変化など、バッテリーに関する情報が変更されたときに送信されます。
         * アプリがバッテリーの状態に関する情報を取得したい場合に、このアクションを使用して IntentFilter を作成し、BroadcastReceiver を登録します。
         * その結果、バッテリーの状態が変化したときに、アプリは対応する処理を実行できます。
         */

        /*
         * registerReceiverはbatteryReceiver という BroadcastReceiver を登録するためのものです。
         * batteryReceiver は、バッテリーの状態が変化したときにアプリに通知を受け取るために使用されます。
         * 具体的には、registerReceiver() メソッドは、指定された IntentFilter が特定のアクションを受信したときに、
         * 指定された BroadcastReceiver を呼び出すようシステムに登録します。
         * この場合、Intent.ACTION_BATTERY_CHANGED はバッテリーの状態が変化したときに送信されるアクションです。
         * つまり、このコードはバッテリーの状態が変化したときに batteryReceiver を呼び出すようにシステムに登録します。
         */
    }

    // アプリを起動していない時でもオーバーレイの表示の更新を行うため
//    override fun onDestroy() {
//        super.onDestroy()
//        // BroadcastReceiverの登録を解除
//        unregisterReceiver(batteryReceiver)
//    }
}
