package com.ksolutions.whatNeed.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.navigation.FragmentUpdateCallback
import com.ksolutions.whatNeed.navigation.VendorsFragmentPagerAdapter
import com.ksolutions.whatNeed.ui.fragments.vendorHome.VendorHomeFragment
import com.ksolutions.whatNeed.ui.fragments.vendorProfile.VendorProfileFragment
import com.ksolutions.whatNeed.utils.PublicValues
import com.ksolutions.whatNeed.utils.GetAddressFromLatLng
import kotlinx.android.synthetic.main.activity_vendors_main.*
import kotlinx.android.synthetic.main.vendors_app_bar_main.*

class VendorsMainActivity : BaseActivity(), FragmentUpdateCallback {

    private lateinit var mPagerAdapter: VendorsFragmentPagerAdapter
    private var mCurrentTabPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendors_main)

        ButterKnife.bind(this)
        mPagerAdapter = VendorsFragmentPagerAdapter(supportFragmentManager)
        MainViewPager.adapter = mPagerAdapter
        MainViewPager.offscreenPageLimit = 2

        setupActionBar()
        updateLocationText()

        vendorsBottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.vendor_navigation_home -> {
                    mCurrentTabPosition = VendorHomeFragment().TAB_POSITION
                    MainViewPager.currentItem = mCurrentTabPosition
                    return@OnNavigationItemSelectedListener true
                }
                R.id.vendor_navigation_profile -> {
                    mCurrentTabPosition = VendorProfileFragment().TAB_POSITION
                    MainViewPager.currentItem = mCurrentTabPosition
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })


        /*val navView: BottomNavigationView = findViewById(R.id.vendorsBottomNavigation)

        val navController = findNavController(R.id.nav_vendors_fragment)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.vendors_nav_home, R.id.vendors_nav_profile
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        vendorsBottomNavigation.setOnNavigationItemSelectedListener(vendorOnNavigationItemSelectedListener)*/

        //loadDefaultFragment()
    }

    override fun addFragment(fragment: Fragment?, tabPosition: Int) {
        mPagerAdapter!!.updateFragment(fragment!!, tabPosition)
    }

    private fun updateLocationText()
    {
        val addressTask = GetAddressFromLatLng(this, PublicValues.userLatitude, PublicValues.userLongitude)

        addressTask.setAddressListener(object : GetAddressFromLatLng.AddressListener {
            override fun onAddressFound(address: String?) {
                Log.e("Address ::", "" + address)
                if (address != null) {
                    val addressData = address.split(", ") as MutableList<String>
                    addressData.remove(addressData.last())
                    tv_vendors_home_location.text = addressData.joinToString(", ")
                }
            }

            override fun onError() {
                Log.e("Get Address ::", "Something is wrong...")
            }
        })
        addressTask.getAddress()
    }

    override fun onBackPressed() {
        if (!mPagerAdapter.removeFragment(mPagerAdapter.getItem(mCurrentTabPosition), mCurrentTabPosition)) {
            finish()
        }
    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_vendors_main_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_vendors_main_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /*private fun loadDefaultFragment()
    {
        val defaultFragment = VendorHomeFragment.newInstance()
        openFragment(defaultFragment)
        //return@OnNavigationItemSelectedListener true
    }*/

    /*private val vendorOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {

            R.id.vendor_navigation_home -> {
                mCurrentTabPosition = VendorHomeFragment().TAB_POSITION
                viewPager.currentItem = mCurrentTabPosition

                //val vendorsHomeFragment = VendorHomeFragment.newInstance()
                //openFragment(vendorsHomeFragment)
                return@OnNavigationItemSelectedListener true
            }

            R.id.vendor_navigation_profile -> {
                mCurrentTabPosition = VendorProfileFragment().TAB_POSITION
                viewPager.currentItem = mCurrentTabPosition

                //val vendorsProfileFragment = VendorProfileFragment.newInstance()
                //openFragment(vendorsProfileFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }*/

    /*private fun openFragment(fragment: Fragment)
    {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_vendors_fragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }*/

    //endregion

}