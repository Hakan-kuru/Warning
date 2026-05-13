package com.hakankuru.yanimda.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hakankuru.yanimda.data.local.dao.ContactDao
import com.hakankuru.yanimda.data.local.dao.EmergencyHistoryDao
import com.hakankuru.yanimda.data.local.dao.LinkedDao
import com.hakankuru.yanimda.data.local.dao.ProfileDao
import com.hakankuru.yanimda.data.local.entity.ContactEntity
import com.hakankuru.yanimda.data.local.entity.IncomingEmergencyEntity
import com.hakankuru.yanimda.data.local.entity.LinkedEntity
import com.hakankuru.yanimda.data.local.entity.OutgoingEmergencyEntity
import com.hakankuru.yanimda.data.local.entity.ProfileEntity

@Database(
    entities = [
        ProfileEntity::class,
        LinkedEntity::class,
        ContactEntity::class,
        IncomingEmergencyEntity::class,
        OutgoingEmergencyEntity::class
               ],
    version = 8,
    exportSchema = true
)
@TypeConverters()
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun contactDao(): ContactDao
    abstract fun linkedDao(): LinkedDao
    abstract fun emergencyHistoryDao(): EmergencyHistoryDao
}
