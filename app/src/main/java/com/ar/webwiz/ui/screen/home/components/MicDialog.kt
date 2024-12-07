package com.ar.webwiz.ui.screen.home.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ar.webwiz.R

@Composable
fun AudioToSpeechDialog(
    appName: String,
    selectedLanguage: String,
    onLanguageChange: () -> Unit,
    onMicClick: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // App Name Title
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Animated Mic with Circular Background
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(150.dp) // Outer size for the animated waves
                        .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                ) {
                    // Animated Waves
                    AnimatedWaves()

                    // Mic Icon
                    Icon(
                        painter = painterResource(R.drawable.google_mic),
                        contentDescription = "Mic",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { onMicClick() }
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selected Language Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.clickable { onLanguageChange() }
                ) {
                    Text(
                        text = "Language: $selectedLanguage",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Change Language",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedWaves() {
    // This can be an animation state that makes the waves grow/shrink over time
    val infiniteTransition = rememberInfiniteTransition()
    val waveSize by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val color = MaterialTheme.colorScheme.primary.copy(0.3f)

    Canvas(modifier = Modifier.size(waveSize.dp)) {
        drawCircle(
            color = color,
            radius = size.maxDimension / 2
        )
    }
}


@Preview
@Composable
fun AudioToSpeechDialogPrev(){
    AudioToSpeechDialog(
        appName = "Audio to Speech",
        selectedLanguage = "English",
        onLanguageChange = { /* Handle language change */ },
        onMicClick = { /* Handle mic click */ }
    )
}

