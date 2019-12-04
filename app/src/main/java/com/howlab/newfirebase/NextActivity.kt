package com.howlab.newfirebase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_next.*

class NextActivity : AppCompatActivity() {
    var googleSignInClient : GoogleSignInClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next)
        logout_button.setOnClickListener {
            logout()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
    }
    fun logout(){
        FirebaseAuth.getInstance().signOut()

        //Google Session out
        googleSignInClient?.signOut()

        //Facebook Session out
        LoginManager.getInstance().logOut()

        finish()
    }
}
