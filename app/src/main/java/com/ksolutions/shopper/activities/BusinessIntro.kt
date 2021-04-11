package com.ksolutions.shopper.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.ksolutions.shopper.R
import kotlinx.android.synthetic.main.activity_business_intro.*

class BusinessIntro : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_intro)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()

        btn_business_intro.setOnClickListener(){
            startActivity(Intent(this,BusinessCategoryActivity::class.java))
        }
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_business_intro_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setLogo(R.drawable.logo)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "  " + resources.getString(R.string.business_with_us)
        }

        toolbar_business_intro_activity.setNavigationOnClickListener { onBackPressed() }
    }
}