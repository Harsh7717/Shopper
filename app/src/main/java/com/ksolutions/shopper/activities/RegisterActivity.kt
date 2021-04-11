package com.ksolutions.shopper.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ksolutions.shopper.R
import com.ksolutions.shopper.firebase.FirestoreClass
import com.ksolutions.shopper.model.User
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*


class RegisterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setupActionBar()
        initDatePicker(et_dob)
        et_dob.setText(getTodaysDate())

        var phoneNo = intent.getStringExtra("phoneNo")
        et_mobile.setText(phoneNo)

        btn_sign_up.setOnClickListener{
            registerUser()
        }

        et_dob.setOnClickListener(){
            showDatePickerDialog(et_dob)
        }
    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_register_activity)
        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_register_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun registerUser(){
        val name: String = et_name.text.toString().trim { it <= ' ' }
        val email: String = et_email.text.toString().trim { it <= ' ' }
        val dob: String = et_dob.text.toString().trim { it <= ' ' }
        val phone: String = et_mobile.text.toString()

        if (validateForm(name, phone, email, dob))
        {
            showProgressDialog("Please Wait....")
            val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

            val user = User(
                    firebaseUser.uid, name, phone, email, dob
            )

            // call the registerUser function of FirestoreClass to make an entry in the database.
            FirestoreClass().registerUser(this@RegisterActivity, user)
        }
    }

    private fun validateForm(name: String, phone: String, email: String, dob: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter name.",true)
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.",true)
                false
            }
            dob.length!=11 || dob[2]!='/' || dob[6]!='/'-> {
                showErrorSnackBar("Please enter valid Date of Birth.",true)
                false
            }
            TextUtils.isEmpty(phone) ->{
                showErrorSnackBar("Please enter Mobile Number.",true)
                false
            }
            else -> {
                true
            }
        }
    }

    fun userRegisteredSuccess()
    {
        showErrorSnackBar("You have successfully registered.",false)

        // Hide the progress dialog
        hideProgressDialog()

        /**
         * Here the new user registered is automatically signed-in so we just sign-out the user from firebase
         * and send him to Intro Screen for Sign-In
         */
        FirebaseAuth.getInstance().signOut()
        // Finish the Sign-Up Screen
        finish()
    }
}