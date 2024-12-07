@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalSharedTransitionApi::class
)

package com.ar.webwiz.ui.screen.search

import android.content.Intent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ar.webwiz.R
import com.ar.webwiz.data.remote.model.SuggestionItem
import com.ar.webwiz.ui.screen.menu.settings.components.CurrentUrlCard
import com.ar.webwiz.ui.screen.menu.settings.components.SearchField
import com.ar.webwiz.utils.function.extractStringBtwBTab
import com.ar.webwiz.utils.function.sanitizeString
import com.ar.webwiz.ui.navigation.AppDestination
import com.ar.webwiz.viewmodel.SSViewModel
import org.koin.androidx.compose.getViewModel


@Composable
fun SearchScreen(
    data: AppDestination.Search,
    onSearch: (String) -> Unit,
    navigate: (AppDestination) -> Unit,
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope,
    searchViewModel: SSViewModel = getViewModel(),
    navigateBack: () -> Unit,
) {
    val searchTextValue =  searchViewModel.searchText
    val textFieldValue by searchTextValue.collectAsState()
    val suggestions by searchViewModel.searchSuggestions.collectAsState()

    val googleLens = "intent://search.app.goo.gl/?link=https://goo.gl/iosgoogleapp/default?url%3Dgoogleapp%253A%252F%252Flens%253Fmin-version%253D180%2526lens_data%253DKAw&al=googleapp://lens?lens_data%3DKAw&apn=com.google.android.googlequicksearchbox&amv=301204913&ofl=https://lens.google&isi=284815942&ibi=com.google.GoogleMobile&ius=googleapp&ifl=https://apps.apple.com/us/app/google/id284815942?ppid%3D1ac8cc35-d99c-4a1d-b909-321c8968cc74%26pt%3D9008%26mt%3D8%26ct%3D4815459-oo-lens-isb-bar-lens-cam%26UTM_campaign%3Dgoogle_search_mweb&utm_campaign=4815459-oo-lens-isb-bar-lens-cam&utm_medium=owned&utm_source=google_search_mweb&ct=4815459-oo-lens-isb-bar-lens-cam&mt=8&pt=9008&efr=1#Intent;package=com.google.android.gms;scheme=https;S.browser_fallback_url=https://play.google.com/store/apps/details%3Fid%3Dcom.google.android.googlequicksearchbox&pcampaignid%3Dfdl_long&url%3Dgoogleapp://lens%3Flens_data%253DKAw&min_version%3D301204913;end;"
    val context  = LocalContext.current

    LaunchedEffect(Unit){
        searchViewModel.clearText()
    }


    with(sharedTransitionScope){
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.Top
            ) {

                SearchField(
                    textState = searchTextValue,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .sharedBounds(
                            rememberSharedContentState(key = "searchBar"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        ),
                    leadingIcon = {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.size(20.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.google_icon),
                                contentDescription = "Google Icon",
                                tint = Color.Unspecified
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.size(20.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.google_mic),
                                contentDescription = "Mic",
                                tint = Color.Unspecified
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = {
                                val intent = Intent.parseUri(googleLens, Intent.URI_INTENT_SCHEME)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(20.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.google_lens),
                                contentDescription = "Google lens",
                                tint = Color.Unspecified
                            )
                        }
                    },
                    onSearch = onSearch,
                    onValueChange = {
                        searchViewModel.onSearchTextChange(it)
                    }
                )


                CurrentUrlCard(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    textState = searchTextValue,
                    data = data,
                    updateText = {
                        searchViewModel.onSearchTextChange(
                            textFieldValue.copy(
                                text = it,
                                selection = TextRange(it.length)
                            )
                        )
                    },
                    navigateBack = navigateBack,
                )


                Spacer (modifier = Modifier.height (16. dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(suggestions,key = { index, item -> item.title.toString() + index.toString()}) { index, item ->

                        val shape = if(index == 0){
                            RoundedCornerShape(topEndPercent = 20, topStartPercent = 20)
                        } else if(index == suggestions.size - 1){
                            RoundedCornerShape(bottomEndPercent = 20, bottomStartPercent = 20)
                        } else{
                            RoundedCornerShape(0)
                        }


                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .wrapContentHeight()
                                .fillMaxWidth(),
                            shape = shape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            SearchSuggestionItem(
                                showArrow = {
                                    item.title != null
                                            && !item.title
                                        .toString()
                                        .equals(textFieldValue.text, ignoreCase = true)
                                },
                                suggestionItem = item,
                                textCallBack = {

                                    val newText = textFieldValue.text + it
                                    val newTextValue = textFieldValue.copy(
                                        text = newText,
                                        selection = TextRange(newText.length)
                                    )
                                    searchViewModel.onSearchTextChange(newTextValue)
                                },
                                onSearch = onSearch
                            )
                        }
                        Spacer(
                            Modifier
                                .height(1.dp)
                                .background(Color.LightGray)
                        )
                    }
                }
            }
        }
    }

}


@Composable
fun SearchSuggestionItem(
    showArrow: () -> Boolean,
    suggestionItem: SuggestionItem,
    textCallBack: (String) -> Unit,
    onSearch: (String) -> Unit
) {


    val annotatedTitle: AnnotatedString? = suggestionItem.details?.title?.let { title ->
        buildAnnotatedString {
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface.copy(0.5f))) {
                append(suggestionItem.title?.substringBefore("<").sanitizeString())
            }
            suggestionItem.title?.extractStringBtwBTab()?.let{
                withStyle(style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )) {
                    append(it.sanitizeString())
                }
            }
        }
    }

    val annotatedTitle2 = buildAnnotatedString {

        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface.copy(0.5f))) {
            append(suggestionItem.title?.substringBefore("<").sanitizeString())
        }
        suggestionItem.title?.extractStringBtwBTab()?.let{
            withStyle(style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )) {
                append(it.sanitizeString())
            }
        }
    }

    val image = suggestionItem.details?.icon



    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 8.dp)
            .padding(end = 8.dp)
            .clickable {
                onSearch(
                    annotatedTitle?.text ?: annotatedTitle2.text
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(60.dp)
                .clip(RoundedCornerShape(5)),
            contentAlignment = Alignment.Center
        ){
            if(image != null) {
                Image(
                    painter = rememberAsyncImagePainter(image),
                    contentDescription = "Icon",
                    modifier = Modifier
                        .clip(RoundedCornerShape(10))
                        .size(40.dp)

                )
            }else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Icon",
                    modifier = Modifier,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }


        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.Start
        ){
            Text(
                text =  annotatedTitle?: annotatedTitle2,
                modifier = Modifier,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            suggestionItem.details?.subtitle?.sanitizeString()?.let {
                Text(
                    text = it,
                    modifier = Modifier,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }


        if(showArrow() && (suggestionItem.title?.extractStringBtwBTab() != null || suggestionItem.details?.title?.extractStringBtwBTab() != null)  ){
            IconButton(
                onClick = {
                    textCallBack(
                        suggestionItem.details?.title?.extractStringBtwBTab().sanitizeString()
                        ?: suggestionItem.title?.extractStringBtwBTab().sanitizeString().toString()
                    )
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.diagonal_arrow_left_up),
                    contentDescription = "add to text Field",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }


    }

}


