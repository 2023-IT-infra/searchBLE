package com.tuk.searchble.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null
    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst()) {
            fileName = it.getString(nameIndex)
        }
    }
    return fileName
}