package com.ksolutions.whatNeed.activities

import android.app.Activity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.firebase.FirestoreClass
import com.ksolutions.whatNeed.models.UserModel
import com.ksolutions.whatNeed.utils.Constants
import com.shopper.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null
    // A global variable for user details.
    private lateinit var mUserModelDetails: UserModel

    // A global variable for a user profile image URL
    private var mProfileImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        initDatePicker(my_profile_dob)

        my_profile_dob.setOnClickListener(){
            showDatePickerDialog(my_profile_dob)
        }

        setupActionBar()
        FirestoreClass().loadUserData(this@MyProfileActivity)

        profile_user_image.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            }
            else
            {
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

        btn_update.setOnClickListener {

            // Here if the image is not selected then update the other details of user.
            if (mSelectedImageFileUri != null)
            {
                uploadUserImage()
            }
            else
            {
                showProgressDialog(resources.getString(R.string.please_wait))
                // Call a function to update user details in the database.
                updateUserProfileData()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
                && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
                && data!!.data != null
        ) {
            // The uri of selection image from phone storage.
            mSelectedImageFileUri = data.data

            try {
                GlideLoader(this).loadUserPicture(
                    mSelectedImageFileUri!!,
                    profile_user_image
                )
                // Load the user image in the ImageView.
                /*Glide
                        .with(this@MyProfileActivity)
                        .load(Uri.parse(mSelectedImageFileUri.toString())) // URI of the image
                        .centerCrop() // Scale type of the image.
                        .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                        .into(profile_user_image)*/ // the view in which the image will be loaded.
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * This function will identify the result of runtime permission after the user allows or deny permission based on the unique code.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            } else {
                //Displaying another toast if permission is not granted
                    showErrorSnackBar("Oops, you just denied the permission for storage. You can also allow it from settings.", true)
            }
        }
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_my_profile_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setLogo(R.drawable.logo)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "  " + resources.getString(R.string.my_profile)
        }

        toolbar_my_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to set the existing details in UI.
     */
    fun setUserDataInUI(userModel: UserModel) {

        // Initialize the user details variable
        mUserModelDetails = userModel

        GlideLoader(this).loadUserPicture(
            userModel.image,
            profile_user_image
        )

        /*Glide
                .with(this@MyProfileActivity)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(profile_user_image)*/

        et_name.setText(userModel.name)
        et_email.setText(userModel.email)
        my_profile_dob.setText(userModel.dob)
        if (userModel.mobile != "") {
            et_mobile.setText(userModel.mobile.toString())
        }
    }

    /**
     * A function for user profile image selection from phone storage.
     */
    /*private fun showImageChooser() {
        // An intent for launching the image selection of phone storage.
        val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // Launches the image selection of phone storage using the constant code.
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }*/

    /**
     * A function to upload the selected user image to firebase cloud storage.
     */
    private fun uploadUserImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().uploadImageToCloudStorage(
            this,
            mSelectedImageFileUri,
            Constants.USER_PROFILE_IMAGE
        )

        /*if (mSelectedImageFileUri != null) {

            //getting the storage reference
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child("User Image").child(
                    "USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(
                            mSelectedImageFileUri
                    )
            )

            //adding the file to reference
            sRef.putFile(mSelectedImageFileUri!!)
                    .addOnSuccessListener { taskSnapshot ->
                        // The image upload is success
                        Log.e(
                                "Firebase Image URL",
                                taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                        )
                        showErrorSnackBar(taskSnapshot.metadata!!.reference!!.downloadUrl.toString(),false)

                        // Get the downloadable url from the task snapshot
                        taskSnapshot.metadata!!.reference!!.downloadUrl
                                .addOnSuccessListener { uri ->
                                    Log.e("Downloadable Image URL", uri.toString())

                                    // assign the image url to the variable.
                                    mProfileImageURL = uri.toString()

                                    // Call a function to update user details in the database.
                                    updateUserProfileData()
                                }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                                this@MyProfileActivity,
                                exception.message,
                                Toast.LENGTH_LONG
                        ).show()

                        hideProgressDialog()
                    }
        }*/
    }

    /**
     * A function to update the user profile details into the database.
     */
    fun updateUserProfileData() {

        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserModelDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (et_name.text.toString() != mUserModelDetails.name) {
            userHashMap[Constants.NAME] = et_name.text.toString()
        }

        if (et_email.text.toString() != mUserModelDetails.email.toString()) {
            userHashMap[Constants.EMAIL] = et_email.text.toString()
        }

        if(my_profile_dob.text.toString() != mUserModelDetails.dob.toString()){
            userHashMap[Constants.DOB] = my_profile_dob.text.toString()
        }

        // Update the data in the database.
        FirestoreClass().updateUserProfileData(this@MyProfileActivity, userHashMap)
    }
    // END

    /**
     * A function to notify the user profile is updated successfully.
     */
    fun profileUpdateSuccess() {
        hideProgressDialog()
        showErrorSnackBar("Profile has been Updated successfully",false)
        setResult(Activity.RESULT_OK)

        Handler().postDelayed({
            finish()
        },2500)
    }

    fun imageUploadSuccess(imageURL: String) {
        // Initialize the global image url variable.
        mProfileImageURL = imageURL
        updateUserProfileData()
    }
}