package com.example.expressfood.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object SyncScheduler {

    private const val SYNC_ON_CONNECT_WORK = "sync_on_connectivity"

    fun enqueueImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val menuWork = OneTimeWorkRequestBuilder<SyncMenuWorker>()
            .setConstraints(constraints)
            .build()

        val ordersWork = OneTimeWorkRequestBuilder<SyncOrdersWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork(SYNC_ON_CONNECT_WORK, ExistingWorkPolicy.REPLACE, menuWork)
            .then(ordersWork)
            .enqueue()
    }
}
