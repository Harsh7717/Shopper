package com.ksolutions.shopper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.ksolutions.shopper.R
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setupActionBar()

        var countryCode = intent.getStringExtra("countryCode")
        var phoneNo = intent.getStringExtra("phoneNo")
        et_mobile.setText(countryCode+phoneNo)
    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_sign_up_activity)
        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_sign_up_activity.setNavigationOnClickListener { onBackPressed() }
    }
}