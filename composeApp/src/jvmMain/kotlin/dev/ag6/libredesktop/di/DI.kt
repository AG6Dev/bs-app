package dev.ag6.libredesktop.di

import com.github.javakeyring.Keyring
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import dev.ag6.libredesktop.AppContext
import dev.ag6.libredesktop.notifications.GlucoseAlertNotifier
import dev.ag6.libredesktop.repository.auth.AuthRepository
import dev.ag6.libredesktop.repository.auth.AuthRepositoryImpl
import dev.ag6.libredesktop.repository.readings.ReadingsRepository
import dev.ag6.libredesktop.repository.readings.ReadingsRepositoryImpl
import dev.ag6.libredesktop.repository.settings.SettingsRepository
import dev.ag6.libredesktop.repository.settings.SettingsRepositoryImpl
import dev.ag6.libredesktop.ui.alarms.AlarmsScreenModel
import dev.ag6.libredesktop.ui.auth.AuthScreenModel
import dev.ag6.libredesktop.ui.overview.OverviewScreenModel
import dev.ag6.libredesktop.ui.settings.SettingsScreenModel
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
    single { AppContext(get(), get()) } onClose { it?.close() }
    single { GlucoseAlertNotifier(get(), get()) } onClose { it?.close() }

    single<Keyring> { Keyring.create() } onClose { it?.close() }
}

fun viewModelModule() = module {
    factory { AuthScreenModel(get()) }
    factory { OverviewScreenModel(get(), get()) }
    factory { AlarmsScreenModel(get()) }
    factory { SettingsScreenModel(get(), get()) }
}
