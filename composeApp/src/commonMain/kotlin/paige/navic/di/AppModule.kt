package paige.navic.di

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import org.koin.dsl.module

val appModule = module {
	single { HttpClient() }
	single { Settings() }
}