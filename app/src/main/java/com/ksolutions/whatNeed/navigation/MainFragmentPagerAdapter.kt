package com.ksolutions.whatNeed.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ksolutions.whatNeed.ui.fragments.business.BusinessFragment
import com.ksolutions.whatNeed.ui.fragments.home.HomeFragment
import com.ksolutions.whatNeed.ui.fragments.notifications.NotificationsFragment
import com.ksolutions.whatNeed.ui.fragments.vendors.VendorsFragment
import java.util.*

class MainFragmentPagerAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager) {
    //endregion
    //region Fields
    private val home: MutableList<Fragment>
    private val vendors: MutableList<Fragment>
    private val business: MutableList<Fragment>
    private val notification: MutableList<Fragment>

    //endregion
    //region FragmentPagerAdapter overridden methods
    override fun getItem(position: Int): Fragment {
        return if (position == HOME_POSITION) {
            if (home.isEmpty()) {
                BASE_FRAGMENTS[position]
            } else home[home.size - 1]
        }
        else if(position == BUSINESS_POSITION) {
            if (business.isEmpty()) {
                BASE_FRAGMENTS[position]
            } else business[business.size - 1]
        }
        else if(position == NOTIFICATION_POSITION){
            if(notification.isEmpty()){
                BASE_FRAGMENTS[position]
            } else notification[notification.size-1]
        }
        else  {
            if (vendors.isEmpty()) {
                BASE_FRAGMENTS[position]
            } else vendors[vendors.size - 1]
        }
    }

    override fun getCount(): Int {
        return BASE_FRAGMENTS.size
    }

    override fun getItemId(position: Int): Long {
        if (position == HOME_POSITION
            && getItem(position) == BASE_FRAGMENTS[position]
        ) {
            return HOME_POSITION.toLong()
        } else if (position == VENDORS_POSITION
            && getItem(position) == BASE_FRAGMENTS[position]
        ) {
            return VENDORS_POSITION.toLong()
        }
        else if (position == NOTIFICATION_POSITION
            && getItem(position) == BASE_FRAGMENTS[position]
        ) {
            return NOTIFICATION_POSITION.toLong()
        }
        else if (position == BUSINESS_POSITION
            && getItem(position) == BASE_FRAGMENTS[position]
        ){
            return BUSINESS_POSITION.toLong()
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
        if (position == HOME_POSITION) {
            if (home.contains(fragment)) {
                removeInnerFragment(fragment, home)
                return true
            }
        } else  {
            if (vendors.contains(fragment)) {
                removeInnerFragment(fragment, vendors)
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
        if (position == HOME_POSITION) {
            home.add(fragment)
        } else  {
            vendors.add(fragment)
        }
    } //endregion

    companion object {
        //region Statics
        private val BASE_FRAGMENTS: List<Fragment> = listOf(
            VendorsFragment.newInstance(),
            HomeFragment.newInstance(),
            BusinessFragment.newInstance(),
            NotificationsFragment.newInstance()
        )
        private const val HOME_POSITION = 1
        private const val VENDORS_POSITION = 1
        private const val BUSINESS_POSITION = 2
        private const val NOTIFICATION_POSITION = 3
    }

    init {
        home = ArrayList()
        vendors = ArrayList()
        notification = ArrayList()
        business = ArrayList()
    }
}