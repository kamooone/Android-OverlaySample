package com.example.overlay_sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val backButton: Button = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            // 現在のアクティビティを終了し、スタック内の前のアクティビティに戻ります。
            finish()
        }
    }
}