package com.ksolutions.whatNeed.ui.fragments.notifications

import android.os.Bundle
import android.view.*
import butterknife.ButterKnife
import butterknife.Unbinder
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.ui.fragments.BaseFragment

class NotificationsFragment : BaseFragment() {

  val TAB_POSITION = 3
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
    val rootView = inflater.inflate(R.layout.fragment_notifications, container, false)
    mUnbinder = ButterKnife.bind(this, rootView)
    return rootView
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.add_menu, menu)
    super.onCreateOptionsMenu(menu, inflater)
  }

  companion object {
    fun newInstance(): NotificationsFragment = NotificationsFragment()
  }
}