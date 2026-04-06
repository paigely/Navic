package paige.navic.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import paige.navic.domain.repositories.AlbumRepository
import paige.navic.domain.repositories.ArtistRepository
import paige.navic.domain.repositories.DbRepository
import paige.navic.domain.repositories.GenreRepository
import paige.navic.domain.repositories.LyricRepository
import paige.navic.domain.repositories.SearchRepository
import paige.navic.domain.repositories.ShareRepository
import paige.navic.domain.repositories.TrackRepository
import paige.navic.domain.repositories.PlaylistRepository

val repositoryModule = module {
	singleOf(::AlbumRepository)
	singleOf(::ArtistRepository)
	singleOf(::DbRepository)
	singleOf(::GenreRepository)
	singleOf(::LyricRepository)
	singleOf(::SearchRepository)
	singleOf(::ShareRepository)
	singleOf(::TrackRepository)
	singleOf(::PlaylistRepository)
}
