package com.ksolutions.whatNeed.activities

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.firebase.FirestoreClass
import com.ksolutions.whatNeed.models.Product
import com.ksolutions.whatNeed.adapter.MyProductListAdapter
import com.ksolutions.whatNeed.utils.CompareDist
import com.ksolutions.whatNeed.utils.MySuggestionProvider
import com.ksolutions.whatNeed.utils.PublicValues
import kotlinx.android.synthetic.main.activity_searchable.*

class SearchableActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchable)
        setupActionBar()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                SearchRecentSuggestions(this, MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE)
                    .saveRecentQuery(query, null)
                showProgressDialog("Searching")
                FirestoreClass().getProductListForSearch(this, query, PublicValues.userPostalCode)
                //doMySearch(query)
            }
        }
    }

    fun successFilteredProductList(productList: ArrayList<Product>)
    {
        hideProgressDialog()
        if(productList.size > 0)
        {
            productList.sortWith(CompareDist)
            rv_home_my_product.visibility = View.VISIBLE
            tv_home_no_product_found.visibility = View.GONE
            //noBusinessHome.visibility = View.GONE

            rv_home_my_product.layoutManager = GridLayoutManager(this, 1)
            rv_home_my_product.setHasFixedSize(true)

            val adapter = MyProductListAdapter(this, productList)
            rv_home_my_product.adapter = adapter
        }
        else
        {
            tv_home_no_product_found.visibility = View.VISIBLE
            rv_home_my_product.visibility = View.GONE
        }
    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_searchable_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_searchable_activity.setNavigationOnClickListener { onBackPressed() }
    }
}