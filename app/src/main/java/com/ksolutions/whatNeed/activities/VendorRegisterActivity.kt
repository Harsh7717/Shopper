package com.ksolutions.whatNeed.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.firebase.FirestoreClass
import com.ksolutions.whatNeed.models.VendorInfoModel
import kotlinx.android.synthetic.main.activity_vendor_register.*

class VendorRegisterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_register)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()
        btn_vendor_register.setOnClickListener(){
            registerVendor()
        }
    }

    private fun registerVendor() {
        val first_name: String = et_vendor_register_first_name.text.toString().trim { it <= ' ' }
        val last_name: String = et_vendor_register_last_name.text.toString().trim { it <= ' ' }
        val selling_item: String = et_vendor_register_item_name.text.toString().trim { it <= ' ' }
        val phone: String = et_vendor_register_mobile.text.toString()

        if (validateForm(first_name, phone, selling_item))
        {
            showProgressDialog("Please Wait....")
            val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

            val vendorInfoModel = VendorInfoModel(
                first_name, last_name, phone, selling_item
            )

            // call the registerUser function of FirestoreClass to make an entry in the database.
            FirestoreClass().registerVendor(this, vendorInfoModel)
        }
    }

    private fun validateForm(name: String, phone: String, sellingItem: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter first name.",true)
                false
            }
            TextUtils.isEmpty(sellingItem) -> {
                showErrorSnackBar("Please tell us, what are you selling?.",true)
                false
            }
            TextUtils.isEmpty(phone) ->{
                showErrorSnackBar("Please enter Mobile Number.",true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_vendor_register_activity)
        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_vendor_register_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun vendorRegisterSuccess() {
        showErrorSnackBar("You are Successfully Registered with us..",false)
        Handler().postDelayed({
            startActivity(Intent(this,VendorsMainActivity::class.java))
            finish()
        },1500)
    }
}