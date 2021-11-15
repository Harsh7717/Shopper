package com.ksolutions.whatNeed.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ksolutions.whatNeed.ui.fragments.vendorHome.VendorHomeFragment
import com.ksolutions.whatNeed.ui.fragments.vendorProfile.VendorProfileFragment
import java.util.*

class VendorsFragmentPagerAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager) {
    //endregion
    //region Fields
    private val vendorHome: MutableList<Fragment>
    private val vendorProfile: MutableList<Fragment>

    //endregion
    //region FragmentPagerAdapter overridden methods
    override fun getItem(position: Int): Fragment {
        return if (position == VENDOR_HOME_POSITION) {
            if (vendorHome.isEmpty()) {
                BASE_FRAGMENTS[position]
            } else vendorHome[vendorHome.size - 1]
        } else  {
            if (vendorProfile.isEmpty()) {
                BASE_FRAGMENTS[position]
            } else vendorProfile[vendorProfile.size - 1]
        }
    }

    override fun getCount(): Int {
        return BASE_FRAGMENTS.size
    }

    override fun getItemId(position: Int): Long {
        if (position == VENDOR_HOME_POSITION
            && getItem(position) == BASE_FRAGMENTS[position]
        ) {
            return VENDOR_HOME_POSITION.toLong()
        } else if (position == VENDOR_PROFILE_POSITION
            && getItem(position) == BASE_FRAGMENTS[position]
        ) {
            return VENDOR_PROFILE_POSITION.toLong()
        }
        return getItem(position).hashCode().toLong()
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    //endregion
    //region helper methods
    fun updateFragment(fragment: Fragment, position: Int) {
        if (!BASE_FRAGMENTS.contains(fragment)) {
            addInnerFragment(fragment, position)
        }
        notifyDataSetChanged()
    }

    fun removeFragment(fragment: Fragment, position: Int): Boolean {
        if (position == VENDOR_HOME_POSITION) {
            if (vendorHome.contains(fragment)) {
                removeInnerFragment(fragment, vendorHome)
                return true
            }
        } else  {
            if (vendorProfile.contains(fragment)) {
                removeInnerFragment(fragment, vendorProfile)
                return true
            }
        }
        return false
    }

    private fun removeInnerFragment(fragment: Fragment, tabFragments: MutableList<Fragment>) {
        tabFragments.remove(fragment)
        notifyDataSetChanged()
    }

    private fun addInnerFragment(fragment: Fragment, position: Int) {
        if (position == VENDOR_HOME_POSITION) {
            vendorHome.add(fragment)
        } else  {
            vendorProfile.add(fragment)
        }
    } //endregion

    companion object {
        //region Statics
        private val BASE_FRAGMENTS: List<Fragment> = Arrays.asList(
            VendorHomeFragment.newInstance(),
            VendorProfileFragment.newInstance(),
        )
        private const val VENDOR_HOME_POSITION = 0
        private const val VENDOR_PROFILE_POSITION = 1
    }

    init {
        vendorHome = ArrayList()
        vendorProfile = ArrayList()
    }
}