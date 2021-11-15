package com.ksolutions.whatNeed.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler().postDelayed({
            val currentUserID = FirestoreClass().getCurrentUserID()
            if (currentUserID.isNotEmpty())
            {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            else
            {
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            }
        },2500)
    }
}