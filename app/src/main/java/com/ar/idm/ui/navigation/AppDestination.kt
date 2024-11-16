package com.ar.idm.ui.navigation

import kotlinx.serialization.Serializable

sealed class AppDestination {
    @Serializable
    data object Home : AppDestination()

    @Serializable
    data class Search(
        val url: String? = null,
        val title: String? = null,
        val favIconUrl: String? = null
    ) : AppDestination()


    @Serializable
    data object Settings : AppDestination()

    @Serializable
    data object DownloadSetting : AppDestination()

    @Serializable
    data object Bookmarks : AppDestination()

    @Serializable
    data object CameraScreen : AppDestination()

    @Serializable
    data object History : AppDestination()

    @Serializable
    data object Downloads : AppDestination()

    @Serializable
    data object Tabs : AppDestination()

    @Serializable
    data object About : AppDestination()

    @Serializable
    data object HelpAndFeedback : AppDestination()

    @Serializable
    data object Privacy : AppDestination()

    @Serializable
    data object TermsAndConditions : AppDestination()

    @Serializable
    data object License : AppDestination()

    @Serializable
    data object AboutApp : AppDestination()

    @Serializable
    data object AboutDeveloper : AppDestination()

    @Serializable
    data object Personalization: AppDestination()

    @Serializable
    data object PasswordManager: AppDestination()

    @Serializable
    data object PaymentMethods: AppDestination()

    @Serializable
    data object Notifications: AppDestination()

    @Serializable
    data object ClearBrowsingData: AppDestination()

    @Serializable
    data object SearchSettings: AppDestination()

    @Serializable
    data object AdBlocking: AppDestination()

    @Serializable
    data object PageLayout: AppDestination()

    @Serializable
    data object Language: AppDestination()

    @Serializable
    data object SearchEngine: AppDestination()

    @Serializable
    data object SiteSettings: AppDestination()

    @Serializable
    data object Accessibility: AppDestination()

    @Serializable
    data object Widget: AppDestination()

    @Serializable
    data object PrivacyPolicy: AppDestination()





}