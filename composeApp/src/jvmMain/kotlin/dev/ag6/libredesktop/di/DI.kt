package dev.ag6.libredesktop.di

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import dev.ag6.libredesktop.repository.auth.AuthRepository
import dev.ag6.libredesktop.repository.auth.AuthRepositoryImpl
import dev.ag6.libredesktop.repository.readings.ReadingsRepository
import dev.ag6.libredesktop.repository.readings.ReadingsRepositoryImpl
import dev.ag6.libredesktop.ui.auth.AuthScreenModel
import dev.ag6.libredesktop.ui.overview.OverviewScreenModel
import io.ktor.client.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import java.util.prefs.Preferences

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
            install(ContentEncoding) {
                gzip()
            }
        }
    }

    single<Settings> { PreferencesSettings(Preferences.userRoot().node("dev/ag6/libredesktop")) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    single<ReadingsRepository> { ReadingsRepositoryImpl(get(), get(), get()) }

    factory { AuthScreenModel(get()) }

    factory { OverviewScreenModel(get()) }
}
