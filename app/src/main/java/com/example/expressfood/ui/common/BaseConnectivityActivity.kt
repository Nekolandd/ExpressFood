package com.example.expressfood.ui.common

import android.content.Intent
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.expressfood.ExpressFoodApplication
import com.example.expressfood.R
import com.example.expressfood.data.local.UserSessionStore
import com.example.expressfood.ui.login.LoginActivity
import com.example.expressfood.worker.SyncScheduler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

abstract class BaseConnectivityActivity : AppCompatActivity() {

    protected fun observeConnectivity(indicatorView: android.widget.TextView) {
        val app = application as ExpressFoodApplication
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                var wasOnline = app.connectivityObserver.isCurrentlyOnline()
                app.connectivityObserver.isOnline.collect { online ->
                    if (online) {
                        indicatorView.text = getString(R.string.online)
                        indicatorView.setTextColor(Color.WHITE)
                        indicatorView.setBackgroundColor(Color.TRANSPARENT)
                        if (!wasOnline) {
                            SyncScheduler.enqueueImmediateSync(this@BaseConnectivityActivity)
                        }
                    } else {
                        indicatorView.text = getString(R.string.offline)
                        indicatorView.setTextColor(Color.WHITE)
                        indicatorView.setBackgroundColor(
                            ContextCompat.getColor(this@BaseConnectivityActivity, R.color.offline)
                        )
                    }
                    wasOnline = online
                }
            }
        }
    }

    protected fun startUserOrderSync() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val app = application as ExpressFoodApplication
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.orderRepository.observeAndSyncUserOrders(userId).collect { }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            logout()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            UserSessionStore(applicationContext).clearUser(userId)
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(this, gso).signOut()
        FirebaseAuth.getInstance().signOut()

        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
