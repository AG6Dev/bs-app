package dev.ag6.bs_app.di

import com.russhwolf.settings.Settings
import dev.ag6.bs_app.repository.AuthRepository
import dev.ag6.bs_app.repository.AuthRepositoryImpl
import dev.ag6.bs_app.screen.auth.AuthScreenModel
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

fun initKoin() = startKoin {
    modules(appModule())
}

fun appModule() = module {
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }

    single<Settings> { Settings() }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    factory { AuthScreenModel(get()) }
}