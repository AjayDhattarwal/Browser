package com.ar.webwiz.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ar.webwiz.R

class SearchWidget: GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                MyContent()
            }
        }
    }
    @Composable
    private fun MyContent() {
        val destinationKey = ActionParameters.Key<String>(
            "search"
        )
        val searchActionParameters = actionParametersOf(destinationKey to "text")
        val micActionParameters = actionParametersOf(destinationKey to "mic"
        )
        val lensActionParameters = actionParametersOf(destinationKey to "lens"
        )
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Row(
                modifier = GlanceModifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .background(colorProvider = GlanceTheme.colors.surface)
                    .clickable(
                        actionRunCallback<SearchActionWidget>(searchActionParameters)
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Search or Type Url",
                    modifier = GlanceModifier.padding(16.dp).fillMaxWidth().defaultWeight(),
                    style = TextStyle(color = GlanceTheme.colors.onSurface)
                )

                Image(
                    provider = ImageProvider(R.drawable.google_mic),
                    contentDescription = "mic",
                    modifier = GlanceModifier
                        .size(20.dp)
                        .clickable(
                            actionRunCallback<SearchActionWidget>(micActionParameters)
                        )
                )
                Spacer(modifier = GlanceModifier.width(12.dp))
                Image(
                    provider = ImageProvider(R.drawable.google_lens),
                    contentDescription = "lens",
                    modifier = GlanceModifier
                        .size(20.dp)
                        .clickable(
                            actionRunCallback<SearchActionWidget>(lensActionParameters)
                        )
                )
                Spacer(modifier = GlanceModifier.width(16.dp))
            }
        }
    }
}



