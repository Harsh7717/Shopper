package com.ksolutions.whatNeed.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.firebase.FirestoreClass
import com.ksolutions.whatNeed.models.Product
import com.ksolutions.whatNeed.utils.Constants
import com.shopper.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_add_business.*
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_product.*
import java.io.IOException

class AddProductActivity : BaseActivity(), View.OnClickListener {

    private var mSelectedImageFileUri: Uri? = null
    private var mBusinessId: String = ""
    private var mProductImageURL: String = ""
    private var businessCategory: String = ""
    private var mProductId: String = ""
    private var businessTitle: String = ""

    private var businessLatitude: Double = 0.0
    private var businessLongitude: Double = 0.0
    private var businessPostalCode: String = ""

    lateinit var productDetails: Product


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        setupActionBar()
        btn_save_product.setOnClickListener(this)
        tv_add_product_image.setOnClickListener(this)

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
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_add_product_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_add_product_activity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {

            R.id.tv_add_product_image -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    Constants.showImageChooser(this@AddProductActivity)
                } else {
                    /*Requests permissions to be granted to this application. These permissions
                     must be requested in your manifest, they should not be granted to your app,
                     and they should have protection level*/
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                    )
                }
            }

            R.id.btn_save_product ->{
                when {
                    et_product_title.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                        showErrorSnackBar("Please Provide title of Product", true)
                    }
                    et_product_description.text.isNullOrEmpty() -> {
                        showErrorSnackBar("Please provide a short description of the Product", true)
                    }
                    et_product_price.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please assign a price to the product", Toast.LENGTH_SHORT)
                            .show()
                        showErrorSnackBar("Please assign a price to the product", true)
                    }
                    et_product_quantity.text.isNullOrEmpty() ->{
                        showErrorSnackBar("Please put some quantity to the Product", true)
                    }
                    else -> {
                        if(mSelectedImageFileUri != null)
                        {
                            uploadProductImage()
                        }
                        else
                        {
                            showProgressDialog("Uploading the details")
                            uploadProductDetails()
                        }
                    }
                }
            }
        }
    }

    private fun uploadProductImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().uploadImageToCloudStorage(
            this@AddProductActivity,
            mSelectedImageFileUri,
            Constants.PRODUCT_IMAGE
        )
    }

    fun imageUploadSuccess(imageURL: String) {

        // Initialize the global image url variable.
        mProductImageURL = imageURL
        uploadProductDetails()
    }

    private fun uploadProductDetails() {

        // Get the logged in username from the SharedPreferences that we have stored at a time of login.
        val username =
            this.getSharedPreferences(Constants.SHOPPER_PREFERENCES, Context.MODE_PRIVATE)
                .getString(Constants.LOGGED_IN_USERNAME, "")!!

        // Here we get the text from editText and trim the space
        if(mProductId == "")
        {
                productDetails = Product(
                FirestoreClass().getCurrentUserID(),
                mProductImageURL,
                et_product_title.text.toString().trim { it <= ' ' },
                et_product_description.text.toString().trim { it <= ' ' },
                et_product_price.text.toString().trim { it <= ' ' },
                et_product_quantity.text.toString().trim { it <= ' ' },
                businessTitle,
                mBusinessId,
                businessLatitude,
                businessLongitude,
                businessPostalCode
            )
            FirestoreClass().uploadProductDetails(this@AddProductActivity, productDetails, businessCategory, mBusinessId)
        }
        /*else
        {
            val businessDetailsHash = HashMap<String, Any>()
            category = mBusinessDetails.category

            if (mBusinessImageURL.isNotEmpty() && mBusinessImageURL != mBusinessDetails.image) {
                businessDetailsHash[Constants.IMAGE] = mBusinessImageURL
            }

            if (et_business_title.text.toString() != mBusinessDetails.title) {
                businessDetailsHash[Constants.TITLE] = et_business_title.text.toString()
            }

            if (et_business_address.text.toString() != mBusinessDetails.address) {
                businessDetailsHash[Constants.ADDRESS] = et_business_address.text.toString()
            }

            if(et_business_owner_name.text.toString() != mBusinessDetails.owner_name){
                businessDetailsHash[Constants.OWNER_NAME] = et_business_owner_name.text.toString()
            }

            if(et_business_owner_contact.text.toString() != mBusinessDetails.owner_contact){
                businessDetailsHash[Constants.OWNER_CONTACT] = et_business_owner_contact.text.toString()
            }

            if(et_business_reg_no.text.toString() != mBusinessDetails.reg_no){
                businessDetailsHash[Constants.REG_NO] = et_business_reg_no.text.toString()
            }

            if(et_business_location.text.toString() != mBusinessDetails.location){
                businessDetailsHash[Constants.LOCATION] = et_business_location.text.toString()
            }

            if(mLatitude != mBusinessDetails.latitude && mLatitude != 0.0) {
                businessDetailsHash[Constants.LATITUDE] = mLatitude
            }

            if(mLongitude != mBusinessDetails.longitude && mLongitude != 0.0) {
                businessDetailsHash[Constants.LONGITUDE] = mLongitude
            }
            //showProgressDialog(category)
            FirestoreClass().updateBusinessDetails(this, businessDetailsHash, category, mBusinessId)
        }*/
    }

    fun productUpdateSuccess() {
        hideProgressDialog()
        showErrorSnackBar("Product Details has been Updated successfully",false)
        setResult(Activity.RESULT_OK)

        Handler().postDelayed({
            startActivity(Intent(this,MainActivity::class.java))
        },1500)
    }

    fun productUploadSuccess(productId: String)
    {
        hideProgressDialog()
        val realDbRefer = FirebaseDatabase.getInstance()

        realDbRefer.getReference(Constants.PRODUCTS).child(productId).setValue(productDetails)
        showErrorSnackBar("Product has been Uploaded Successfully",false)

        /*Toast.makeText(
                this@AddBusiness,
                resources.getString(R.string.product_uploaded_success_message),
                Toast.LENGTH_SHORT
        ).show()*/
        Handler().postDelayed({
            startActivity(Intent(this,ProductActivity::class.java))
        },1500)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            mSelectedImageFileUri = data.data!!
            try {

                GlideLoader(this@AddProductActivity).loadProductPicture(
                    mSelectedImageFileUri!!,
                    iv_add_product_image
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
