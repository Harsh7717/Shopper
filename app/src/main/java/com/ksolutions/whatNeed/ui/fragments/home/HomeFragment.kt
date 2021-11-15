package com.ksolutions.whatNeed.ui.fragments.home

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import butterknife.ButterKnife
import butterknife.Unbinder
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.firebase.FirestoreClass
import com.ksolutions.whatNeed.models.BusinessModel
import com.ksolutions.whatNeed.ui.fragments.BaseFragment
import com.ksolutions.whatNeed.adapter.MyBusinessListAdapter
import com.ksolutions.whatNeed.utils.CompareBusinessDist
import com.ksolutions.whatNeed.utils.PublicValues
import kotlinx.android.synthetic.main.fragment_home.*


open class HomeFragment : BaseFragment(), View.OnClickListener {

  val TAB_POSITION = 1
  private var mUnbinder: Unbinder? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(
          inflater: LayoutInflater,
          container: ViewGroup?,
          savedInstanceState: Bundle?
  ): View? {
    val rootView = inflater.inflate(R.layout.fragment_home, container, false)
    mUnbinder = ButterKnife.bind(this, rootView)
    return rootView
  }

  override fun onDestroyView() {
    super.onDestroyView()
    if (mUnbinder != null) {
      mUnbinder!!.unbind()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.home_menu, menu)
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onResume() {
    super.onResume()
    getBusinessListFromFireStore()
  }

  companion object {
    fun newInstance(): HomeFragment = HomeFragment()
  }
    private fun getBusinessListFromFireStore() {
      // Show the progress dialog.
      showProgressDialog(resources.getString(R.string.please_wait))
      val categoryList = ArrayList<String>(listOf(*resources.getStringArray(R.array.shopsCategory)))
      FirestoreClass().getBusinessList(this@HomeFragment, categoryList, PublicValues.userPostalCode)
    }

    fun successBusinessListFromFireStore(BusinessList: ArrayList<BusinessModel>)
    {
      hideProgressDialog()

      if (BusinessList.size > 0)
      {
        BusinessList.sortWith(CompareBusinessDist)
        rv_home_my_business.visibility = View.VISIBLE
        tv_home_no_business_found.visibility = View.GONE

        rv_home_my_business.layoutManager = GridLayoutManager(activity, 2)
        rv_home_my_business.setHasFixedSize(true)

        val adapter = MyBusinessListAdapter(requireActivity(), BusinessList)
        rv_home_my_business.adapter = adapter


        /*rv_my_business.visibility = View.VISIBLE
        tv_no_business_found.visibility = View.GONE

        rv_my_business.layoutManager = LinearLayoutManager(activity)
        rv_my_business.setHasFixedSize(true)

        val adapterProducts = MyBusinessListAdapter(requireActivity(), BusinessList, this)
        rv_my_business.adapter = adapterProducts*/
      }
      else
      {
        rv_home_my_business.visibility = View.GONE
        tv_home_no_business_found.visibility = View.VISIBLE
        tv_home_no_business_found.text = PublicValues.userPostalCode
      }
    }

  override fun onClick(v: View?) {
    TODO("Not yet implemented")
  }
}