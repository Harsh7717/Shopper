package com.ksolutions.whatNeed.navigation

import androidx.fragment.app.Fragment

public interface FragmentUpdateCallback {
    fun addFragment(fragment: Fragment?, tabPosition: Int)
}