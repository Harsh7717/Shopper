package com.ksolutions.whatNeed.activities

import android.content.Intent
import android.os.Bundle
import android.view.*
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.firebase.FirestoreClass
import com.ksolutions.whatNeed.models.BusinessModel
import com.ksolutions.whatNeed.utils.Constants
import com.shopper.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_business_info.*


class BusinessInfo : BaseActivity() {

    private var mBusinessId: String = ""
    private var businessCategory: String = ""
    private var mUserId: String = ""
    private var currentUserId: String = ""
    private var businessTitle: String = ""
    private var businessLatitude: Double = 0.0
    private var businessLongitude: Double = 0.0
    private var businessPostalCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_info)

        if (intent.hasExtra(Constants.EXTRA_BUSINESS_ID)) {
            mBusinessId = intent.getStringExtra(Constants.EXTRA_BUSINESS_ID)!!
        }
        if (intent.hasExtra(Constants.USER_ID)) {
            mUserId = intent.getStringExtra(Constants.USER_ID)!!
        }
        if (intent.hasExtra(Constants.CATEGORY)) {
            businessCategory = intent.getStringExtra(Constants.CATEGORY)!!
        }

        setupActionBar()
        getBusinessDetails()
        currentUserId = FirestoreClass().getCurrentUserID()

        btn_product_and_services.setOnClickListener(){
            intent = Intent(this, ProductActivity::class.java)
            intent.putExtra(Constants.EXTRA_BUSINESS_ID, mBusinessId)
            intent.putExtra(Constants.CATEGORY, businessCategory)
            intent.putExtra(Constants.BUSINESS_NAME, businessTitle)
            intent.putExtra(Constants.LATITUDE, businessLatitude)
            intent.putExtra(Constants.LONGITUDE, businessLongitude)
            intent.putExtra(Constants.POSTAL_CODE, businessPostalCode)
            intent.putExtra(Constants.USER_ID, mUserId)
            startActivity(intent)
        }
        btn_business_detail_map.setOnClickListener(){
            intent = Intent(this, MapsActivity::class.java)
            intent.putExtra(Constants.LATITUDE, businessLatitude)
            intent.putExtra(Constants.LONGITUDE, businessLongitude)
            intent.putExtra(Constants.BUSINESS_NAME, businessTitle)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(mUserId == currentUserId)
        {
            val inflater = menuInflater
            inflater.inflate(R.menu.edit_menu, menu)
            return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_edit_business) {
            val intent = Intent(this, AddBusiness::class.java)
            intent.putExtra(Constants.EXTRA_BUSINESS_ID, mBusinessId)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_business_details_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_business_details_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getBusinessDetails() {

        // Show the product dialog
        showProgressDialog(resources.getString(R.string.please_wait))

        // Call the function of FirestoreClass to get the product details.
        val categoryList = ArrayList<String>(listOf(*resources.getStringArray(R.array.shopsCategory)))
        FirestoreClass().getBusinessDetails(this, mBusinessId, categoryList)
    }

    fun businessDetailsSuccess(business: BusinessModel, category: String)
    {
        hideProgressDialog()
        businessTitle = business.title
        businessLatitude = business.latitude
        businessLongitude = business.longitude
        businessPostalCode = business.postal_code

        GlideLoader(this).loadProductPicture(
            business.image,
            iv_business_detail_image
        )

        tv_business_details_category.text = category
        tv_business_details_title.text = business.title
        tv_business_details_address.text = business.address
        tv_business_details_owner_name.text = business.owner_name
        tv_business_details_owner_contact.text = business.owner_contact
        tv_business_details_reg_no.text = business.reg_no
        tv_business_details_location.text = business.location
    }
}