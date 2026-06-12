package com.example.expressfood.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expressfood.ExpressFoodApplication
import com.google.firebase.auth.FirebaseAuth

class SyncOrdersWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as ExpressFoodApplication
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val uploadResult = app.orderRepository.syncUnsyncedOrders()
        if (userId != null) {
            app.orderRepository.pullRemoteOrdersForUser(userId)
        }

        return uploadResult.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }
}
