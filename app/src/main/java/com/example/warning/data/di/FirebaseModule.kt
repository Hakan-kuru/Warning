package com.example.warning.data.di

import com.example.warning.data.local.dao.ContactDao
import com.example.warning.data.local.dao.LinkedDao
import com.example.warning.data.local.dao.ProfileDao
import com.example.warning.data.remote.service.FirestoreService
import com.example.warning.data.remote.listener.ContactRealtimeSyncManager
import com.example.warning.data.remote.listener.LinkedRealtimeSyncManager
import com.example.warning.data.remote.listener.UserRealtimeSyncManager
import com.example.warning.data.repository.FirebaseRepositoryImpl
import com.example.warning.domain.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
<<<<<<< Updated upstream
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
=======
    fun provideFirebaseAuth():
            FirebaseAuth {
        val auth = FirebaseAuth.getInstance()
        if (BuildConfig.DEBUG) {
//            auth.useEmulator("10.0.2.2", 9099)
//            auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        }
        return auth
>>>>>>> Stashed changes
    }

    @Provides
    @Singleton
<<<<<<< Updated upstream
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
=======
    fun provideFirebaseFunctions(): FirebaseFunctions {
        return FirebaseFunctions.getInstance("europe-west1")
    }

    @Provides
    @Singleton
    fun provideFirestore():
            FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        if (BuildConfig.DEBUG) {
//            firestore.useEmulator("10.0.2.2", 8080)
        }
        return firestore
>>>>>>> Stashed changes
    }

    @Provides
    @Singleton
    fun provideUserSyncManager(
        firestore: FirebaseFirestore,
        profileDao: ProfileDao
    ): UserRealtimeSyncManager {
        return UserRealtimeSyncManager(firestore, profileDao)
    }

    @Provides
    @Singleton
    fun provideLinkedSyncManager(
        firestore: FirebaseFirestore,
        linkedDao: LinkedDao
    ): LinkedRealtimeSyncManager {
        return LinkedRealtimeSyncManager(firestore, linkedDao)
    }

    @Provides
    @Singleton
    fun provideContactSyncManager(
        firestore: FirebaseFirestore,
        contactDao: ContactDao
    ): ContactRealtimeSyncManager {
        return ContactRealtimeSyncManager(firestore, contactDao)
    }

    @Provides
    @Singleton
    fun provideFirestoreService(firestore: FirebaseFirestore): FirestoreService {
        return FirestoreService(firestore)
    }

    @Provides
    @Singleton
    fun provideFirebaseRepository(
        firestoreService: FirestoreService,
        syncContact: ContactRealtimeSyncManager,
        syncManagerUser: UserRealtimeSyncManager,
        syncLinked: LinkedRealtimeSyncManager
    ): FirebaseRepository {
        return FirebaseRepositoryImpl(
            firestoreService,
            syncManagerUser,
            syncLinked,
            syncContact
        )
    }
}