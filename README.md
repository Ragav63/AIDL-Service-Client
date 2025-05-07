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

✅ Permissions
Client App:

xml
Copy
Edit
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
Service App:

xml
Copy
Edit
<permission android:name="com.example.aidlservice.permission.ACCESS"
    android:protectionLevel="signature"/>
<uses-permission android:name="com.example.aidlservice.permission.ACCESS"/>
FileProvider Setup (both apps):

Define file_paths.xml in res/xml/

Declare FileProvider in AndroidManifest.xml

📌 Requirements
Android Studio Hedgehog or newer

Android SDK 30+

Two separate Android apps (Client & Service) installed on the device/emulator

📥 Getting Started

1️⃣ Clone the Repository
bash
Copy
Edit

git clone https://github.com/your-username/AIDLFileSharingApp.git

cd AIDLFileSharingApp

⚠️ Make sure you have both aidlclient and aidlservice subdirectories.

2️⃣ Import into Android Studio
Open Android Studio

Choose File > Open

Select the root folder of the cloned repo

Let it sync and index both modules (Client & Service)

⚠️ What to Do if build.gradle (app-level) Is Missing
If you accidentally deleted the build.gradle inside the aidlclient/ or aidlservice/ module:

Option 1: Restore Manually
Create a new build.gradle inside the aidlclient/ or aidlservice/ directory:

<details> <summary><strong>Sample app-level <code>build.gradle</code> for Client App</strong></summary>
groovy
Copy
Edit
plugins {
    id 'com.android.application'
    id 'kotlin-android'
}

android {
    namespace 'com.example.aidlclient'
    compileSdk 33

    defaultConfig {
        applicationId "com.example.aidlclient"
        minSdk 21
        targetSdk 33
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.8.0"
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.recyclerview:recyclerview:1.3.1'
}
</details>
Repeat a similar process for the Service App with its own package name (com.example.aidlservice).

Option 2: Recreate Module
Right-click on the project root in Android Studio

Click New > Module > Android Module

Name it aidlclient or aidlservice

Copy your Java/Kotlin and resource files into the newly generated module

Add AIDL interfaces under src/main/aidl/

📄 License
This project is open-source and free to use under the MIT License.

## 📌 Requirements

- Android Studio Hedgehog or later
- Android SDK 30+
- Two separate Android apps installed (client and service)
