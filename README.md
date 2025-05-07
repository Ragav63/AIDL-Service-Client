# AIDL-Based Client-Server File Sharing App (Android)

This project demonstrates secure inter-process communication (IPC) between two Android applications using AIDL (Android Interface Definition Language). The **Service App** manages user registration, login, and file sharing, while the **Client App** interacts with the service to authenticate users and display accessible files using a `RecyclerView`.

## 🔧 Features

- AIDL-based communication between two Android apps
- User authentication (register & login) via the service
- Secure file access via `FileProvider`
- File listing and opening in the client app
- Internal file URI sharing using `content://` scheme
- Dynamic RecyclerView to show accessible files after login

## 📂 Modules

### 🟢 Service App (`com.example.aidlservice`)
- Exposes AIDL interface `IRemoteFileService.aidl`
- Manages user credentials and accessible files
- Provides file metadata (`FileItem`) via Parcelable
- Uses `FileProvider` to expose files internally

### 🔵 Client App (`com.example.aidlclient`)
- Binds to the remote service using `ServiceConnection`
- Allows user to register and login via AIDL calls
- Loads and displays accessible files after login
- Uses `FileProvider.getUriForFile()` to open files securely

## ✅ Permissions

- `android.permission.READ_EXTERNAL_STORAGE` (Client)
- Custom permission `com.example.aidlservice.permission.ACCESS` to access the service
- Uses `FileProvider` with defined `file_paths.xml`

## 📌 Requirements

- Android Studio Hedgehog or later
- Android SDK 30+
- Two separate Android apps installed (client and service)
