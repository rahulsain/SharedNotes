package com.rahuls.sharednotes.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Email::class], version = 1, exportSchema = false)
abstract class EmailDatabase: RoomDatabase() {

    abstract fun getEmailDao() : EmailDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: EmailDatabase? = null

        fun getDatabase(context: Context): EmailDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                    EmailDatabase::class.java,
                        "notes_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
