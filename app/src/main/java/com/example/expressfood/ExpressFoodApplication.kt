package com.example.expressfood

import android.app.Application
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.expressfood.data.local.AppDatabase
import com.example.expressfood.data.local.UserSessionStore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.example.expressfood.data.repository.CartRepository
import com.example.expressfood.data.repository.OrderRepository
import com.example.expressfood.data.repository.ProductRepository
import com.example.expressfood.data.repository.UserRepository
import com.example.expressfood.util.ConnectivityObserver
import com.example.expressfood.worker.SyncMenuWorker
import com.example.expressfood.worker.SyncOrdersWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ExpressFoodApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var userRepository: UserRepository
        private set

    lateinit var productRepository: ProductRepository
        private set

    lateinit var cartRepository: CartRepository
        private set

    lateinit var orderRepository: OrderRepository
        private set

    lateinit var connectivityObserver: ConnectivityObserver
        private set

    override fun onCreate() {
        super.onCreate()

        FirebaseFirestore.getInstance().firestoreSettings =
            FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "expressfood.db"
        ).build()

        userRepository = UserRepository(UserSessionStore(applicationContext))
        productRepository = ProductRepository(database)
        cartRepository = CartRepository(database)
        orderRepository = OrderRepository(database)
        connectivityObserver = ConnectivityObserver(applicationContext)

        scheduleSyncWorkers()
        seedMenuIfNeeded()
    }

    private fun seedMenuIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            productRepository.ensureLocalMenu()
        }
    }

    private fun scheduleSyncWorkers() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val menuWork = PeriodicWorkRequestBuilder<SyncMenuWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        val ordersWork = PeriodicWorkRequestBuilder<SyncOrdersWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(
            "sync_menu",
            ExistingPeriodicWorkPolicy.KEEP,
            menuWork
        )
        workManager.enqueueUniquePeriodicWork(
            "sync_orders",
            ExistingPeriodicWorkPolicy.KEEP,
            ordersWork
        )

        workManager.enqueue(
            androidx.work.OneTimeWorkRequestBuilder<SyncMenuWorker>()
                .setConstraints(constraints)
                .build()
        )
        workManager.enqueue(
            androidx.work.OneTimeWorkRequestBuilder<SyncOrdersWorker>()
                .setConstraints(constraints)
                .build()
        )
    }
}
