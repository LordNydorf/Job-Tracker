package com.rohit.jobtracker.android.di

import com.rohit.jobtracker.android.network.KtorJobTrackerApi
import com.rohit.jobtracker.android.network.ServerConfig
import com.rohit.jobtracker.android.ui.addedit.AddEditViewModel
import com.rohit.jobtracker.android.ui.detail.ApplicationDetailViewModel
import com.rohit.jobtracker.android.ui.list.ApplicationListViewModel
import com.rohit.jobtracker.shared.api.JobTrackerApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        ServerConfig(context = androidContext())
    }

    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    single<JobTrackerApi> {
        KtorJobTrackerApi(
            client = get(),
            serverConfig = get()
        )
    }

    viewModel { ApplicationListViewModel(api = get(), serverConfig = get()) }
    viewModel { AddEditViewModel(api = get()) }
    viewModel { (applicationId: String) -> ApplicationDetailViewModel(applicationId = applicationId, api = get()) }
}
