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
    private lateinit  var datePickerDialog: DatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setupActionBar()
        initDatePicker()
        et_dob.setText(getTodaysDate())

        var phoneNo = intent.getStringExtra("phoneNo")
        et_mobile.setText(phoneNo)

        btn_sign_up.setOnClickListener{
            registerUser()
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
                showErrorSnackBar("Please enter name.")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.")
                false
            }
            dob.length!=11 || dob[2]!='/' || dob[6]!='/'-> {
                showErrorSnackBar("Please enter valid Date of Birth.")
                false
            }
            TextUtils.isEmpty(phone) ->{
                showErrorSnackBar("Please enter Mobile Number.")
                false
            }
            else -> {
                true
            }
        }
    }

    fun userRegisteredSuccess()
    {

        Toast.makeText(
                this@RegisterActivity,
                "You have successfully registered.",
                Toast.LENGTH_SHORT
        ).show()

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

    private fun getTodaysDate(): String?
    {
        val cal = Calendar.getInstance()
        val year = cal[Calendar.YEAR]
        var month = cal[Calendar.MONTH]
        month += 1
        val day = cal[Calendar.DAY_OF_MONTH]
        return makeDateString(day, month, year)
    }

    private fun initDatePicker()
    {
        val dateSetListener = OnDateSetListener { datePicker, year, month, day ->
            var month = month
            month += 1
            et_dob.setText(makeDateString(day, month, year))
        }
        val cal: Calendar = Calendar.getInstance()
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DAY_OF_MONTH)
        val style: Int = AlertDialog.THEME_HOLO_LIGHT
        datePickerDialog = DatePickerDialog(this, style, dateSetListener, year, month, day)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis();
    }

    private fun makeDateString(day: Int, month: Int, year: Int): String?
    {
        return day.toString() + "/" + getMonthFormat(month).toString() + "/" + year
    }

    private fun getMonthFormat(month: Int): String?
    {
        if (month == 1) return "JAN"
        if (month == 2) return "FEB"
        if (month == 3) return "MAR"
        if (month == 4) return "APR"
        if (month == 5) return "MAY"
        if (month == 6) return "JUN"
        if (month == 7) return "JUL"
        if (month == 8) return "AUG"
        if (month == 9) return "SEP"
        if (month == 10) return "OCT"
        if (month == 11) return "NOV"
        return if (month == 12) "DEC" else "JAN"

        //default should never happen
    }

    fun showDatePickerDialog(v: View)
    {
        this.datePickerDialog.show();
    }
}