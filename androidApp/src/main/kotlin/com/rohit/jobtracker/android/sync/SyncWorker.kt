package com.rohit.jobtracker.android.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rohit.jobtracker.android.cache.LocalApplicationStore
import com.rohit.jobtracker.shared.api.JobTrackerApi
import com.rohit.jobtracker.shared.model.CreateApplicationRequest
import com.rohit.jobtracker.shared.model.CreateNoteRequest
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val api: JobTrackerApi by inject()
    private val localStore: LocalApplicationStore by inject()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun doWork(): Result {
        val mutations = localStore.getPendingMutations()
        if (mutations.isEmpty()) {
            return Result.success()
        }

        for (mutation in mutations) {
            try {
                val success = processMutation(mutation)
                if (success) {
                    localStore.removeMutation(mutation.id)
                } else {
                    return Result.retry()
                }
            } catch (e: Exception) {
                return Result.retry()
            }
        }

        // Once the queue is drained, fetch fresh applications from server to ensure cache parity
        try {
            val freshApps = api.getApplications()
            localStore.saveApplications(freshApps)
        } catch (_: Exception) {}

        return Result.success()
    }

    private suspend fun processMutation(mutation: PendingMutation): Boolean {
        return when (mutation.type) {
            MutationType.CREATE_APP -> {
                val req = mutation.payloadJson?.let { json.decodeFromString<CreateApplicationRequest>(it) }
                if (req != null) {
                    val created = api.createApplication(req)
                    localStore.saveOrUpdateApplication(created)
                    true
                } else true
            }
            MutationType.UPDATE_APP -> {
                val req = mutation.payloadJson?.let { json.decodeFromString<UpdateApplicationRequest>(it) }
                if (req != null) {
                    val updated = api.updateApplication(mutation.entityId, req)
                    localStore.saveOrUpdateApplication(updated)
                    true
                } else true
            }
            MutationType.DELETE_APP -> {
                api.deleteApplication(mutation.entityId)
                localStore.deleteCachedApplication(mutation.entityId)
                true
            }
            MutationType.ADD_NOTE -> {
                val req = mutation.payloadJson?.let { json.decodeFromString<CreateNoteRequest>(it) }
                val appId = mutation.parentEntityId
                if (req != null && appId != null) {
                    val note = api.addNote(appId, req)
                    localStore.addCachedNote(appId, note)
                    true
                } else true
            }
            MutationType.DELETE_NOTE -> {
                val appId = mutation.parentEntityId
                if (appId != null) {
                    api.deleteNote(appId, mutation.entityId)
                    localStore.deleteCachedNote(appId, mutation.entityId)
                    true
                } else true
            }
        }
    }
}
