package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.AccountEntity
import com.example.data.model.ServiceBillerEntity
import com.example.data.model.TransactionEntity

@Database(
  entities = [AccountEntity::class, ServiceBillerEntity::class, TransactionEntity::class],
  version = 1,
  exportSchema = false,
)
@TypeConverters(BankConverters::class)
abstract class BankDatabase : RoomDatabase() {
  abstract fun bankDao(): BankDao

  companion object {
    @Volatile
    private var INSTANCE: BankDatabase? = null

    fun getInstance(context: Context): BankDatabase {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          BankDatabase::class.java,
          "novabanca_simulator.db",
        ).build().also { INSTANCE = it }
      }
    }
  }
}
