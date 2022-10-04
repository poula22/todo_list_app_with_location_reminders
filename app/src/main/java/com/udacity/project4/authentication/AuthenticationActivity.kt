package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import kotlinx.android.synthetic.main.activity_authentication.*

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    val SIGN_CODE=1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)


//        Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
//           If the user was authenticated, send him to RemindersActivity

        val providers= listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        login_btn.setOnClickListener {
           startActivityForResult(
               AuthUI.getInstance()
                   .createSignInIntentBuilder()
                   .setAvailableProviders(providers)
                   .setIsSmartLockEnabled(false)
                   .build()
               ,SIGN_CODE
           )
        }

//           a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val response=IdpResponse.fromResultIntent(data)
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val intent = Intent(this, RemindersActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "user name or password wrong", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}
