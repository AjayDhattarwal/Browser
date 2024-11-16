package com.ar.idm.di

import com.ar.idm.data.local.roomdatabase.bookmarkDb.BookmarkDatabase
import com.ar.idm.domain.repository.SearchRepository
import com.ar.idm.utils.download.DownloadRepository
import com.ar.idm.data.local.roomdatabase.downloadDb.DownloadDatabase
import com.ar.idm.data.local.roomdatabase.historyDb.HistoryDatabase
import com.ar.idm.data.local.roomdatabase.tabstate.TabDatabase
import com.ar.idm.data.remote.network.HttpClientFactory
import com.ar.idm.domain.repository.BookmarkRepository
import com.ar.idm.domain.repository.HistoryRepository
import com.ar.idm.domain.repository.ImageRepository
import com.ar.idm.domain.repository.TabStateRepository
import com.ar.idm.utils.webview.AdBlocker
import com.ar.idm.viewmodel.BookmarkViewModel
import com.ar.idm.viewmodel.BrowserViewModel
import com.ar.idm.viewmodel.DownloadViewModel
import com.ar.idm.viewmodel.HistoryViewModel
import com.ar.idm.viewmodel.SSViewModel
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

