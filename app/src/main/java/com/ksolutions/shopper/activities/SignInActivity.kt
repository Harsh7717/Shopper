package com.ksolutions.shopper.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import com.ksolutions.shopper.R
import com.ksolutions.shopper.model.User
import kotlinx.android.synthetic.main.activity_sign_in.*
import java.util.concurrent.TimeUnit


class SignInActivity : BaseActivity() {

    private lateinit var phoneVal : String
    private lateinit var phone: EditText
    private lateinit var OTP : String
    public var isExist : Boolean = true

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_sign_in)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()

        auth = FirebaseAuth.getInstance()

        phone = findViewById(R.id.et_mobile_sign_in)

        var intent = Intent(this, MainActivity::class.java)

        var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken)
            {
                if(verificationId != null)
                {
                    false.also { et_mobile_sign_in.isEnabled = it }
                    sign_in_desc_text.visibility = VISIBLE
                    sign_in_otp.visibility = VISIBLE
                    btn_sign_in.visibility = VISIBLE
                    OTP = verificationId
                    hideProgressDialog()
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
                hideProgressDialog()

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

        btn_get_otp.setOnClickListener {
            phoneVal = phone.text.toString().trim{ it<=' ' }

            if(validate(phone.text.toString()))
            {
                showProgressDialog(resources.getString(R.string.please_wait))
                PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneVal, 60, TimeUnit.SECONDS, this, callbacks)
            }
        }

        btn_sign_in.setOnClickListener(){
            if(sign_in_otp.text.toString().isEmpty())
            {
                showErrorSnackBar("Please enter the OTP.")
                sign_in_otp.requestFocus()
            }
            else
            {
                showProgressDialog(resources.getString(R.string.please_wait))
                val credential = PhoneAuthProvider.getCredential(OTP!!, sign_in_otp.text.toString())
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun validate(phone: String):Boolean
    {
        return when {
            TextUtils.isEmpty(phone) ->{
                showErrorSnackBar("Please enter Mobile Number.")
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
                            hideProgressDialog()
                            showErrorSnackBar("Mobile Number isn't Registered Please Sign UP first.")
                            Handler().postDelayed({
                                deleteUser()
                            },1500)
                            hideProgressDialog()
                        }
                        else
                        {
                            //Toast.makeText(applicationContext, "Mobile Number Registered", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }

                        val user = task.result?.user
                        // ...
                    }
                    else
                    {
                        Toast.makeText(applicationContext, "Sign In Failed", Toast.LENGTH_LONG).show()
                        if (task.exception is FirebaseAuthInvalidCredentialsException)
                        {
                            Toast.makeText(applicationContext, "Invalid OTP", Toast.LENGTH_LONG).show()
                        }
                    }
                }
    }

    fun deleteUser()
    {
        //Toast.makeText(applicationContext, "Deleting the User", Toast.LENGTH_LONG).show()
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        currentUser.delete().addOnCompleteListener { task ->
            if (task.isSuccessful)
            {
                //Toast.makeText(applicationContext, "Mobile Number Deleted Successfully", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, SignUpActivity::class.java))
                finish()
            }
            else
            {
                Toast.makeText(applicationContext, "Faced Any Error", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun signInSuccess(user: User)
    {
        hideProgressDialog()
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }

    private fun setupActionBar()
    {

        setSupportActionBar(toolbar_sign_in_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_sign_in_activity.setNavigationOnClickListener { onBackPressed() }
    }
}