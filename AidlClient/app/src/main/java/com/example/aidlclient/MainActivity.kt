package com.example.aidlclient

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aidlclient.databinding.ActivityMainBinding
import com.example.aidlservice.FileItem
import com.example.aidlservice.IRemoteFileService
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fileAdapter: FileAdapter

    private var service: IRemoteFileService? = null
    private var isServiceBound = false
    private var loggedInUser: String? = null
    private val TAG = "ClientMain"

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = IRemoteFileService.Stub.asInterface(binder)
            isServiceBound = true
            Log.d(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            isServiceBound = false
            Log.d(TAG, "Service disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bind to the AIDL service
        val intent = Intent("com.example.aidlservice.DataHandlingService")
        intent.setPackage("com.example.aidlservice")
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        setupRecyclerView()

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            if (username.isNotBlank() && password.isNotBlank()) {
                try {
                    val success = service?.register(username, password) ?: false
                    Toast.makeText(this, if (success) "Registered" else "User already exists", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Registration failed", e)
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            if (username.isNotBlank() && password.isNotBlank()) {
                try {
                    val success = service?.login(username, password) ?: false
                    if (success) {
                        loggedInUser = username
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        showFileSection(true)
                        loadAccessibleFiles()
                    } else {
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Login failed", e)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter { fileItem ->
            openFile(fileItem)
        }
        binding.rvFiles.layoutManager = LinearLayoutManager(this)
        binding.rvFiles.adapter = fileAdapter
    }

    private fun loadAccessibleFiles() {
        val user = loggedInUser ?: return
        try {
            val files = service?.getAccessibleFiles(user)
            files?.let {
                fileAdapter.submitList(it)
                Log.d(TAG, "Files loaded: ${it.map { f -> f.name }}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load files", e)
        }
    }

    private fun openFile(fileItem: FileItem) {
        try {
            // Use the file's path to create a File instance
            val file = File(fileItem.path)

            // Convert the file to a Uri using FileProvider
            val uri = FileProvider.getUriForFile(
                this,
                "com.example.aidlclient.fileprovider", // Make sure this matches the authority defined in the provider
                file
            )

            Log.d(TAG, "Trying to open file via URI: $uri")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, contentResolver.getType(uri) ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                startActivity(Intent.createChooser(intent, "Open file with"))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open file", e)
                Toast.makeText(this, "Cannot open file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open file", e)
            Toast.makeText(this, "Cannot open file", Toast.LENGTH_SHORT).show()
        }
    }




    private fun showFileSection(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        binding.rvFiles.visibility = visibility
        binding.tvFilesLabel.visibility = visibility
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}