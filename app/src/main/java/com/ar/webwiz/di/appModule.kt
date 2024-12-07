package com.ar.webwiz.di

import com.ar.webwiz.data.local.roomdatabase.bookmarkDb.BookmarkDatabase
import com.ar.webwiz.domain.repository.SearchRepository
import com.ar.webwiz.utils.download.DownloadRepository
import com.ar.webwiz.data.local.roomdatabase.downloadDb.DownloadDatabase
import com.ar.webwiz.data.local.roomdatabase.historyDb.HistoryDatabase
import com.ar.webwiz.data.local.roomdatabase.tabstate.TabDatabase
import com.ar.webwiz.data.remote.network.HttpClientFactory
import com.ar.webwiz.domain.repository.BookmarkRepository
import com.ar.webwiz.domain.repository.HistoryRepository
import com.ar.webwiz.domain.repository.ImageRepository
import com.ar.webwiz.domain.repository.TabStateRepository
import com.ar.webwiz.utils.webview.AdBlocker
import com.ar.webwiz.viewmodel.BookmarkViewModel
import com.ar.webwiz.viewmodel.BrowserViewModel
import com.ar.webwiz.viewmodel.DownloadViewModel
import com.ar.webwiz.viewmodel.HistoryViewModel
import com.ar.webwiz.viewmodel.SSViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single { AdBlocker(get()) }
}

val databaseModule = module {
    single { DownloadDatabase.getDatabase(androidContext()) }
    single { TabDatabase.getDatabase(androidContext()) }
    single { HistoryDatabase.getDatabase(androidContext()) }
    single { BookmarkDatabase.getDatabase(androidContext())}
}

val repositoryModule = module {
    single { DownloadRepository(androidApplication(),get()) }
    single { SearchRepository(get(named("basicClient"))) }
    single { ImageRepository(get(named("googleLens")))}
    singleOf(::TabStateRepository)
    singleOf(::HistoryRepository)
    singleOf(::BookmarkRepository)
}

val networkModule = module {
    single(named("googleLens")) { HttpClientFactory.createClientForGoogleLens() }
    single(named("basicClient")) { HttpClientFactory.createBasicClient() }
}

val viewModelModule = module {
    viewModelOf(::BrowserViewModel)
    viewModel { SSViewModel(get()) }
    viewModel { DownloadViewModel(get()) }
    viewModelOf(::HistoryViewModel)
    viewModelOf(::BookmarkViewModel)
}

