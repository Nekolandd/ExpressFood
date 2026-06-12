package com.example.expressfood.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.expressfood.ExpressFoodApplication
import com.example.expressfood.R
import com.example.expressfood.databinding.ActivityLoginBinding
import com.example.expressfood.domain.model.UserRole
import com.example.expressfood.ui.admin.AdminActivity
import com.example.expressfood.ui.client.ClientActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)

            val credential = GoogleAuthProvider.getCredential(
                account.idToken,
                null
            )

            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { authResult ->

                    if (authResult.isSuccessful) {
                        navigateAfterLogin()
                    } else {

                        Toast.makeText(
                            this,
                            getString(com.example.expressfood.R.string.login_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        } catch (e: ApiException) {
            val message = when (e.statusCode) {
                10 -> getString(R.string.login_error_developer)
                12501 -> getString(R.string.login_error_cancelled)
                else -> getString(R.string.login_error) + " (${e.statusCode})"
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                e.message ?: getString(R.string.login_error),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            navigateAfterLogin()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleClient = GoogleSignIn.getClient(this, gso)

        binding.btnGoogleLogin.setOnClickListener {

            googleClient.signOut()

            launcher.launch(
                googleClient.signInIntent
            )
        }
    }

    private fun navigateAfterLogin() {
        val firebaseUser = auth.currentUser ?: return
        val app = application as ExpressFoodApplication

        lifecycleScope.launch {
            try {
                val user = app.userRepository.ensureUserExists(
                    userId = firebaseUser.uid,
                    name = firebaseUser.displayName.orEmpty(),
                    email = firebaseUser.email.orEmpty()
                )

                val destination = when (user.role) {
                    UserRole.ADMIN -> AdminActivity::class.java
                    UserRole.CLIENT -> ClientActivity::class.java
                }

                startActivity(Intent(this@LoginActivity, destination))
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    e.message ?: getString(com.example.expressfood.R.string.login_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
