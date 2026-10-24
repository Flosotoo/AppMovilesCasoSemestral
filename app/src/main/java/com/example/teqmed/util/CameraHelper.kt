package com.example.teqmed.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

//Genera URIs seguras dentro del almacenamiento interno privado vía FileProvider
//exclusivamente para comprobantes de peaje

object CameraHelper {

    fun crearUriTemporal(context: Context, prefijo: String = "peaje_"): Uri {
        val tempFile = File.createTempFile(prefijo, ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }
}
