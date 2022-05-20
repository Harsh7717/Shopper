package com.ksolutions.whatNeed.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.databinding.ActivityMainBinding
import com.ksolutions.whatNeed.firebase.FirestoreClass
import com.ksolutions.whatNeed.models.UserModel
import com.ksolutions.whatNeed.navigation.FragmentUpdateCallback
import com.ksolutions.whatNeed.navigation.MainFragmentPagerAdapter
import com.ksolutions.whatNeed.ui.fragments.business.BusinessFragment
import com.ksolutions.whatNeed.ui.fragments.home.HomeFragment
import com.ksolutions.whatNeed.ui.fragments.notifications.NotificationsFragment
import com.ksolutions.whatNeed.ui.fragments.vendors.VendorsFragment
import com.ksolutions.whatNeed.utils.Constants
import com.ksolutions.whatNeed.utils.GetAddressFromLatLng
import com.ksolutions.whatNeed.utils.PublicValues
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : BaseActivity(), FragmentUpdateCallback, NavigationView.OnNavigationItemSelectedListener{

    private lateinit var mPagerAdapter: MainFragmentPagerAdapter
    private var mCurrentTabPosition = 0

    private var userLatitude: Double = 0.0 // A variable which will hold the latitude value.
    private var userLongitude: Double = 0.0 // A variable which will hold the longitude value.
    private var userPostalCode:String = ""

    lateinit var searchView: androidx.appcompat.widget.SearchView
    lateinit private var homeBusinessView: RecyclerView
    lateinit private var noBusinessHome: TextView

    private lateinit var binding: ActivityMainBinding

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationManager: LocationManager
    private lateinit var mLocationListener: LocationListener
    private var mLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ButterKnife.bind(this)
        mPagerAdapter = MainFragmentPagerAdapter(supportFragmentManager)
        mainViewPager.adapter = mPagerAdapter
        mainViewPager.offscreenPageLimit = 4

        setupActionBar()
        nav_view.setNavigationItemSelectedListener(this)

        mainBottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.navigation_dashboard -> {
                    mCurrentTabPosition = BusinessFragment().TAB_POSITION
                    mainViewPager.currentItem = mCurrentTabPosition
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifications -> {
                    mCurrentTabPosition = NotificationsFragment().TAB_POSITION
                    mainViewPager.currentItem = mCurrentTabPosition
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_vendors -> {
                    mCurrentTabPosition = VendorsFragment().TAB_POSITION
                    mainViewPager.currentItem = mCurrentTabPosition
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_home -> {
                    mCurrentTabPosition = HomeFragment().TAB_POSITION
                    mainViewPager.currentItem = mCurrentTabPosition
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })

        FirestoreClass().loadUserData(this@MainActivity)

        /*val navView: BottomNavigationView = findViewById(R.id.mainBottomNavigation)
        val navController = findNavController(R.id.main_nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.navigation_vendors, R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
                )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)*/

        //mainBottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        detectLocation()
        initializeViews()


        tv_home_location.setOnClickListener(){
            detectLocation()
        }

        searchView = findViewById<View>(R.id.home_search_view) as androidx.appcompat.widget.SearchView
        configureSearchView()

        home_search_view.setOnClickListener(){
            startActivity(Intent(this, SearchableActivity::class.java))
        }

        /*searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    showProgressDialog("Searching")
                    FirestoreClass().getProductListForHome(this@MainActivity, query, PublicValues.userPostalCode)
                }
                return false
            }// do something on text submit


            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    showErrorSnackBar(newText, false)
                }
                // do something when text changes
                return false
            }
        })*/
}

    override fun addFragment(fragment: Fragment?, tabPosition: Int) {
        mPagerAdapter!!.updateFragment(fragment!!, tabPosition)
    }

    /*private fun loadDefaultFragment()
    {
        home_search_view.visibility = View.VISIBLE
        val defaultFragment = VendorsFragment.newInstance()
        openFragment(defaultFragment)
    }

    private fun openFragment(fragment: Fragment)
    {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_nav_host_fragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }*/

    private fun initializeViews()
    {
        val homeFragment = HomeFragment.newInstance()
        val view: View? = homeFragment.view

        if (view != null) {
            homeBusinessView = view.findViewById<View>(R.id.rv_home_my_business) as androidx.recyclerview.widget.RecyclerView
            noBusinessHome = view.findViewById<View>(R.id.tv_home_no_business_found) as TextView
            //homeProductView = view.findViewById<View>(R.id.rv_home_my_product) as androidx.recyclerview.widget.RecyclerView
        }
    }


    private fun detectLocation()
    {
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Your location provider is turned off. Please turn it on.", Toast.LENGTH_SHORT).show()

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
                            if (report!!.areAllPermissionsGranted())
                            {
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

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
                .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
                .setPositiveButton("GO TO SETTINGS") { _, _ ->
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
        mLocationRequest.numUpdates = 10

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())

        mFusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->

            if(location == null) {
                val newLocation: Location = Location("flp")
                newLocation.latitude = 37.377166
                newLocation.longitude = -122.086966
                newLocation.accuracy = 3.0f

                mLocation = newLocation
            }
            else
                mLocation = location

            userLatitude = mLocation!!.latitude
            PublicValues.userLatitude = mLocation!!.latitude

            userLongitude = mLocation!!.longitude
            PublicValues.userLongitude = mLocation!!.longitude

            val addressTask = GetAddressFromLatLng(this@MainActivity, userLatitude, userLongitude)

            addressTask.setAddressListener(object : GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    Log.e("Address ::", "" + address)
                    if (address != null) {
                        val addressData = address.split(", ") as MutableList<String>
                        userPostalCode = addressData[addressData.lastIndex]
                        PublicValues.userPostalCode = userPostalCode
                        addressData.remove(addressData.last())
                        tv_home_location?.text = addressData.joinToString(", ")
                    }
                    hideProgressDialog()
                }

                override fun onError() {
                    Log.e("Get Address ::", "Something is wrong...")
                }
            })
            addressTask.getAddress()
        }
    }

    /*private val mLocationCallback = object : LocationCallback()
    {
        override fun onLocationResult(locationResult: LocationResult)
        {
            showErrorSnackBar("Still Calling",false)
            mLocation = locationResult.lastLocation
            userLatitude = mLocation!!.latitude
            PublicValues.userLatitude = mLocation!!.latitude
            //Log.e("Current Latitude", "$userLatitude")
            userLongitude = mLocation!!.longitude
            PublicValues.userLongitude = mLocation!!.longitude
            //Log.e("Current Longitude", "$userLongitude")
        }
    }

    private fun stopUpdate()
    {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }*/

    private fun isLocationEnabled(): Boolean
    {
        val locationManager: LocationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        )
    }

    /*private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_vendors -> {
                home_search_view.visibility = View.VISIBLE
                val vendorsFragment = VendorsFragment.newInstance()
                activeFragment = vendorsFragment
                openFragment(vendorsFragment)
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_home -> {
                home_search_view.visibility = View.VISIBLE
                val homeFragment = HomeFragment.newInstance()
                activeFragment = homeFragment
                openFragment(homeFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                home_search_view.visibility = View.GONE
                val notificationFragment = NotificationsFragment.newInstance()
                activeFragment = notificationFragment
                openFragment(notificationFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                home_search_view.visibility = View.GONE
                val businessFragment = BusinessFragment.newInstance()
                activeFragment = businessFragment
                openFragment(businessFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }*/


    private fun configureSearchView()
    {
        home_search_view.isSubmitButtonEnabled = true

        val txtSearch = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        txtSearch.hint = resources.getString(R.string.search_view_hint)
        txtSearch.setHintTextColor(Color.LTGRAY)
        txtSearch.setTextColor(Color.BLACK)
        txtSearch.textSize = 15F

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(false)
        }
    }

    override fun onNewIntent(intent: Intent)
    {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            home_search_view.setQuery(query, false)
        }
    }

    override fun onBackPressed()
    {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else
        {
            doubleBackToExit()
            if (!mPagerAdapter.removeFragment(mPagerAdapter.getItem(mCurrentTabPosition), mCurrentTabPosition)) {
                finish()
            }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean
    {
        when (menuItem.itemId)
        {
            R.id.nav_my_profile -> {
                startActivityForResult(
                        Intent(this@MainActivity, MyProfileActivity::class.java),
                        MY_PROFILE_REQUEST_CODE
                )
            }

            R.id.nav_sign_out -> {
                // Here sign outs the user from firebase in this device.
                FirebaseAuth.getInstance().signOut()

                // Send the user to the intro screen of the application.
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

            R.id.nav_partner -> {
                startActivity(Intent(this, BusinessIntro::class.java))
            }

            R.id.nav_vendor -> {
                val rootRef = FirebaseDatabase.getInstance().reference
                val vendorKeyRef = rootRef.child(Constants.VENDOR_INFO_REF).child(FirestoreClass().getCurrentUserID())

                val eventListener: ValueEventListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            startVendorsRegisterActivity()
                        }
                        else {
                            startVendorsMainActivity()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        showErrorSnackBar(databaseError.message,true)
                    }
                }
                vendorKeyRef.addListenerForSingleValueEvent(eventListener)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun startVendorsMainActivity()
    {
        startActivity(Intent(this, VendorsMainActivity::class.java))
    }
    private fun startVendorsRegisterActivity()
    {
        startActivity(Intent(this, VendorRegisterActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE
        ) {
            // Get the user updated details.
            FirestoreClass().loadUserData(this@MainActivity)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    fun updateNavigationUserDetails(userModel: UserModel)
    {
        // The instance of the header view of the navigation view.
        val headerView = nav_view.getHeaderView(0)

        // The instance of the user image of the navigation view.
        val navUserImage = headerView.findViewById<ImageView>(R.id.iv_user_image)

        // Load the user image in the ImageView.
        Glide
            .with(this@MainActivity)
            .load(userModel.image) // URL of the image
            .centerCrop() // Scale type of the image.
            .placeholder(R.drawable.ic_user_place_holder) // A default place holder
            .into(navUserImage) // the view in which the image will be loaded.

        // The instance of the user name TextView of the navigation view.
        val navUsername = headerView.findViewById<TextView>(R.id.tv_username)
        val navUserMobile = headerView.findViewById<TextView>(R.id.navigation_mobile)

        val nameParts = userModel.name.split(' ')
        // Set the user name
        navUsername.text = "Hello, " + nameParts[0]
        navUserMobile.text = userModel.mobile
    }

    /**
     * A companion object to declare the constants.
     */
    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
    }

    private fun toggleDrawer() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }
}