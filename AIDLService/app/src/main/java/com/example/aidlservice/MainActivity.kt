package com.example.aidlservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aidlservice.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fileAdapter: FileAdapter
    private var loggedInUser: String? = null
    private val TAG = "Check"
    private var isServiceConnected = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            DataHandlingService.serviceStub = IRemoteFileService.Stub.asInterface(service)
            isServiceConnected = true
            Log.d(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceConnected = false
            Log.d(TAG, "Service disconnected")
        }
    }

    // ✅ Use OpenDocument for persistable permissions
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            handlePickedFile(uri)
        } else {
            Log.d(TAG, "No file selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent("com.example.aidlservice.DataHandlingService")
        intent.setPackage("com.example.aidlservice")
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        // Hide initially
        binding.btnAddFile.visibility = View.GONE
        binding.rvFiles.visibility = View.GONE
        binding.tvFilesLabel.visibility = View.GONE


        fileAdapter = FileAdapter { fileItem, action ->
            when (action){
                FileAdapter.ActionType.VIEW -> openFile(fileItem)
                FileAdapter.ActionType.EDIT -> editFileName(fileItem)
            }

        }
        binding.rvFiles.layoutManager = LinearLayoutManager(this)
        binding.rvFiles.adapter = fileAdapter

        binding.btnRegister.setOnClickListener {
            if (!isServiceConnected) return@setOnClickListener

            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            try {
                val success = DataHandlingService.serviceStub.register(username, password)
                Log.d(TAG, if (success) "Registered user: $username" else "User already exists: $username")
            } catch (e: Exception) {
                Log.e(TAG, "Registration failed: ${e.message}", e)
            }
        }

        binding.btnLogin.setOnClickListener {
            if (!isServiceConnected) return@setOnClickListener

            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            try {
                val success = DataHandlingService.serviceStub.login(username, password)
                if (success) {
                    loggedInUser = username
                    Log.d(TAG, "Logged in as $username")

                    // ✅ Make these views visible after login
                    binding.btnAddFile.visibility = View.VISIBLE
                    binding.rvFiles.visibility = View.VISIBLE
                    binding.tvFilesLabel.visibility = View.VISIBLE

                    updateFileList()
                } else {
                    Log.d(TAG, "Login failed for user: $username")
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}", e)
            }
        }

        binding.btnAddFile.setOnClickListener {
            if (!isServiceConnected || loggedInUser == null) {
                Log.d(TAG, "Add file failed: not logged in or service not connected")
                return@setOnClickListener
            }

            pickFileLauncher.launch(arrayOf("*/*")) // ✅ Launch with MIME type array
        }
    }

    private fun handlePickedFile(uri: Uri) {
        val user = loggedInUser ?: return

        val fileName = getFileNameFromUri(uri)

        try {
            // Step 1: Copy content to a local file
            val inputStream = this@MainActivity.contentResolver.openInputStream(uri)
            val file = File(this.filesDir, fileName)
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Step 2: Generate a FileProvider URI for the new file
            val fileProviderUri = FileProvider.getUriForFile(
                this,
                "com.example.aidlservice.fileprovider",  // Make sure this matches your manifest authority
                file
            )


            // Step 3: Store this FileProvider URI
            val newFile = FileItem(
                name = fileName,
                path = file.absolutePath,
                isDirectory = false,
                uri = fileProviderUri.toString()
            )

            // Step 4: Add to service data
            DataHandlingService.serviceStub.addFileForUser(user, newFile)
            Log.d(TAG, "File added for $user: ${newFile.name}")
            updateFileList()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle picked file: ${e.message}", e)
            Toast.makeText(this, "Failed to add file", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getFileNameFromUri(uri: Uri): String {
        var name = "unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun updateFileList() {
        if (!isServiceConnected) return

        val user = loggedInUser ?: return
        try {
            val files = DataHandlingService.serviceStub.getAccessibleFiles(user)
            fileAdapter.submitList(files.toList())
            Log.d(TAG, "File list updated for user $user. Files: ${files.map { it.name }}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load files: ${e.message}", e)
        }
    }

    private fun openFile(fileItem: FileItem){
        val uri = Uri.parse(fileItem.uri)
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, contentResolver.getType(uri) ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open file: ${e.message}", e)
        }
    }

    private fun editFileName(fileItem: FileItem) {
        val context = this
        val editText = android.widget.EditText(context).apply {
            setText(fileItem.name)
        }

        android.app.AlertDialog.Builder(context)
            .setTitle("Edit File Name")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    try {
                        // ⚠️ You may want to update the backend too
                        val updatedFile = fileItem.copy(name = newName)
                        val user = loggedInUser ?: return@setPositiveButton
                        DataHandlingService.serviceStub.updateFileName(user, fileItem.uri, newName) // You'd need this in your AIDL!
                        updateFileList()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to update filename: ${e.message}", e)
                        Toast.makeText(this, "Failed to update file name", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceConnected) {
            unbindService(connection)
            isServiceConnected = false
        }
    }
}