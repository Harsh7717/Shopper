package com.ksolutions.whatNeed.ui.fragments.business

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import butterknife.ButterKnife
import butterknife.Unbinder
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.activities.BusinessCategoryActivity
import com.ksolutions.whatNeed.firebase.FirestoreClass
import com.ksolutions.whatNeed.models.BusinessModel
import com.ksolutions.whatNeed.ui.fragments.BaseFragment
import com.ksolutions.whatNeed.adapter.MyBusinessListAdapter
import kotlinx.android.synthetic.main.fragment_business.*

class BusinessFragment : BaseFragment()
{
    val TAB_POSITION = 2
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
        val rootView = inflater.inflate(R.layout.fragment_business, container, false)

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
        inflater.inflate(R.menu.add_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_add) {
            startActivity(Intent(activity, BusinessCategoryActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        getBusinessListFromFireStore()
    }

    private fun getBusinessListFromFireStore() {
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        val categoryList = ArrayList<String>(listOf(*resources.getStringArray(R.array.shopsCategory)))
        // Call the function of Firestore class.
        FirestoreClass().getBusinessList(this@BusinessFragment, categoryList, FirestoreClass().getCurrentUserID())
    }

    fun successBusinessListFromFireStore(BusinessList: ArrayList<BusinessModel>)
    {
        hideProgressDialog()

        if (BusinessList.size > 0)
        {
            rv_my_business.visibility = View.VISIBLE
            tv_no_business_found.visibility = View.GONE

            rv_my_business.layoutManager = GridLayoutManager(activity, 2)
            rv_my_business.setHasFixedSize(true)

            val adapter = MyBusinessListAdapter(requireActivity(), BusinessList)
            rv_my_business.adapter = adapter


            /*rv_my_business.visibility = View.VISIBLE
            tv_no_business_found.visibility = View.GONE

            rv_my_business.layoutManager = LinearLayoutManager(activity)
            rv_my_business.setHasFixedSize(true)

            val adapterProducts = MyBusinessListAdapter(requireActivity(), BusinessList, this)
            rv_my_business.adapter = adapterProducts*/
        }
        else
        {
            rv_my_business.visibility = View.GONE
            tv_no_business_found.visibility = View.VISIBLE
        }
    }

    fun deleteProduct(productID: String) {

        Toast.makeText(
                requireActivity(),
                "You can now delete the product. $productID",
                Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        fun newInstance(): BusinessFragment = BusinessFragment()
    }
}