package dev.ag6.libredesktop.di

import com.github.javakeyring.Keyring
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import dev.ag6.libredesktop.repository.auth.AuthRepository
import dev.ag6.libredesktop.repository.auth.AuthRepositoryImpl
import dev.ag6.libredesktop.repository.readings.ReadingsRepository
import dev.ag6.libredesktop.repository.readings.ReadingsRepositoryImpl
import dev.ag6.libredesktop.repository.settings.SettingsRepository
import dev.ag6.libredesktop.repository.settings.SettingsRepositoryImpl
import dev.ag6.libredesktop.ui.auth.AuthScreenModel
import dev.ag6.libredesktop.ui.overview.OverviewScreenModel
import dev.ag6.libredesktop.ui.screen.SettingsScreenModel
import dev.ag6.libredesktop.ui.theme.AppThemeController
import io.ktor.client.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.util.prefs.Preferences

fun initKoin() = startKoin {
    modules(appModule() + viewModelModule())
}

fun appModule() = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
    }

    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(get())
            }
            install(ContentEncoding) {
                gzip()
            }
        }
    }

    single<ObservableSettings> { PreferencesSettings(Preferences.userRoot().node("dev/ag6/libredesktop")) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), get()) }
    single<ReadingsRepository> { ReadingsRepositoryImpl(get(), get(), get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single { AppThemeController(get()) }

    single<Keyring> { Keyring.create() } onClose {
        it?.close()
    }
}

fun viewModelModule() = module {
    factory { AuthScreenModel(get()) }
    factory { OverviewScreenModel(get(), get()) }
    factory { SettingsScreenModel(get(), get()) }
}
