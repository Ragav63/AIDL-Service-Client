package com.example.aidlservice

import android.os.Parcel
import android.os.Parcelable

data class FileItem(
    var name: String,
    val path: String,
    val isDirectory: Boolean,
    val uri: String? = null // Optional URI for file reference
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(path)
        parcel.writeByte(if (isDirectory) 1 else 0)
        parcel.writeString(uri)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FileItem> {
        override fun createFromParcel(parcel: Parcel) = FileItem(parcel)
        override fun newArray(size: Int) = arrayOfNulls<FileItem>(size)
    }
}

