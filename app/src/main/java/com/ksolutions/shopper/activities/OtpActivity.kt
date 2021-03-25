package com.ksolutions.shopper.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.chaos.view.PinView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.ksolutions.shopper.R
import kotlinx.android.synthetic.main.activity_otp.*

class OtpActivity : BaseActivity() {

    private lateinit var userOtp : PinView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        setupActionBar()

        auth = FirebaseAuth.getInstance()

        var OTP = intent.getStringExtra("systemOtp")
        given_mobile_number.text = intent.getStringExtra("countryCode") + intent.getStringExtra("phoneNo")

        userOtp = findViewById(R.id.otpVerifyEdit)

        btn_otp_verify.setOnClickListener(){

            if(userOtp.text.toString().isEmpty())
            {
                userOtp.error="Please Enter the OTP"
                userOtp.requestFocus()
            }
            else
            {
                showProgressDialog(resources.getString(R.string.please_wait))
                val credential = PhoneAuthProvider.getCredential(OTP!!, userOtp.text.toString())
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential)
    {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                {
                    Toast.makeText(applicationContext, "OTP Verified Successfully", Toast.LENGTH_LONG).show()

                    var phoneNo = intent.getStringExtra("phoneNo")
                    var countryCode = intent.getStringExtra("countryCode")

                    var intent2 = Intent(this, RegisterActivity::class.java)
                    intent2.putExtra("phoneNo",phoneNo)
                    intent2.putExtra("countryCode",countryCode)
                    startActivity(intent2)
                    finish()

                    val user = task.result?.user
                    // ...
                }
                else
                {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_otp_activity)
        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_otp_activity.setNavigationOnClickListener { onBackPressed() }
    }
}