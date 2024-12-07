@file:OptIn(ExperimentalMaterial3Api::class)

package com.ar.webwiz.ui.screen.menu.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ar.webwiz.R
import com.ar.webwiz.data.local.preferences.PreferencesManager
import com.ar.webwiz.ui.components.CommonTextFrame
import com.ar.webwiz.ui.components.TopBar
import com.ar.webwiz.ui.components.VisualContentBlock
import com.ar.webwiz.ui.navigation.AppDestination
import com.ar.webwiz.utils.function.getDefaultBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navigate: (AppDestination) -> Unit,
    navigateBack: () -> Unit,
){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { PreferencesManager(context = context) }
    val userProfile = preferencesManager.getUserProfile()
    var syncStatus by remember { mutableStateOf(preferencesManager.getDataSyncStatus()) }
    val searchEngine = preferencesManager.getSearchEngine()
    val adsBlocking = preferencesManager.getAdsBlocked()
    val defaultBrowserApp by remember { derivedStateOf{
        getDefaultBrowser(context)
    }}

    Scaffold(
        topBar = {
            TopBar(
                title = "Settings",
                navigateBack = navigateBack,
                menuAction = {}
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 5.dp)
        ) {

            item{
                Text(
                    text = "You and Google",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            item {
                userProfile.userEmail?.let {
                    VisualContentBlock(
                        modifier = Modifier.padding(vertical = 10.dp),
                        title = userProfile.userName,
                        subtitle = it,
                        image = userProfile.userImage
                    )
                }
            }

            item {
                VisualContentBlock(
                    modifier = Modifier.padding(vertical = 10.dp)
                        .clickable(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch{
                                    preferencesManager.toggleDataSyncStatus()
                                    syncStatus = preferencesManager.getDataSyncStatus()
                                }
                            },
                            interactionSource = remember { MutableInteractionSource()  },
                            indication = null
                        ),
                    title = "Sync",
                    subtitle = syncStatus,
                    image = R.drawable.ic_sync,
                    imageSize = 24.dp,
                    imageColorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                )
            }

            item{
                Text(
                    text = "General",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            item{
                CommonTextFrame(
                    title = "Search Engine",
                    subTitle = searchEngine,
                    onClick = {
                        navigate(AppDestination.SearchEngine)
                    }
                )
            }
            item{
                CommonTextFrame(
                    title = "Personalization",
                    subTitle = "Default",
                    onClick = {
                        navigate(AppDestination.Personalization)
                    }
                )
            }
            item{
                CommonTextFrame(
                    title = "Clear Browsing Data",
                    onClick = {
                        navigate(AppDestination.ClearBrowsingData)
                    }
                )
            }

            item{
                CommonTextFrame(
                    title = "Page Layout",
                    onClick = {
                        navigate(AppDestination.PageLayout)
                    }
                )
            }

            item{
                CommonTextFrame(
                    title = "Ad blocking",
                    subTitle = adsBlocking,
                    onClick = {
                        navigate(AppDestination.AdBlocking)
                    }
                )
            }

            item{
                CommonTextFrame(
                    title = "Language",
                    subTitle = "English",
                    onClick = {
                        navigate(AppDestination.Language)
                    }
                )
            }

            item{
                CommonTextFrame(
                    title = "Set as default browser",
                    subTitle = "current default : $defaultBrowserApp",
                    onClick = {
                        val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                        context.startActivity(intent)
                    }
                )
            }

            item{
                CommonTextFrame(
                    title = "Notification",
                    onClick = {
                        navigate(AppDestination.Notifications)
                    }
                )
            }

            item{
                CommonTextFrame(
                    title = "Password Manager",
                    onClick = {
                        navigate(AppDestination.PasswordManager)
                    }
                )
            }

            item{
                CommonTextFrame(
                    title = "Payment methods",
                    onClick = {
                        navigate(AppDestination.PaymentMethods)
                    }
                )
            }


            item{
                Text(
                    text = "Advanced",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }


            item{
                CommonTextFrame(
                    title = "Accessibility",
                    onClick = {
                        navigate(AppDestination.Accessibility)
                    }
                )
            }

            item{
                CommonTextFrame(
                    title = "Site settings",
                    onClick = {
                        navigate(AppDestination.SiteSettings)
                    }
                )
            }
            item{
                CommonTextFrame(
                    title = "Downloads",
                    onClick = {
                        navigate(AppDestination.DownloadSetting)
                    }
                )
            }
            item{
                CommonTextFrame(
                    title = "About ${stringResource(R.string.app_name)}",
                    onClick = {
                        navigate(AppDestination.AboutApp)
                    }
                )
            }
            item{
                CommonTextFrame(
                    title = "Help & Feedback",
                    onClick = {
                        navigate(AppDestination.HelpAndFeedback)
                    }
                )
            }

            item{
                CommonTextFrame(
                    title = "About Developer",
                    onClick = {
                        navigate(AppDestination.AboutDeveloper)
                    }
                )
            }

        }

    }
}




@Preview
@Composable
fun SettingsPreview() {
    SettingsScreen(
        navigate = {},
        navigateBack = {}
    )
}

