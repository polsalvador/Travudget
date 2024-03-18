package com.travudget.travudget

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IniciSessio : AppCompatActivity() {
    private val backendManager = BackendManager()

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inici_sessio)
        val id = "175719393623-pvcognqc965hust5r3hph12d6kl4259e.apps.googleusercontent.com"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(id)
            .requestEmail()
            .build()

        val signInButton: SignInButton = findViewById(R.id.login_google)
        signInButton.setOnClickListener {
            var googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
                val googleEmail = account?.email.toString()
                val googleName = account?.displayName.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    backendManager.sendLogin(googleEmail, googleName)
                    startActivity(Intent(this@IniciSessio, Principal::class.java))
                    finish()
                }
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }
}
