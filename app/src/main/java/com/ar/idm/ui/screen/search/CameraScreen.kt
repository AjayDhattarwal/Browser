package com.ar.idm.ui.screen.search

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ar.idm.R
import com.ar.idm.ui.components.PermissionHandler
import com.ar.idm.ui.navigation.AppDestination
import com.ar.idm.viewmodel.SSViewModel
import com.google.common.util.concurrent.ListenableFuture
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    navigateBack: () -> Unit,
    searchImage: (File) -> Unit,
    navigate: (AppDestination) -> Unit
) {
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val file = uriToFile(it, context)
                file?.let {
                    searchImage(file)
                }

            }
        }
    )


    val observer = LocalLifecycleOwner.current

    Box(modifier = Modifier.fillMaxSize().background(Color.Red)) {
        PermissionHandler(
            permission = "android.permission.CAMERA",
            minSdkVersion = Build.VERSION_CODES.M
        )
        AndroidView(
            factory = {

                val previewView = PreviewView(context)
                val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                    ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    bindPreview(cameraProvider, previewView, observer)

                    imageCapture = ImageCapture.Builder().build()
                    cameraProvider.bindToLifecycle(observer, CameraSelector.DEFAULT_BACK_CAMERA, imageCapture)

                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Button(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_image),
                    contentDescription = "Image Picker",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(0.8f))
            Button(
                onClick = {
                    captureImage(imageCapture, context) { file ->
                        searchImage(file)
                    }
                },
                modifier = Modifier.size(64.dp)
            ) {
                Text(text = "Capture")
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun bindPreview(
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    observer: LifecycleOwner
) {
    val preview = Preview.Builder().build()
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    preview.setSurfaceProvider(previewView.surfaceProvider)

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            observer, cameraSelector, preview
        )
    } catch (exc: Exception) {
        Log.e("CameraScreen", "Use case binding failed", exc)
    }
}

private fun captureImage(imageCapture: ImageCapture?, context: Context, onImageSaved: (File) -> Unit) {
    if (imageCapture == null) return

    val photoFile = File.createTempFile("temp_image_", ".jpg", context.externalCacheDir)

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    // Capture the image
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                onImageSaved(photoFile)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Image capture failed", exception)
            }
        }
    )
}

fun uriToFile(uri: Uri, context: Context): File? {
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                val displayName = it.getString(displayNameIndex)
                val file = File(context.cacheDir, displayName)
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    return file
                } catch (e: IOException) {
                    // Handle exceptions
                }
            }
        }
    }
    return null
}
