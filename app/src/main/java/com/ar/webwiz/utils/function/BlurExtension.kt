package com.ar.webwiz.utils.function

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Applies a blur effect to the background using RenderEffect.
 * Works on Android API 31+ (Android 12) and supports rounded shapes.
 *
 * @param radius The radius of the blur.
 * @param shape The shape to clip the blurred area to, defaults to RoundedCornerShape(0).
 */
@SuppressLint("SuspiciousModifierThen")
fun Modifier.applyBlur(radius: Float, shape: Shape = RoundedCornerShape(0)) = this.then(
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        graphicsLayer {
            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                radius, radius, android.graphics.Shader.TileMode.CLAMP
            ).asComposeRenderEffect()
            clip = true
            this.shape = shape
        }
    } else this
)