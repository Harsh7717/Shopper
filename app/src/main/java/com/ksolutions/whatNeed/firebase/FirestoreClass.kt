package com.ksolutions.whatNeed.firebase

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ksolutions.whatNeed.activities.*
import com.ksolutions.whatNeed.models.BusinessModel
import com.ksolutions.whatNeed.models.Product
import com.ksolutions.whatNeed.models.UserModel
import com.ksolutions.whatNeed.models.VendorInfoModel
import com.ksolutions.whatNeed.ui.fragments.business.BusinessFragment
import com.ksolutions.whatNeed.ui.fragments.home.HomeFragment
import com.ksolutions.whatNeed.utils.Constants

class FirestoreClass{

    private val mFireStore = FirebaseFirestore.getInstance()
    private var currentUserRef: DatabaseReference?=null
    var productId: String = ""

    fun registerUser(activity: RegisterActivity, userModelInfo: UserModel)
    {
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(getCurrentUserID())
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(userModelInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }
    fun registerVendor(activity: VendorRegisterActivity, vendorInfoModel: VendorInfoModel)
    {
        val rootRef = FirebaseDatabase.getInstance().reference
        val vendorKeyRef = rootRef.child(Constants.VENDOR_INFO_REF).child(getCurrentUserID())

        vendorKeyRef.setValue(vendorInfoModel)
        activity.vendorRegisterSuccess()
    }
    fun loadUserData(activity: Activity)
    {
        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
                // The document id to get the Fields of user.
                .document(getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->
                    Log.e(
                            activity.javaClass.simpleName, document.toString()
                    )

                    // Here we have received the document snapshot which is converted into the User Data model object.
                    val loggedInUser = document.toObject(UserModel::class.java)

                    // Here call a function of base activity for transferring the result to it.
                    if(loggedInUser!=null)
                    {
                        when (activity) {
                            is SignInActivity -> {
                                activity.signInSuccess(loggedInUser)
                            }
                            is MainActivity -> {
                                activity.updateNavigationUserDetails(loggedInUser)
                            }
                            is MyProfileActivity -> {
                                activity.setUserDataInUI(loggedInUser)
                            }
                        }
                    }
                }
            .addOnFailureListener { e ->
                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }

    /**
     * A function to update the user profile data into the database.
     */
    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>)
    {
        mFireStore.collection(Constants.USERS) // Collection Name
                .document(getCurrentUserID()) // Document ID
                .update(userHashMap) // A hashmap of fields which are to be updated.
                .addOnSuccessListener {
                    // Notify the success result.
                    activity.profileUpdateSuccess()
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(
                            activity.javaClass.simpleName,
                            "Error while creating a board.",
                            e
                    )
                }
    }

    fun updateBusinessDetails(activity: AddBusiness, businessInfoHash: HashMap<String, Any>, category:String, mBusinessId:String)
    {
        mFireStore.collection(Constants.BUSINESS)
                .document(Constants.BUSINESS_CAT)
                .collection(category)
                .document(mBusinessId)
                .update(businessInfoHash)
                .addOnSuccessListener {
                    // Notify the success result.
                    activity.businessUpdateSuccess()
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(
                            activity.javaClass.simpleName,
                            "Error while creating a board.",
                            e
                    )
                }
    }

    public fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    fun uploadBusinessDetails(activity: AddBusiness, businessInfo: BusinessModel, category:String)
    {
        mFireStore.collection(Constants.BUSINESS)
                .document(Constants.BUSINESS_CAT)
                .collection(category)
                .document()
                .set(businessInfo, SetOptions.merge())
                .addOnSuccessListener {
                    activity.businessUploadSuccess()
                }
                .addOnFailureListener { e ->

                    activity.hideProgressDialog()

                    Log.e(
                            activity.javaClass.simpleName,
                            "Error while uploading the product details.",
                            e
                    )
                }
    }

    fun uploadImageToCloudStorage(activity: Activity, imageFileURI: Uri?, imageType: String)
    {
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(imageType).child(
                imageType + System.currentTimeMillis() + "."
                        + Constants.getFileExtension(
                        activity,
                        imageFileURI
                )
        )

        //adding the file to reference
        sRef.putFile(imageFileURI!!)
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success
                    Log.e(
                            "Firebase Image URL",
                            taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    // Get the downloadable url from the task snapshot
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener { uri ->
                                Log.e("Downloadable Image URL", uri.toString())

                                // Here call a function of base activity for transferring the result to it.
                                when (activity) {
                                    is AddBusiness -> {
                                        activity.imageUploadSuccess(uri.toString())
                                    }
                                    is MyProfileActivity -> {
                                        activity.imageUploadSuccess(uri.toString())
                                    }
                                    is AddProductActivity -> {
                                        activity.imageUploadSuccess(uri.toString())
                                    }
                                }
                            }
                }
                .addOnFailureListener { exception ->

                    // Hide the progress dialog if there is any error. And print the error in log.
                    when (activity) {
                        is MyProfileActivity -> {
                            activity.hideProgressDialog()
                        }

                        is AddBusiness -> {
                            activity.hideProgressDialog()
                        }
                    }

                    Log.e(
                            activity.javaClass.simpleName,
                            exception.message,
                            exception
                    )
                }
    }

    fun getBusinessList(fragment: Fragment, categoryList: ArrayList<String>, filterKey:String)
    {
        val businessList: ArrayList<BusinessModel> = ArrayList()
        val compareAttribute:String = if(filterKey == getCurrentUserID()){
            Constants.USER_ID
        }
        else{
            Constants.POSTAL_CODE
        }

        val businessRef : CollectionReference = mFireStore.collection(Constants.BUSINESS)
        for (category in categoryList)
        {
            //mFireStore.collection(Constants.BUSINESS)
            businessRef.document("Business Categories").collection(category)
                .whereEqualTo(compareAttribute, filterKey)
                .get() // Will get the documents snapshots.
                .addOnSuccessListener { document ->

                    // Here we get the list of boards in the form of documents.
                    Log.e("Products List", document.documents.toString())

                    // A for loop as per the list of documents to convert them into Products ArrayList.
                    for (i in document.documents)
                    {
                        val business = i.toObject(BusinessModel::class.java)
                        business!!.business_id = i.id  //receiving the id of the business and storing it to a variable

                        businessList.add(business)
                    }

                    when (fragment) {
                        is BusinessFragment -> {
                            fragment.successBusinessListFromFireStore(businessList)
                        }
                        is HomeFragment -> {
                            fragment.successBusinessListFromFireStore(businessList)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Hide the progress dialog if there is any error based on the base class instance.
                    when (fragment) {
                        is BusinessFragment -> {
                            fragment.hideProgressDialog()
                        }
                        is HomeFragment -> {
                            fragment.hideProgressDialog()
                        }
                    }
                    Log.e("Get Product List", "Error while getting product list.", e)
                }
        }
    }

    fun getBusinessDetails(activity: Activity, businessId: String, categoryList: ArrayList<String>)
    {
        val businessRef: CollectionReference = mFireStore.collection(Constants.BUSINESS)
        var found:Boolean = false
        for (category in categoryList)
        {
            businessRef.document(Constants.BUSINESS_CAT).collection(category)
                .document(businessId)
                .get() // Will get the document snapshots.
                .addOnSuccessListener { document ->

                    // Here we get the product details in the form of document.
                    Log.e(activity.javaClass.simpleName, document.toString())

                    if(document.exists())
                    {
                        found = true
                        val business = document.toObject(BusinessModel::class.java)!!
                        when(activity){
                            is AddBusiness ->{
                                activity.businessDetailsSuccess(business, category)
                            }
                            is BusinessInfo ->{
                                activity.businessDetailsSuccess(business, category)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    when(activity){
                        is AddBusiness ->{
                            activity.hideProgressDialog()
                        }
                        is BusinessInfo ->{
                            activity.hideProgressDialog()
                        }
                    }
                    Log.e(activity.javaClass.simpleName, "Error while getting the product details.", e)
                }
            if(found) break
        }
    }

    fun uploadProductDetails(activity: AddProductActivity, productDetails: Product, businessCategory: String, mBusinessId: String)
    {
        var productRef = mFireStore.collection(Constants.BUSINESS)
            .document(Constants.BUSINESS_CAT)
            .collection(businessCategory)
            .document(mBusinessId)
            .collection(Constants.PRODUCTS)
            .document()

        productId = productRef.id

        productRef
            .set(productDetails, SetOptions.merge())
            .addOnSuccessListener {
                uploadSeparateProduct(productId, productDetails)
                activity.productUploadSuccess(productId)
            }
            .addOnFailureListener { e ->

                activity.hideProgressDialog()

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while uploading the product details.",
                    e
                )
            }
    }

    private fun uploadSeparateProduct(ID: String, productDetails: Product)
    {
        mFireStore.collection(Constants.PRODUCTS)
                .document(ID)
                .set(productDetails, SetOptions.merge())
                .addOnSuccessListener {
                }
                .addOnFailureListener { e ->
                }
    }

    fun getProductList(activity: ProductActivity, mBusinessId: String, businessCategory: String)
    {
        val productList: ArrayList<Product> = ArrayList()
        val businessRef: CollectionReference = mFireStore.collection(Constants.BUSINESS)

        businessRef
            .document(Constants.BUSINESS_CAT)
            .collection(businessCategory)
            .document(mBusinessId)
            .collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                Log.e("Products List", document.documents.toString())

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id

                    productList.add(product)
                }

                when (activity) {
                    is ProductActivity -> {
                        activity.successProductListFromFireStore(productList)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                when (activity) {
                    is ProductActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e("Get Product List", "Error while getting product list.", e)
            }
    }

    fun getProductListForSearch(activity: SearchableActivity, query:String, postalCode: String)
    {
        val productRef : CollectionReference = mFireStore.collection(Constants.PRODUCTS)
        val productList: ArrayList<Product> = ArrayList()
            productRef.orderBy(Constants.P_TITLE).startAt(query).endAt(query+'\uf8ff')
                    .get() // Will get the documents snapshots.
                    .addOnSuccessListener { document ->

                        // Here we get the list of boards in the form of documents.
                        Log.e("Products List", document.documents.toString())

                        // A for loop as per the list of documents to convert them into Products ArrayList.
                        for (i in document.documents)
                        {
                            val product = i.toObject(Product::class.java)
                            product!!.product_id = i.id  //receiving the id of the business and storing it to a variable

                            productList.add(product)
                        }
                        when (activity) {
                            else -> {
                                activity.successFilteredProductList(productList)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Hide the progress dialog if there is any error based on the base class instance.
                        when (activity) {
                            else -> {
                                activity.hideProgressDialog()
                            }
                        }
                        Log.e("Get Product List", "Error while getting product list.", e)
                    }
    }
}