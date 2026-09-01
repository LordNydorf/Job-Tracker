package com.rohit.jobtracker.android.cache

import android.content.Context
import com.rohit.jobtracker.android.sync.PendingMutation
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class LocalApplicationStore(private val filesDir: File) {

    constructor(context: Context) : this(context.filesDir)

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val appsFile = File(filesDir, "cached_applications.json")
    private val notesFile = File(filesDir, "cached_notes.json")
    private val mutationsFile = File(filesDir, "pending_mutations.json")
    private val lock = Any()

    private val _pendingMutationsCount = MutableStateFlow(0)
    val pendingMutationsCount: StateFlow<Int> = _pendingMutationsCount.asStateFlow()

    private val _applicationsFlow = MutableStateFlow<List<Application>>(emptyList())
    val applicationsFlow: StateFlow<List<Application>> = _applicationsFlow.asStateFlow()

    init {
        synchronized(lock) {
            _pendingMutationsCount.value = getPendingMutationsInternal().size
            _applicationsFlow.value = getCachedApplicationsInternal()
        }
    }

    // ==========================================
    // Applications Cache
    // ==========================================

    private fun getCachedApplicationsInternal(): List<Application> {
        if (!appsFile.exists()) return emptyList()
        return try {
            val raw = appsFile.readText()
            json.decodeFromString<List<Application>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getCachedApplications(): List<Application> = synchronized(lock) {
        getCachedApplicationsInternal()
    }

    fun saveApplications(apps: List<Application>) = synchronized(lock) {
        try {
            val raw = json.encodeToString(apps)
            val temp = File(appsFile.parentFile, "cached_applications.tmp")
            temp.writeText(raw)
            if (temp.exists()) {
                if (appsFile.exists()) appsFile.delete()
                temp.renameTo(appsFile)
            }
            _applicationsFlow.value = apps
        } catch (_: Exception) {}
    }

    fun getCachedApplication(id: String): Application? = synchronized(lock) {
        return getCachedApplications().find { it.id == id }
    }

    fun saveOrUpdateApplication(app: Application) = synchronized(lock) {
        val current = getCachedApplications().toMutableList()
        val index = current.indexOfFirst { it.id == app.id }
        if (index >= 0) {
            current[index] = app
        } else {
            current.add(0, app)
        }
        saveApplications(current)
    }

    fun deleteCachedApplication(id: String) = synchronized(lock) {
        val current = getCachedApplications().filter { it.id != id }
        saveApplications(current)

        // Cascade delete notes in cache as well
        try {
            if (notesFile.exists()) {
                val currentMap = json.decodeFromString<Map<String, List<Note>>>(notesFile.readText()).toMutableMap()
                if (currentMap.remove(id) != null) {
                    val raw = json.encodeToString(currentMap)
                    val temp = File(notesFile.parentFile, "cached_notes.tmp")
                    temp.writeText(raw)
                    if (temp.exists()) {
                        if (notesFile.exists()) notesFile.delete()
                        temp.renameTo(notesFile)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    // ==========================================
    // Notes Cache
    // ==========================================

    fun getCachedNotes(applicationId: String): List<Note> = synchronized(lock) {
        if (!notesFile.exists()) return emptyList()
        return try {
            val raw = notesFile.readText()
            val allNotes = json.decodeFromString<Map<String, List<Note>>>(raw)
            allNotes[applicationId] ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveNotes(applicationId: String, notes: List<Note>) = synchronized(lock) {
        try {
            val currentMap = if (notesFile.exists()) {
                try {
                    json.decodeFromString<Map<String, List<Note>>>(notesFile.readText()).toMutableMap()
                } catch (e: Exception) {
                    mutableMapOf()
                }
            } else {
                mutableMapOf()
            }
            currentMap[applicationId] = notes
            val raw = json.encodeToString(currentMap)
            val temp = File(notesFile.parentFile, "cached_notes.tmp")
            temp.writeText(raw)
            if (temp.exists()) {
                if (notesFile.exists()) notesFile.delete()
                temp.renameTo(notesFile)
            }
        } catch (_: Exception) {}
    }

    fun addCachedNote(applicationId: String, note: Note) = synchronized(lock) {
        val existing = getCachedNotes(applicationId).toMutableList()
        existing.add(0, note)
        saveNotes(applicationId, existing)
    }

    fun deleteCachedNote(applicationId: String, noteId: String) = synchronized(lock) {
        val existing = getCachedNotes(applicationId).filter { it.id != noteId }
        saveNotes(applicationId, existing)
    }

    // ==========================================
    // Pending Mutations Queue
    // ==========================================

    private fun getPendingMutationsInternal(): List<PendingMutation> {
        if (!mutationsFile.exists()) return emptyList()
        return try {
            val raw = mutationsFile.readText()
            json.decodeFromString<List<PendingMutation>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getPendingMutations(): List<PendingMutation> = synchronized(lock) {
        getPendingMutationsInternal()
    }

    fun enqueueMutation(mutation: PendingMutation) = synchronized(lock) {
        val current = getPendingMutationsInternal().toMutableList()
        current.add(mutation)
        saveMutationsInternal(current)
    }

    fun removeMutation(mutationId: String) = synchronized(lock) {
        val current = getPendingMutationsInternal().filter { it.id != mutationId }
        saveMutationsInternal(current)
    }

    fun clearPendingMutations() = synchronized(lock) {
        saveMutationsInternal(emptyList())
    }

    private fun saveMutationsInternal(mutations: List<PendingMutation>) {
        try {
            val raw = json.encodeToString(mutations)
            val temp = File(mutationsFile.parentFile, "pending_mutations.tmp")
            temp.writeText(raw)
            if (temp.exists()) {
                if (mutationsFile.exists()) mutationsFile.delete()
                temp.renameTo(mutationsFile)
            }
            _pendingMutationsCount.value = mutations.size
        } catch (_: Exception) {}
    }
}
