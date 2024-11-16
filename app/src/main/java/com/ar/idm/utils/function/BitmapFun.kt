package com.ar.idm.utils.function

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.write
import kotlin.io.path.exists



fun ImageBitmap.storeToFile(file: File, quality: Int = 70) {
    file.parentFile?.mkdirs()

    try {
        val bos = ByteArrayOutputStream()
        this.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, quality, bos)
        val bitmapData = bos.toByteArray()

        FileOutputStream(file).use { fos ->
            fos.write(bitmapData)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}


fun File.retrieveImageBitmap(): ImageBitmap? {
    if (!this.exists()) {
        return null
    }

    val bitmap = BitmapFactory.decodeFile(this.absolutePath)
    return bitmap?.asImageBitmap()
}