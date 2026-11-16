package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountEntity
import com.example.data.model.ServiceBillerEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {
  @Query("SELECT * FROM account WHERE id = 1 LIMIT 1")
  fun observeAccount(): Flow<AccountEntity?>

  @Query("SELECT * FROM account WHERE id = 1 LIMIT 1")
  suspend fun getAccountOnce(): AccountEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAccount(account: AccountEntity)

  @Query("SELECT * FROM service_billers ORDER BY isPaid ASC, id ASC")
  fun observeServices(): Flow<List<ServiceBillerEntity>>

  @Query("SELECT * FROM service_billers WHERE id = :id LIMIT 1")
  suspend fun getServiceById(id: Int): ServiceBillerEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertServices(services: List<ServiceBillerEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertService(service: ServiceBillerEntity): Long

  @Update
  suspend fun updateService(service: ServiceBillerEntity)

  @Query("DELETE FROM service_billers")
  suspend fun clearServices()

  @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC, id DESC")
  fun observeTransactions(): Flow<List<TransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity): Long

  @Update
  suspend fun updateTransaction(transaction: TransactionEntity)

  @Query("DELETE FROM transactions")
  suspend fun clearTransactions()
}
