@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalSharedTransitionApi::class)

package com.ar.idm.ui.screen.home.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ar.idm.R
import com.ar.idm.ui.components.CommonCardRowFrame
import com.ar.idm.ui.components.CommonImageFrame
import com.ar.idm.ui.navigation.AppDestination


@Composable
fun OverlayHomeComposable(
    modifier: Modifier,
    headingStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    isTransitionActive:Boolean = true,
    searchBarHeight: Dp = 60.dp,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigate: (AppDestination) -> Unit,
    onSearch: (String) -> Unit
) {
    val googleLens = "intent://search.app.goo.gl/?link=https://goo.gl/iosgoogleapp/default?url%3Dgoogleapp%253A%252F%252Flens%253Fmin-version%253D180%2526lens_data%253DKAw&al=googleapp://lens?lens_data%3DKAw&apn=com.google.android.googlequicksearchbox&amv=301204913&ofl=https://lens.google&isi=284815942&ibi=com.google.GoogleMobile&ius=googleapp&ifl=https://apps.apple.com/us/app/google/id284815942?ppid%3D1ac8cc35-d99c-4a1d-b909-321c8968cc74%26pt%3D9008%26mt%3D8%26ct%3D4815459-oo-lens-isb-bar-lens-cam%26UTM_campaign%3Dgoogle_search_mweb&utm_campaign=4815459-oo-lens-isb-bar-lens-cam&utm_medium=owned&utm_source=google_search_mweb&ct=4815459-oo-lens-isb-bar-lens-cam&mt=8&pt=9008&efr=1#Intent;package=com.google.android.gms;scheme=https;S.browser_fallback_url=https://play.google.com/store/apps/details%3Fid%3Dcom.google.android.googlequicksearchbox&pcampaignid%3Dfdl_long&url%3Dgoogleapp://lens%3Flens_data%253DKAw&min_version%3D301204913;end;"
    val context  = LocalContext.current

    val placeholderStyle = when(searchBarHeight){
        60.dp -> MaterialTheme.typography.bodyMedium
        else -> TextStyle.Default.copy(fontSize = 8.sp)
    }

    val websiteList = listOf(
        Webpage(
            url = "https://www.instagram.com",
            icon = "https://www.instagram.com/favicon.ico",
            title = "Instagram"
        ),
        Webpage(
            url = "https://www.facebook.com",
            icon = "https://www.facebook.com/favicon.ico",
            title = "Facebook"
        ),
        Webpage(
            url = "https://www.google.com",
            icon = "https://www.google.com/favicon.ico",
            title = "Google"
        ),
        Webpage(
            url = "https://www.yahoo.com",
            icon = "https://www.yahoo.com/favicon.ico",
            title = "Yahoo"
        ),
        Webpage(
            url = "https://www.linkedin.com",
            icon = "https://www.linkedin.com/favicon.ico",
            title = "LinkedIn"
        ),
        Webpage(
            url = "https://www.twitter.com",
            icon = "https://www.twitter.com/favicon.ico",
            title = "Twitter"
        ),
        Webpage(
            url = "https://www.youtube.com",
            icon = "https://www.youtube.com/favicon.ico",
            title = "YouTube"
        ),
        Webpage(
            url = "https://www.amazon.com",
            icon = "https://www.amazon.com/favicon.ico",
            title = "Amazon"
        ),
        Webpage(
            url = "https://www.netflix.com",
            icon = "https://www.netflix.com/favicon.ico",
            title = "Netflix"
        ),
        Webpage(
            url = "https://www.spotify.com",
            icon = "https://www.spotify.com/favicon.ico",
            title = "Spotify"
        ),
        Webpage(
            url = "https://www.openai.com/chatgpt",
            icon = "https://www.openai.com/favicon.ico",
            title = "ChatGPT"
        ),
        Webpage(
            url = "https://www.github.com",
            icon = "https://www.github.com/favicon.ico",
            title = "GitHub"
        )
    )

    val scrollState = rememberScrollState()

    with(sharedTransitionScope) {
        val searchFieldModifier = if(isTransitionActive){
            Modifier.sharedBounds(
                rememberSharedContentState(key = "searchBar"),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }else{
            Modifier
        }
        Surface(
            modifier = modifier
                .fillMaxSize(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(searchBarHeight/3.75f)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(searchBarHeight/3.75f))

                CommonCardRowFrame(
                    contentHorizontalPadding = searchBarHeight/3.75f,
                    modifier = searchFieldModifier
                        .clickable (
                            onClick = { onNavigate(AppDestination.Search()) },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .height(searchBarHeight),
                    content = {
                        Text(
                            text = "Search or type Url",
                            style = placeholderStyle,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.padding(start = 8.dp).fillMaxWidth().weight(1f)
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.size(searchBarHeight/3),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.google_mic),
                                contentDescription = "Mic",
                                tint = Color.Unspecified
                            )
                        }
                        Spacer(modifier = Modifier.width(searchBarHeight/6))
                        IconButton(
                            onClick = {
                                val intent = Intent.parseUri(googleLens, Intent.URI_INTENT_SCHEME)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(searchBarHeight/3),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.google_lens),
                                contentDescription = "Google lens",
                                tint = Color.Unspecified
                            )
                        }
                    },
                )

                Spacer(modifier = Modifier.height(searchBarHeight/3.75f))

                WebsiteShortcuts(
                    list = websiteList,
                    onClick = onSearch
                )

            }


        }
    }

}

data class Webpage(
    val url: String,
    val icon: String,
    val title: String
)





@Composable
fun WebsiteShortcuts(list: List<Webpage>, onClick: (String) -> Unit){

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(70.dp),
        userScrollEnabled = false,
        modifier = Modifier.height(220.dp),
        verticalItemSpacing = 16.dp
    ) {
        items(list){
            CommonImageFrame(
                modifier = Modifier,
                model = it.icon,
                imageSize = 40.dp,
                title = it.title
            ){
                onClick(it.url)
            }
        }

        item{
            CommonImageFrame(
                modifier = Modifier,
                model = R.drawable.ic_add,
                imageSize = 40.dp,
                title = "Add"
            ){

            }
        }
    }

}


