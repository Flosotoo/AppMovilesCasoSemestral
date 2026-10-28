package com.example.teqmed.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

// Genera URIs seguras dentro del almacenamiento interno privado vía FileProvider
// exclusivamente para comprobantes de peaje, verificando permisos y decodificando miniaturas de forma segura.

object CameraHelper {

    fun tienePermisoCamara(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

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

    fun cargarBitmapDesdeUri(context: Context, uriString: String): ImageBitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2
            }
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)?.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }
}
