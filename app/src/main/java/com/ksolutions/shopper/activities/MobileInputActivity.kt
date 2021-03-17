package com.ksolutions.shopper.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.hbb20.CountryCodePicker
import com.ksolutions.shopper.R
import kotlinx.android.synthetic.main.activity_mobile_input.*
import java.util.concurrent.TimeUnit

class MobileInputActivity : AppCompatActivity() {

    private lateinit var phoneVal : String
    private lateinit var phone: EditText
    private lateinit var CountryCode : CountryCodePicker
    private lateinit var countryCode : String

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_input)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setupActionBar()

        auth = FirebaseAuth.getInstance()

        phone = findViewById(R.id.mobile_input_et)
        CountryCode = findViewById(R.id.ccp)

        var intent = Intent(this, OtpActivity::class.java)

        var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken)
            {
                if(verificationId != null)
                {
                    intent.putExtra("systemOtp",verificationId)
                    intent.putExtra("phoneNo",phone.text.toString())
                    intent.putExtra("countryCode",countryCode)
                    startActivity(intent)
                    finish()
                }
            }
            override fun onVerificationCompleted(credential: PhoneAuthCredential)
            {
                Toast.makeText(applicationContext, "Verificatoin Completed", Toast.LENGTH_LONG).show()
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException)
            {
                // This callback is invoked if an invalid request for verification is made,
                // for instance if the the phone number format is invalid
                Toast.makeText(applicationContext, "Failed", Toast.LENGTH_LONG).show()

                if (e is FirebaseAuthInvalidCredentialsException)
                {
                    // Invalid request
                    Toast.makeText(applicationContext, "Invalid Phone Number", Toast.LENGTH_LONG).show()
                }
                else if (e is FirebaseTooManyRequestsException)
                {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(applicationContext, "Limit Exceeded", Toast.LENGTH_LONG).show()
                }
                Toast.makeText(applicationContext, phoneVal, Toast.LENGTH_LONG).show()
            }
        }

        btn_mobile_input.setOnClickListener(){
            countryCode = CountryCode.selectedCountryCodeWithPlus.toString()
            phoneVal = countryCode + phone.text.toString()

            PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneVal, 60, TimeUnit.SECONDS, this, callbacks)
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential)
    {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(applicationContext, "OTP Verified Successfully", Toast.LENGTH_LONG).show()

                    var intent2 = Intent(this, SignUpActivity::class.java)

                    intent2.putExtra("phoneNo",phone.text.toString())
                    intent2.putExtra("countryCode",countryCode.toString())

                    startActivity(intent2)
                    finish()

                    val user = task.result?.user
                    // ...
                }
                else
                {
                    Toast.makeText(applicationContext, "Sign In Failed", Toast.LENGTH_LONG).show()
                    if (task.exception is FirebaseAuthInvalidCredentialsException)
                    {
                        // The verification code entered was invalid
                    }
                }
            }
    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_mobile_input_activity)
        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_mobile_input_activity.setNavigationOnClickListener { onBackPressed() }
    }
}