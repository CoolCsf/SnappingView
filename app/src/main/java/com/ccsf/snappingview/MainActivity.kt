package com.ccsf.snappingview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.TextValueSanitizer
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val view = TestView(this, "测试测试撒打算打算打算打算打算", 3)
        rl_main.addView(view)
        val paramLayout = view.layoutParams as RelativeLayout.LayoutParams
        paramLayout.addRule(RelativeLayout.CENTER_IN_PARENT)
        view.layoutParams = paramLayout
    }
}
