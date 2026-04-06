package paige.navic.di

import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import paige.navic.domain.models.DomainSong
import paige.navic.ui.components.dialogs.DeletionViewModel
import paige.navic.ui.screens.album.viewmodels.AlbumListViewModel
import paige.navic.ui.screens.artist.viewmodels.ArtistDetailViewModel
import paige.navic.ui.screens.artist.viewmodels.ArtistListViewModel
import paige.navic.ui.screens.genre.viewmodels.GenreListViewModel
import paige.navic.ui.screens.lyrics.viewmodels.LyricsScreenViewModel
import paige.navic.ui.screens.playlist.viewmodels.PlaylistCreateDialogViewModel
import paige.navic.ui.screens.playlist.viewmodels.PlaylistListViewModel
import paige.navic.ui.screens.playlist.viewmodels.PlaylistUpdateDialogViewModel
import paige.navic.ui.screens.queue.viewmodels.QueueViewModel
import paige.navic.ui.screens.search.viewmodels.SearchViewModel
import paige.navic.ui.screens.settings.viewmodels.LyricsPriorityViewModel
import paige.navic.ui.screens.settings.viewmodels.NavtabsViewModel
import paige.navic.ui.screens.settings.viewmodels.SettingsDataStorageViewModel
import paige.navic.ui.screens.share.viewmodels.ShareDialogViewModel
import paige.navic.ui.screens.share.viewmodels.ShareListViewModel
import paige.navic.ui.screens.song.viewmodels.SongListViewModel
import paige.navic.ui.screens.track.viewmodels.TrackListViewModel
import paige.navic.ui.viewmodels.LoginViewModel

val viewModelModule = module {
	viewModelOf(::ArtistDetailViewModel)

	viewModel { (track: DomainSong?) ->
		LyricsScreenViewModel(
			track = track,
			repository = get()
		)
	}

	viewModel { (tracks: List<DomainSong>, playlistToExclude: String?) ->
		PlaylistUpdateDialogViewModel(
			tracks = tracks,
			playlistToExclude = playlistToExclude
		)
	}

	viewModelOf(::AlbumListViewModel)
	viewModel { params ->
		SongListViewModel(
			artistId = params.getOrNull(),
			repository = get()
		)
	}
	viewModelOf(::ArtistListViewModel)
	viewModelOf(::SearchViewModel)
	viewModelOf(::GenreListViewModel)
	viewModelOf(::PlaylistListViewModel)
	viewModelOf(::LoginViewModel)
	viewModelOf(::QueueViewModel)
	viewModelOf(::ShareListViewModel)
	viewModelOf(::DeletionViewModel)
	viewModelOf(::ShareDialogViewModel)
	viewModelOf(::PlaylistCreateDialogViewModel)
	viewModelOf(::TrackListViewModel)
	viewModelOf(::SettingsDataStorageViewModel)
	viewModel {
		NavtabsViewModel(
			settings = Settings(),
			json = Json
		)
	}
	viewModel {
		LyricsPriorityViewModel(
			settings = Settings(),
			json = Json
		)
	}
}