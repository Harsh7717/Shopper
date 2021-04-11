package com.ksolutions.shopper.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
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
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import java.util.concurrent.TimeUnit

class SignUpActivity : BaseActivity() {

    private lateinit var phoneVal : String
    public lateinit var phone: EditText
    private lateinit var OTP : String

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setupActionBar()

        setupActionBar()

        auth = FirebaseAuth.getInstance()

        phone = findViewById(R.id.et_mobile_sign_up)

        var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken)
            {
                if(verificationId != null)
                {
                    et_mobile_sign_up.isEnabled=false
                    sign_up_desc_text.visibility = View.VISIBLE
                    sign_up_otp.visibility = View.VISIBLE
                    btn_sign_up.visibility = View.VISIBLE
                    OTP = verificationId
                    hideProgressDialog()
                }
            }
            override fun onVerificationCompleted(credential: PhoneAuthCredential)
            {
                showErrorSnackBar("Verificatoin Completed",false)
                //Toast.makeText(applicationContext, "Verificatoin Completed", Toast.LENGTH_LONG).show()
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException)
            {
                // This callback is invoked if an invalid request for verification is made,
                // for instance if the the phone number format is invalid
                showErrorSnackBar("Facing Problem..Please try again",true)
                hideProgressDialog()

                if (e is FirebaseAuthInvalidCredentialsException)
                {
                    // Invalid request
                    showErrorSnackBar("Please enter valid Phone number.",true)
                }
                else if (e is FirebaseTooManyRequestsException)
                {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(applicationContext, "Limit Exceeded", Toast.LENGTH_LONG).show()
                }
                Toast.makeText(applicationContext, phoneVal, Toast.LENGTH_LONG).show()
            }
        }

        btn_get_otp_sign_up.setOnClickListener {
            phoneVal = phone.text.toString().trim{ it<=' ' }

            if(validate(phone.text.toString()))
            {
                showProgressDialog(resources.getString(R.string.please_wait))
                PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneVal, 60, TimeUnit.SECONDS, this, callbacks)
            }
        }

        btn_sign_up.setOnClickListener(){
            if(sign_up_otp.text.toString().isEmpty())
            {
                showErrorSnackBar("Please enter the OTP.",true)
                sign_up_otp.requestFocus()
            }
            else
            {
                showProgressDialog(resources.getString(R.string.please_wait))
                val credential = PhoneAuthProvider.getCredential(OTP!!, sign_up_otp.text.toString())
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun validate(phone: String):Boolean
    {
        return when {
            TextUtils.isEmpty(phone) ->{
                showErrorSnackBar("Please enter Mobile Number.",true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential)
    {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful)
                    {
                        // Sign in success, update UI with the signed-in user's information
                        //Toast.makeText(applicationContext, "OTP Verified Successfully", Toast.LENGTH_LONG).show()

                        if (task.result!!.additionalUserInfo.isNewUser)
                        {
                            var intent = Intent(this, RegisterActivity::class.java)

                            intent.putExtra("phoneNo",et_mobile_sign_up.text.toString())
                            startActivity(intent)
                            finish()
                        }
                        else
                        {
                            //startActivity(Intent(this, SignInActivity::class.java))
                            hideProgressDialog()
                            showErrorSnackBar("Mobile Number Already Registered Please Sign In.",true)
                            Handler().postDelayed({
                                startActivity(Intent(this, SignInActivity::class.java))
                                finish()
                            },1500)
                        }

                        val user = task.result?.user
                        // ...
                    }
                    else
                    {
                        hideProgressDialog()
                        showErrorSnackBar("OTP Verification Failed..",true)
                        if (task.exception is FirebaseAuthInvalidCredentialsException)
                        {
                            showErrorSnackBar("Please Enter Valid OTP..",true)
                        }
                    }
                }
    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_sign_up_activity)
        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        toolbar_sign_up_activity.setNavigationOnClickListener { onBackPressed() }
    }
}