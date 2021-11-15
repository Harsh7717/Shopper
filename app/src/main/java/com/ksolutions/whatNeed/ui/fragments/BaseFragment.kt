package com.ksolutions.whatNeed.ui.fragments

import android.app.Dialog
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.ksolutions.whatNeed.R
import kotlinx.android.synthetic.main.dialog_progress.*

open class BaseFragment : Fragment() {

    private lateinit var mProgressDialog: Dialog

    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(requireActivity())

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        mProgressDialog.setContentView(R.layout.dialog_progress)

        mProgressDialog.tv_progress_text.text = text

        mProgressDialog.setCancelable(false)
        mProgressDialog.setCanceledOnTouchOutside(false)

        //Start the dialog and display it on screen.
        mProgressDialog.show()
    }

    /**
     * This function is used to dismiss the progress dialog if it is visible to user.
     */
    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }

    fun showErrorSnackBar(message: String, errorMessage:Boolean) {
        val snackBar = getActivity()?.let {
            Snackbar.make(
                it.findViewById(android.R.id.content),
                message, Snackbar.LENGTH_LONG)
        }
        val snackBarView = snackBar?.view

        if(errorMessage)
        {
            if (snackBarView != null) {
                snackBarView.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.snackbar_error_color)
                )
            }
        }
        else
        {
            if (snackBarView != null) {
                snackBarView.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.snackbar_success_color)
                )
            }
        }
        if (snackBar != null) {
            snackBar.show()
        }
    }
}