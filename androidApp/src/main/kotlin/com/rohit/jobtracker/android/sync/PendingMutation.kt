package com.rohit.jobtracker.android.sync

import kotlinx.serialization.Serializable

@Serializable
enum class MutationType {
    CREATE_APP,
    UPDATE_APP,
    DELETE_APP,
    ADD_NOTE,
    DELETE_NOTE
}

@Serializable
data class PendingMutation(
    val id: String,
    val type: MutationType,
    val entityId: String,
    val parentEntityId: String? = null,
    val payloadJson: String? = null,
    val createdAt: Long = 0L,
    val retryCount: Int = 0
)

enum class SyncState {
    SYNCED,
    SYNCING,
    OFFLINE_PENDING
}
