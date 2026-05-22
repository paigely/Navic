package paige.navic.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import paige.navic.domain.repositories.AlbumRepository
import paige.navic.domain.repositories.ArtistRepository
import paige.navic.domain.repositories.CollectionRepository
import paige.navic.domain.repositories.DbRepository
import paige.navic.domain.repositories.GenreRepository
import paige.navic.domain.repositories.LyricRepository
import paige.navic.domain.repositories.PlaylistRepository
import paige.navic.domain.repositories.RadioRepository
import paige.navic.domain.repositories.SearchRepository
import paige.navic.domain.repositories.ShareRepository
import paige.navic.domain.repositories.SongRepository

val repositoryModule = module {
	singleOf(::AlbumRepository)
	singleOf(::ArtistRepository)
	singleOf(::DbRepository)
	singleOf(::GenreRepository)
	singleOf(::LyricRepository)
	singleOf(::SearchRepository)
	singleOf(::ShareRepository)
	singleOf(::CollectionRepository)
	singleOf(::PlaylistRepository)
	singleOf(::SongRepository)
	singleOf(::RadioRepository)
}
