package com.ksolutions.shopper.activities

import android.content.res.Resources
import android.os.Bundle
import android.view.WindowManager
import android.widget.ArrayAdapter
import com.ksolutions.shopper.R
import kotlinx.android.synthetic.main.activity_business_category.*
import java.util.*
import kotlin.collections.ArrayList


class BusinessCategoryActivity : BaseActivity() {

    var categoryList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_category)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setupActionBar()
        categoryList = ArrayList<String>(listOf(*resources.getStringArray(R.array.shopsCategory)))
        val categoryAdapter = ArrayAdapter(this, R.layout.drop_down_item, categoryList)
        category_drop_down.setAdapter(categoryAdapter)
        category_drop_down.threshold = 1
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_business_category_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setLogo(R.drawable.logo)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "  " + resources.getString(R.string.business_with_us)
        }

        toolbar_business_category_activity.setNavigationOnClickListener { onBackPressed() }
    }
}