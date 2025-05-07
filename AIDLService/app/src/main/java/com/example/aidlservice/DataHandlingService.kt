package com.example.aidlservice

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

class DataHandlingService : Service() {

    companion object {
        lateinit var serviceStub: IRemoteFileService
    }

    private val users = mutableMapOf<String, String>()
    private val fileAccessMap = mutableMapOf<String, MutableList<FileItem>>()
    private val logs = mutableListOf<String>()

    private val binder = object : IRemoteFileService.Stub() {

        override fun register(username: String?, password: String?): Boolean {
            if (username == null || password == null) return false
            if (users.containsKey(username)) return false

            users[username] = password
            fileAccessMap[username] = getSampleFilesForUser(username)
            return true
        }

        override fun login(username: String?, password: String?): Boolean {
            return users[username] == password
        }

        override fun getAccessibleFiles(username: String?): List<FileItem> {
            return fileAccessMap[username] ?: emptyList()
        }

        override fun logAction(username: String?, message: String?) {
            val logMessage = "[$username] $message"
            logs.add(logMessage)
            Log.d("RemoteLogger", logMessage)
        }

        override fun addFileForUser(username: String?, file: FileItem?) {
            if (username == null || file == null) return

            val userFiles = fileAccessMap[username] ?: mutableListOf()
            userFiles.add(file)
            fileAccessMap[username] = userFiles

            // Use FileProvider to generate URI for the file and persist permission
            file.uri?.let {
                try {
                    val uri = Uri.parse(it)
                    // Persist URI permission for the file
                    applicationContext.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Log.d("RemoteLogger", "Persisted URI permission for $uri")
                } catch (e: SecurityException) {
                    Log.e("RemoteLogger", "Failed to persist URI permission: ${e.message}", e)
                }
            }

            val logMessage = "[$username] File added: ${file.name}"
            logs.add(logMessage)
            Log.d("RemoteLogger", logMessage)
        }

        override fun updateFileName(username: String, fileUri: String, newName: String) {
            val userFiles = fileAccessMap[username]
            val fileToUpdate = userFiles?.find { it.uri == fileUri }
            fileToUpdate?.name = newName
        }

        // Helper method to create a URI using FileProvider
        private fun getFileUri(username: String, fileName: String): Uri? {
            val file = File(applicationContext.filesDir, fileName)  // Using internal storage for the example
            return if (file.exists()) {
                FileProvider.getUriForFile(applicationContext, "com.example.aidlservice.fileprovider", file)
            } else {
                null
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        serviceStub = binder
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun getSampleFilesForUser(username: String): MutableList<FileItem> {
        // Return a list of sample files for the user
        return fileAccessMap[username] ?: mutableListOf()
    }

    // Helper function to add files to a user (called from the client)
    fun addFileToUser(username: String, file: FileItem) {
        val userFiles = fileAccessMap[username] ?: mutableListOf()
        userFiles.add(file)
        fileAccessMap[username] = userFiles
        Log.d("RemoteLogger", "[$username] Added file from Service UI: ${file.name}")
    }
}
