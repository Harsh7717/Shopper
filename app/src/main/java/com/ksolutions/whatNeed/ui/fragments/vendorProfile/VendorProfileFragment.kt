package com.ksolutions.whatNeed.ui.fragments.vendorProfile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.navigation.FragmentUpdateCallback

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VendorProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VendorProfileFragment : Fragment() {

    val TAB_POSITION = 1

    //endregion
    private var mUnbinder: Unbinder? = null
    private var mFragmentUpdateCallback: FragmentUpdateCallback? = null



    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    //endregion
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentUpdateCallback) {
            mFragmentUpdateCallback = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView =  inflater.inflate(R.layout.fragment_vendor_profile, container, false)

        mUnbinder = ButterKnife.bind(this, rootView)

        if (activity != null) {
            rootView.setBackgroundColor(requireActivity().resources.getColor(R.color.colorPrimary))
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mUnbinder != null) {
            mUnbinder!!.unbind()
        }
    }

    companion object {
        fun newInstance(): VendorProfileFragment = VendorProfileFragment()
    }
}