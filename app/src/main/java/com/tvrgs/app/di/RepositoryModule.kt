package com.tvrgs.app.di

import com.tvrgs.data.local.DatabaseTransactionRunner
import com.tvrgs.data.local.RoomDatabaseTransactionRunner
import com.tvrgs.data.manager.DownloadManagerImpl
import com.tvrgs.data.preferences.PreferencesRepository
import com.tvrgs.data.security.AndroidKeystoreCredentialCrypto
import com.tvrgs.data.security.CredentialCrypto
import com.tvrgs.data.sync.ProviderSyncStateReaderImpl
import com.tvrgs.data.validation.ProviderSetupInputValidatorImpl
import com.tvrgs.domain.manager.ParentalPinVerifier
import com.tvrgs.domain.manager.ProviderSetupInputValidator
import com.tvrgs.domain.manager.ProviderSyncStateReader
import com.tvrgs.data.repository.*
import com.tvrgs.domain.manager.ParentalControlSessionStore
import com.tvrgs.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindProviderRepository(impl: ProviderRepositoryImpl): ProviderRepository

    @Binds @Singleton
    abstract fun bindChannelRepository(impl: ChannelRepositoryImpl): ChannelRepository

    @Binds @Singleton
    abstract fun bindCombinedM3uRepository(impl: CombinedM3uRepositoryImpl): CombinedM3uRepository

    @Binds @Singleton
    abstract fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository

    @Binds @Singleton
    abstract fun bindSeriesRepository(impl: SeriesRepositoryImpl): SeriesRepository

    @Binds @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds @Singleton
    abstract fun bindEpgRepository(impl: EpgRepositoryImpl): EpgRepository

    @Binds @Singleton
    abstract fun bindEpgSourceRepository(impl: EpgSourceRepositoryImpl): EpgSourceRepository

    @Binds @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds @Singleton
    abstract fun bindPlaybackHistoryRepository(impl: PlaybackHistoryRepositoryImpl): PlaybackHistoryRepository

    @Binds @Singleton
    abstract fun bindExternalRatingsRepository(impl: ExternalRatingsRepositoryImpl): ExternalRatingsRepository

    @Binds @Singleton
    abstract fun bindSyncMetadataRepository(impl: SyncMetadataRepositoryImpl): SyncMetadataRepository

    @Binds @Singleton
    abstract fun bindPlaybackCompatibilityRepository(impl: PlaybackCompatibilityRepositoryImpl): PlaybackCompatibilityRepository

    @Binds @Singleton
    abstract fun bindDatabaseTransactionRunner(impl: RoomDatabaseTransactionRunner): DatabaseTransactionRunner

    @Binds @Singleton
    abstract fun bindBackupManager(impl: com.tvrgs.data.manager.BackupManagerImpl): com.tvrgs.domain.manager.BackupManager

    @Binds @Singleton
    abstract fun bindDriveBackupSyncManager(impl: com.tvrgs.data.manager.GoogleDriveBackupSyncManager): com.tvrgs.domain.manager.DriveBackupSyncManager

    @Binds @Singleton
    abstract fun bindRecordingManager(impl: com.tvrgs.data.manager.RecordingManagerImpl): com.tvrgs.domain.manager.RecordingManager

    @Binds @Singleton
    abstract fun bindDownloadManager(impl: DownloadManagerImpl): DownloadManager

    @Binds @Singleton
    abstract fun bindProgramReminderManager(impl: com.tvrgs.data.manager.ProgramReminderManagerImpl): com.tvrgs.domain.manager.ProgramReminderManager

    @Binds @Singleton
    abstract fun bindParentalControlSessionStore(impl: PreferencesRepository): ParentalControlSessionStore

    @Binds @Singleton
    abstract fun bindParentalPinVerifier(impl: PreferencesRepository): ParentalPinVerifier

    @Binds @Singleton
    abstract fun bindProviderSetupInputValidator(impl: ProviderSetupInputValidatorImpl): ProviderSetupInputValidator

    @Binds @Singleton
    abstract fun bindProviderSyncStateReader(impl: ProviderSyncStateReaderImpl): ProviderSyncStateReader

    @Binds @Singleton
    abstract fun bindCredentialCrypto(impl: AndroidKeystoreCredentialCrypto): CredentialCrypto

    @Binds @Singleton
    abstract fun bindMultiDnsRepository(impl: com.tvrgs.data.repository.MultiDnsRepositoryImpl): MultiDnsRepository

    companion object {
        @Provides
        @Singleton
        fun provideRepositoryCoroutineScope(): CoroutineScope {
            return CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }

        @Provides
        @Singleton
        fun provideM3uParser(): com.tvrgs.data.parser.M3uParser {
            return com.tvrgs.data.parser.M3uParser()
        }
    }
}
