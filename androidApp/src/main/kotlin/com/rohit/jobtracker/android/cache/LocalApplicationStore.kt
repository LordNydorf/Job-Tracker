package com.rohit.jobtracker.android.cache

import android.content.Context
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.Note
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class LocalApplicationStore(context: Context) {

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val appsFile = File(context.filesDir, "cached_applications.json")
    private val notesFile = File(context.filesDir, "cached_notes.json")
    private val lock = Any()

    fun getCachedApplications(): List<Application> = synchronized(lock) {
        if (!appsFile.exists()) return emptyList()
        return try {
            val raw = appsFile.readText()
            json.decodeFromString<List<Application>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
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
    }

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
}
