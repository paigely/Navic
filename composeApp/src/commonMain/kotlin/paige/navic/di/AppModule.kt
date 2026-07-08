package paige.navic.di

import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import paige.navic.ui.navigation.PersistentViewModelStoreOwner

val appModule = module {
	single { Settings() }
	singleOf(::PersistentViewModelStoreOwner)
}
