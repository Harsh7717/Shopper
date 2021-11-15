package com.ksolutions.whatNeed.activities


import android.Manifest
import com.ksolutions.whatNeed.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.ksolutions.whatNeed.firebase.FirestoreClass
import com.ksolutions.whatNeed.models.BusinessModel
import com.ksolutions.whatNeed.utils.Constants
import com.ksolutions.whatNeed.utils.GetAddressFromLatLng
import com.shopper.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_add_business.*
import java.io.IOException


class AddBusiness : BaseActivity(), View.OnClickListener {

    private var mSelectedImageFileUri: Uri? = null
    private var mBusinessId: String = ""
    private lateinit var mBusinessDetails: BusinessModel

    // A global variable for uploaded product image URL.
    private var mBusinessImageURL: String = ""

    private var mLatitude: Double = 0.0 // A variable which will hold the latitude value.
    private var mLongitude: Double = 0.0 // A variable which will hold the longitude value.

    private lateinit var category:String
    private var postalCode: String = ""


    private lateinit var mFusedLocationClient: FusedLocationProviderClient // A fused location client variable which is further user to get the user's current location


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_business)

        setupActionBar()
        et_business_location.setOnClickListener(this)
        btn_save_business.setOnClickListener(this)
        iv_add_business_icon.setOnClickListener(this)


        if(intent.hasExtra("category"))
        {
            category = intent.getStringExtra("category")!!
        }
        if (intent.hasExtra(Constants.EXTRA_BUSINESS_ID))
        {
            mBusinessId = intent.getStringExtra(Constants.EXTRA_BUSINESS_ID)!!
            Log.i("Business Id", mBusinessId)
            getBusinessDetails()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.et_business_location -> {
                if (!isLocationEnabled()) {
                    Toast.makeText(
                            this,
                            "Your location provider is turned off. Please turn it on.",
                            Toast.LENGTH_SHORT
                    ).show()

                    showErrorSnackBar("Your location provider is turned off. Please turn it on.", true)

                    // This will redirect you to settings from where you need to turn on the location provider.
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {

                    showProgressDialog("Detecting Location")
                    // For Getting current location of user please have a look at below link for better understanding
                    // https://www.androdocs.com/kotlin/getting-current-location-latitude-longitude-in-android-using-kotlin.html
                    Dexter.withActivity(this)
                            .withPermissions(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            .withListener(object : MultiplePermissionsListener {
                                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                    if (report!!.areAllPermissionsGranted()) {
                                        requestNewLocationData()
                                    }
                                }

                                override fun onPermissionRationaleShouldBeShown(
                                        permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                                        token: PermissionToken?
                                ) {
                                    showRationalDialogForPermissions()
                                }
                            }).onSameThread()
                            .check()
                }
            }

            R.id.iv_add_business_icon -> {
                if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        == PackageManager.PERMISSION_GRANTED
                ) {
                    Constants.showImageChooser(this@AddBusiness)
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

            R.id.btn_save_business -> {
                when {
                    et_business_title.text.isNullOrEmpty() -> {
                        //Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                        showErrorSnackBar("Please Provide title of your Business", true)
                    }
                    et_business_address.text.isNullOrEmpty() -> {
                        showErrorSnackBar("Please provide the address of your business", true)
                    }
                    et_business_location.text.isNullOrEmpty() -> {
                        //Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT)
                        //       .show()
                        showErrorSnackBar("Please select the location", true)
                    }
                    et_business_owner_name.text.isNullOrEmpty() -> {
                        showErrorSnackBar("Please Enter the Name of the Owner", true)
                    }
                    et_business_owner_contact.text.isNullOrEmpty() -> {
                        showErrorSnackBar("Please Provide your contact Number", true)
                    }
                    else -> {
                        if (mSelectedImageFileUri != null) {
                            uploadBusinessImage()
                        } else {
                            showProgressDialog("Uploading the details")
                            uploadBusinessDetails()
                        }
                    }
                }
            }
            }
        }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_add_business_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_add_business_activity.setNavigationOnClickListener { onBackPressed() }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (!Places.isInitialized()) {
            Places.initialize(
                    this@AddBusiness,
                    resources.getString(R.string.google_maps_api_key)
            )
        }
    }

    private fun getBusinessDetails() {

        // Show the product dialog
        showProgressDialog(resources.getString(R.string.please_wait))

        // Call the function of FirestoreClass to get the product details.
        val categoryList = ArrayList<String>(listOf(*resources.getStringArray(R.array.shopsCategory)))
        FirestoreClass().getBusinessDetails(this, mBusinessId, categoryList)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        )
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton(
                    "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 100

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback()
    {
        override fun onLocationResult(locationResult: LocationResult)
        {
            val mLastLocation: Location = locationResult.lastLocation
            mLatitude = mLastLocation.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")

            val addressTask = GetAddressFromLatLng(this@AddBusiness, mLatitude, mLongitude)

            addressTask.setAddressListener(object : GetAddressFromLatLng.AddressListener
            {
                override fun onAddressFound(address: String?)
                {
                    Log.e("Address ::", "" + address)
                    if (address != null)
                    {
                        val addressData = address.split(", ") as MutableList<String>
                        postalCode = addressData[addressData.lastIndex]
                        addressData.remove(addressData.last())
                        et_business_location.setText(addressData.joinToString(", "))
                    }
                    hideProgressDialog()// Address is set to the edittext
                }
                override fun onError()
                {
                    Log.e("Get Address ::", "Something is wrong...")
                }
            })
            addressTask.getAddress()
        }
    }

    private fun uploadBusinessImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().uploadImageToCloudStorage(
                this@AddBusiness,
                mSelectedImageFileUri,
                Constants.BUSINESS_IMAGE
        )
    }

    fun imageUploadSuccess(imageURL: String) {

        // Initialize the global image url variable.
        mBusinessImageURL = imageURL
        uploadBusinessDetails()
    }

    private fun uploadBusinessDetails() {

        // Get the logged in username from the SharedPreferences that we have stored at a time of login.
        val username =
                this.getSharedPreferences(Constants.SHOPPER_PREFERENCES, Context.MODE_PRIVATE)
                        .getString(Constants.LOGGED_IN_USERNAME, "")!!

        // Here we get the text from editText and trim the space
        if(mBusinessId == "")
        {
            val businessDetails = BusinessModel(
                    FirestoreClass().getCurrentUserID(),
                    et_business_title.text.toString().trim { it <= ' ' },
                    mBusinessImageURL,
                    et_business_owner_name.text.toString().trim { it <= ' ' },
                    et_business_address.text.toString().trim { it <= ' ' },
                    et_business_owner_contact.text.toString().trim { it <= ' ' },
                    et_business_reg_no.text.toString().trim { it <= ' ' },
                    et_business_location.text.toString().trim { it <= ' ' },
                    postalCode,
                    mLatitude,
                    mLongitude,
                    category
            )
            FirestoreClass().uploadBusinessDetails(this@AddBusiness, businessDetails, category)
        }
        else
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

            if(postalCode != mBusinessDetails.postal_code){
                businessDetailsHash[Constants.POSTAL_CODE] = postalCode
            }
            //showProgressDialog(category)
            FirestoreClass().updateBusinessDetails(this, businessDetailsHash, category, mBusinessId)
        }
    }

    fun businessUpdateSuccess() {
        hideProgressDialog()
        showErrorSnackBar("Your Business Details has been Updated successfully", false)
        setResult(Activity.RESULT_OK)

        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
        }, 1500)
    }

    fun businessUploadSuccess() {
        hideProgressDialog()
        showErrorSnackBar(resources.getString(R.string.product_uploaded_success_message), false)

        /*Toast.makeText(
                this@AddBusiness,
                resources.getString(R.string.product_uploaded_success_message),
                Toast.LENGTH_SHORT
        ).show()*/
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
        }, 1500)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
                && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
                && data!!.data != null
        ) {

            // Replace the add icon with edit icon once the image is selected.
            iv_add_business_icon.setImageDrawable(
                    ContextCompat.getDrawable(
                            this@AddBusiness,
                            R.drawable.ic_baseline_edit_24
                    )
            )

            // The uri of selection image from phone storage.
            mSelectedImageFileUri = data.data!!

            try {

                GlideLoader(this@AddBusiness).loadProductPicture(
                        mSelectedImageFileUri!!,
                        iv_business_image
                )
                showProgressDialog("Recognizing Title form Image")
                recognizeTextFromImage()
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

   private fun recognizeTextFromImage()
   {
       val image: InputImage
       try {
           image = InputImage.fromFilePath(this, mSelectedImageFileUri!!)
           recognizeText(image)
       } catch (e: IOException) {
           e.printStackTrace()
       }
   }

    private fun recognizeText(image: InputImage)
    {
        val recognizer = TextRecognition.getClient()

        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                hideProgressDialog()
                et_business_title.setText(visionText.text)

                for (block in visionText.textBlocks)
                {
                    val boundingBox = block.boundingBox
                    val cornerPoints = block.cornerPoints
                    val text = block.text

                    for (line in block.lines)
                    {
                        // ...
                        for (element in line.elements)
                        {
                            // ...
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
            }
    }


    fun businessDetailsSuccess(business: BusinessModel, category: String)
    {
        mBusinessDetails = business
        hideProgressDialog()
        //showProgressDialog(business.category)

        GlideLoader(this).loadProductPicture(
                business.image,
                iv_business_image
        )

        et_business_title.setText(business.title)
        et_business_owner_name.setText(business.owner_name)
        et_business_owner_contact.setText(business.owner_contact)
        et_business_reg_no.setText(business.reg_no)
        et_business_address.setText(business.address)
        et_business_location.setText(business.location)
    }
}