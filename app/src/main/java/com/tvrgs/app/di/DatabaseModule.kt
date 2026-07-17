package com.tvrgs.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.tvrgs.app.BuildConfig
import com.tvrgs.data.local.TVRGSDatabase
import com.tvrgs.data.local.dao.*
import com.tvrgs.data.remote.jellyfin.JellyfinProvider
import com.google.gson.Gson
import okhttp3.OkHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DEBUG_SLOW_QUERY_THRESHOLD_MS = 100L

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TVRGSDatabase =
        Room.databaseBuilder(
            context,
            TVRGSDatabase::class.java,
            "tvrgs.db"
        )
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .openHelperFactory(
                if (BuildConfig.DEBUG) {
                    SlowQueryLoggingOpenHelperFactory(
                        delegate = FrameworkSQLiteOpenHelperFactory(),
                        slowQueryThresholdMs = DEBUG_SLOW_QUERY_THRESHOLD_MS
                    )
                } else {
                    FrameworkSQLiteOpenHelperFactory()
                }
            )
            .addMigrations(
                TVRGSDatabase.MIGRATION_1_2,
                TVRGSDatabase.MIGRATION_2_3,
                TVRGSDatabase.MIGRATION_3_4,
                TVRGSDatabase.MIGRATION_4_5,
                TVRGSDatabase.MIGRATION_5_6,
                TVRGSDatabase.MIGRATION_6_7,
                TVRGSDatabase.MIGRATION_7_8,
                TVRGSDatabase.MIGRATION_8_9,
                TVRGSDatabase.MIGRATION_9_10,
                TVRGSDatabase.MIGRATION_10_11,
                TVRGSDatabase.MIGRATION_11_12,
                TVRGSDatabase.MIGRATION_12_13,
                TVRGSDatabase.MIGRATION_13_14,
                TVRGSDatabase.MIGRATION_14_15,
                TVRGSDatabase.MIGRATION_15_16,
                TVRGSDatabase.MIGRATION_16_17,
                TVRGSDatabase.MIGRATION_17_18,
                TVRGSDatabase.MIGRATION_18_19,
                TVRGSDatabase.MIGRATION_19_20,
                TVRGSDatabase.MIGRATION_20_21,
                TVRGSDatabase.MIGRATION_21_22,
                TVRGSDatabase.MIGRATION_22_23,
                TVRGSDatabase.MIGRATION_23_24,
                TVRGSDatabase.MIGRATION_24_25,
                TVRGSDatabase.MIGRATION_25_26,
                TVRGSDatabase.MIGRATION_26_27,
                TVRGSDatabase.MIGRATION_27_28,
                TVRGSDatabase.MIGRATION_28_29,
                TVRGSDatabase.MIGRATION_29_30,
                TVRGSDatabase.MIGRATION_30_31,
                TVRGSDatabase.MIGRATION_31_32,
                TVRGSDatabase.MIGRATION_32_33,
                TVRGSDatabase.MIGRATION_33_34,
                TVRGSDatabase.MIGRATION_34_35,
                TVRGSDatabase.MIGRATION_35_36,
                TVRGSDatabase.MIGRATION_36_37,
                TVRGSDatabase.MIGRATION_37_38,
                TVRGSDatabase.MIGRATION_38_39,
                TVRGSDatabase.MIGRATION_39_40,
                TVRGSDatabase.MIGRATION_40_41,
                TVRGSDatabase.MIGRATION_41_42,
                TVRGSDatabase.MIGRATION_42_43,
                TVRGSDatabase.MIGRATION_43_44,
                TVRGSDatabase.MIGRATION_44_45,
                TVRGSDatabase.MIGRATION_45_46,
                TVRGSDatabase.MIGRATION_46_47,
                TVRGSDatabase.MIGRATION_47_48,
                TVRGSDatabase.MIGRATION_48_49,
                TVRGSDatabase.MIGRATION_49_50,
                TVRGSDatabase.MIGRATION_50_51,
                TVRGSDatabase.MIGRATION_51_52,
                TVRGSDatabase.MIGRATION_52_53,
                TVRGSDatabase.MIGRATION_53_54,
                TVRGSDatabase.MIGRATION_54_55,
                TVRGSDatabase.MIGRATION_55_56,
                TVRGSDatabase.MIGRATION_56_57,
                TVRGSDatabase.MIGRATION_57_58,
                TVRGSDatabase.MIGRATION_58_59,
                TVRGSDatabase.MIGRATION_59_60,
                TVRGSDatabase.MIGRATION_60_61,
                TVRGSDatabase.MIGRATION_61_62
            )
            // NOTE: fallbackToDestructiveMigration() intentionally removed.
            // All future schema changes MUST add a corresponding Migration in TVRGSDatabase.
            .build()

    @Provides @Singleton
    fun provideJellyfinProvider(okHttpClient: OkHttpClient, gson: Gson): JellyfinProvider = JellyfinProvider(okHttpClient, gson)

    @Provides fun provideProviderDao(db: TVRGSDatabase): ProviderDao = db.providerDao()
    @Provides fun provideChannelDao(db: TVRGSDatabase): ChannelDao = db.channelDao()
    @Provides fun provideChannelPreferenceDao(db: TVRGSDatabase): ChannelPreferenceDao = db.channelPreferenceDao()
    @Provides fun provideMovieDao(db: TVRGSDatabase): MovieDao = db.movieDao()
    @Provides fun provideSeriesDao(db: TVRGSDatabase): SeriesDao = db.seriesDao()
    @Provides fun provideEpisodeDao(db: TVRGSDatabase): EpisodeDao = db.episodeDao()
    @Provides fun provideCategoryDao(db: TVRGSDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideCatalogSyncDao(db: TVRGSDatabase): CatalogSyncDao = db.catalogSyncDao()
    @Provides fun provideProgramDao(db: TVRGSDatabase): ProgramDao = db.programDao()
    @Provides fun provideFavoriteDao(db: TVRGSDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideVirtualGroupDao(db: TVRGSDatabase): VirtualGroupDao = db.virtualGroupDao()
    @Provides fun providePlaybackHistoryDao(db: TVRGSDatabase): PlaybackHistoryDao = db.playbackHistoryDao()
    @Provides fun provideTmdbIdentityDao(db: TVRGSDatabase): TmdbIdentityDao = db.tmdbIdentityDao()
    @Provides fun provideSearchHistoryDao(db: TVRGSDatabase): SearchHistoryDao = db.searchHistoryDao()
    @Provides fun provideSearchDao(db: TVRGSDatabase): SearchDao = db.searchDao()
    @Provides fun provideSyncMetadataDao(db: TVRGSDatabase): SyncMetadataDao = db.syncMetadataDao()
    @Provides fun provideMovieCategoryHydrationDao(db: TVRGSDatabase): MovieCategoryHydrationDao = db.movieCategoryHydrationDao()
    @Provides fun provideSeriesCategoryHydrationDao(db: TVRGSDatabase): SeriesCategoryHydrationDao = db.seriesCategoryHydrationDao()
    @Provides fun provideEpgSourceDao(db: TVRGSDatabase): EpgSourceDao = db.epgSourceDao()
    @Provides fun provideProviderEpgSourceDao(db: TVRGSDatabase): ProviderEpgSourceDao = db.providerEpgSourceDao()
    @Provides fun provideEpgChannelDao(db: TVRGSDatabase): EpgChannelDao = db.epgChannelDao()
    @Provides fun provideEpgProgrammeDao(db: TVRGSDatabase): EpgProgrammeDao = db.epgProgrammeDao()
    @Provides fun provideChannelEpgMappingDao(db: TVRGSDatabase): ChannelEpgMappingDao = db.channelEpgMappingDao()
    @Provides fun provideCombinedM3uProfileDao(db: TVRGSDatabase): CombinedM3uProfileDao = db.combinedM3uProfileDao()
    @Provides fun provideCombinedM3uProfileMemberDao(db: TVRGSDatabase): CombinedM3uProfileMemberDao = db.combinedM3uProfileMemberDao()
    @Provides fun provideRecordingScheduleDao(db: TVRGSDatabase): RecordingScheduleDao = db.recordingScheduleDao()
    @Provides fun provideRecordingRunDao(db: TVRGSDatabase): RecordingRunDao = db.recordingRunDao()
    @Provides fun provideProgramReminderDao(db: TVRGSDatabase): ProgramReminderDao = db.programReminderDao()
    @Provides fun provideRecordingStorageDao(db: TVRGSDatabase): RecordingStorageDao = db.recordingStorageDao()
    @Provides fun providePlaybackCompatibilityDao(db: TVRGSDatabase): PlaybackCompatibilityDao = db.playbackCompatibilityDao()
    @Provides fun provideXtreamContentIndexDao(db: TVRGSDatabase): XtreamContentIndexDao = db.xtreamContentIndexDao()
    @Provides fun provideXtreamIndexJobDao(db: TVRGSDatabase): XtreamIndexJobDao = db.xtreamIndexJobDao()
    @Provides fun provideXtreamLiveOnboardingDao(db: TVRGSDatabase): XtreamLiveOnboardingDao = db.xtreamLiveOnboardingDao()
    @Provides fun provideDownloadDao(db: TVRGSDatabase): DownloadDao = db.downloadDao()
}
