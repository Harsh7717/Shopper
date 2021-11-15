package com.ksolutions.whatNeed.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.firebase.FirestoreClass
import com.ksolutions.whatNeed.models.Product
import com.ksolutions.whatNeed.adapter.MyProductListAdapter
import com.ksolutions.whatNeed.utils.Constants
import kotlinx.android.synthetic.main.activity_product.*

class ProductActivity : BaseActivity() {

    private var mBusinessId: String = ""
    private var businessCategory: String = ""
    private var businessTitle: String = ""
    private var businessLatitude: Double = 0.0
    private var businessLongitude: Double = 0.0
    private var businessPostalCode: String = ""
    private var mUserId:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_BUSINESS_ID)) {
            mBusinessId = intent.getStringExtra(Constants.EXTRA_BUSINESS_ID)!!
            Log.i("Business Id", mBusinessId)
        }
        if (intent.hasExtra(Constants.CATEGORY)) {
            businessCategory = intent.getStringExtra(Constants.CATEGORY)!!
            Log.i("Category", businessCategory)
        }
        if (intent.hasExtra(Constants.BUSINESS_NAME)) {
            businessTitle = intent.getStringExtra(Constants.BUSINESS_NAME)!!
            Log.i("Business Title", businessTitle)
        }
        if(intent.hasExtra(Constants.LATITUDE)) {
            businessLatitude = intent.getDoubleExtra(Constants.LATITUDE, 0.0)
        }
        if(intent.hasExtra(Constants.LONGITUDE)) {
            businessLongitude = intent.getDoubleExtra(Constants.LONGITUDE, 0.0)
        }
        if(intent.hasExtra(Constants.POSTAL_CODE)){
            businessPostalCode = intent.getStringExtra(Constants.POSTAL_CODE)!!
        }
        if(intent.hasExtra(Constants.USER_ID)){
            mUserId = intent.getStringExtra(Constants.USER_ID)!!
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(mUserId == FirestoreClass().getCurrentUserID())
        {
            val inflater = menuInflater
            inflater.inflate(R.menu.add_menu, menu)
            return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_add) {
            val intent = Intent(this, AddProductActivity::class.java)
            intent.putExtra(Constants.EXTRA_BUSINESS_ID, mBusinessId)
            intent.putExtra(Constants.CATEGORY, businessCategory)
            intent.putExtra(Constants.BUSINESS_NAME, businessTitle)
            intent.putExtra(Constants.LATITUDE, businessLatitude)
            intent.putExtra(Constants.LONGITUDE, businessLongitude)
            intent.putExtra(Constants.POSTAL_CODE, businessPostalCode)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        getProductListFromFireStore()
    }

    private fun getProductListFromFireStore() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getProductList(this, mBusinessId, businessCategory)
    }

    fun successProductListFromFireStore(productList: ArrayList<Product>)
    {
        hideProgressDialog()

        if (productList.size > 0)
        {
            rv_my_product_items.visibility = View.VISIBLE
            tv_no_product_found.visibility = View.GONE

            rv_my_product_items.layoutManager = LinearLayoutManager(this)
            rv_my_product_items.setHasFixedSize(true)

            val adapter = MyProductListAdapter(this, productList)
            rv_my_product_items.adapter = adapter


            /*rv_my_business.visibility = View.VISIBLE
            tv_no_business_found.visibility = View.GONE

            rv_my_business.layoutManager = LinearLayoutManager(activity)
            rv_my_business.setHasFixedSize(true)

            val adapterProducts = MyBusinessListAdapter(requireActivity(), BusinessList, this)
            rv_my_business.adapter = adapterProducts*/
        }
        else
        {
            rv_my_product_items.visibility = View.GONE
            tv_no_product_found.visibility = View.VISIBLE
        }
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_product_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_product_activity.setNavigationOnClickListener { onBackPressed() }
    }
}