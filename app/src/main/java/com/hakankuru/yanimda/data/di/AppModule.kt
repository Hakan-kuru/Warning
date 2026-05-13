package com.hakankuru.yanimda.data.di

import android.content.Context
import androidx.room.Room
import com.hakankuru.yanimda.data.local.AppDatabase
import com.hakankuru.yanimda.data.local.dao.ContactDao
import com.hakankuru.yanimda.data.local.dao.EmergencyHistoryDao
import com.hakankuru.yanimda.data.local.dao.LinkedDao
import com.hakankuru.yanimda.data.local.dao.ProfileDao

import com.hakankuru.yanimda.data.remote.api.EmergencyApi
import com.hakankuru.yanimda.data.repository.EmergencyHistoryRepositoryImpl
import com.hakankuru.yanimda.data.repository.EmergencyRepositoryImpl
import com.hakankuru.yanimda.data.repository.ProfileRepositoryImpl
import com.hakankuru.yanimda.domain.repository.EmergencyHistoryRepository
import com.hakankuru.yanimda.domain.repository.EmergencyRepository

import com.hakankuru.yanimda.domain.repository.FirebaseRepository
import com.hakankuru.yanimda.domain.repository.ProfileRepository
import com.hakankuru.yanimda.domain.usecase.ProfileUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

//migrations
import com.hakankuru.yanimda.data.MIGRATION_2
import com.hakankuru.yanimda.data.MIGRATION_2_3
import com.hakankuru.yanimda.data.MIGRATION_3_4
import com.hakankuru.yanimda.data.MIGRATION_4_5
import com.hakankuru.yanimda.data.MIGRATION_5_6
import com.hakankuru.yanimda.data.MIGRATION_6_7
import com.hakankuru.yanimda.data.MIGRATION_7_8

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideEmergencyHistoryDao(db: AppDatabase): EmergencyHistoryDao = db.emergencyHistoryDao()

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext appContext: Context
    ): AppDatabase {
        return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                "profile_database"
            )
            .build()
    }

    @Provides
    fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideContactDao(db: AppDatabase): ContactDao = db.contactDao()

    @Provides
    fun provideLinkedDao(db: AppDatabase): LinkedDao = db.linkedDao()


    @Provides
    @Singleton
    fun provideProfileRepository(
        profileDao: ProfileDao,
        contactDao: ContactDao,
        linkedDao: LinkedDao
    ): ProfileRepository {
        return ProfileRepositoryImpl(
            profileDao,
            linkedDao,
            contactDao
        )
    }

    @Provides
    @Singleton
    fun provideProfileUseCases(repository: ProfileRepository, firebaseRepo: FirebaseRepository): ProfileUseCases {
        return ProfileUseCases(repository, firebaseRepo)
    }


    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://us-central1-warning-5d457.cloudfunctions.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 60 saniye yaptık
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
    @Provides
    @Singleton
    fun provideEmergencyRepository(
        emergencyApi: EmergencyApi
    ): EmergencyRepository {
        return EmergencyRepositoryImpl(emergencyApi)
    }

    @Provides
    @Singleton
    fun provideEmergencyApi(retrofit: Retrofit): EmergencyApi {
        return retrofit.create(EmergencyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEmergencyHistoryRepository(
        emergencyHistoryDao: EmergencyHistoryDao
    ): EmergencyHistoryRepository {
        return EmergencyHistoryRepositoryImpl(emergencyHistoryDao)
    }

    @Provides
    @Singleton
    fun provideThemeRepository(
        @ApplicationContext context: Context
    ): com.hakankuru.yanimda.domain.repository.ThemeRepository {
        return com.hakankuru.yanimda.data.repository.ThemeRepositoryImpl(context)
    }
}