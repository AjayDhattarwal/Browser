package com.ar.webwiz.utils.function

import android.os.Bundle
import android.os.Parcel
import android.util.Base64
import com.ar.webwiz.domain.repository.TabInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


suspend fun File.readBundleFromFile(): Bundle? {
    return withContext(Dispatchers.IO){
        if (exists()) {
            val json = readText()
            stringToBundle(json)
        } else {
            null
        }
    }
}



suspend fun stringToBundle(encodedString: String): Bundle = withContext(Dispatchers.IO) {
    val byteArray = Base64.decode(encodedString, Base64.NO_WRAP)
    val byteArrayInputStream = ByteArrayInputStream(byteArray)
    val objectInputStream = ObjectInputStream(byteArrayInputStream)

    val map = objectInputStream.readObject() as? Map<*, *>
    val bundle = Bundle()

    map?.forEach { (key, value) ->
        if (key is String) {
            bundle.putSerializable(key, value as? java.io.Serializable)
        }
    }

    return@withContext bundle
}

suspend fun bundleToString(bundle: Bundle): String {
    return withContext(Dispatchers.IO){
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)

            val map = mutableMapOf<String, Any?>()
            for (key in bundle.keySet()) {
                map[key] = bundle[key]
            }

            objectOutputStream.writeObject(map)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}

fun readTabInfoFromFile(file: File): TabInfo? {
    val parcel = Parcel.obtain()
    return try {

        val byteArray = FileInputStream(file).use { it.readBytes() }


        parcel.unmarshall(byteArray, 0, byteArray.size)
        parcel.setDataPosition(0)


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(TabInfo::class.java.classLoader, TabInfo::class.java)
        } else {

            @Suppress("DEPRECATION")
            parcel.readParcelable(TabInfo::class.java.classLoader)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } finally {
        parcel.recycle()
    }
}

